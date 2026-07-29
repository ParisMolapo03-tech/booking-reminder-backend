package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.dto.BookingDto;
import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.entity.Business;
import com.parismolapo.bookingreminder.entity.Customer;
import com.parismolapo.bookingreminder.exception.BadRequestException;
import com.parismolapo.bookingreminder.exception.NotFoundException;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.repository.CustomerRepository;
import com.parismolapo.bookingreminder.repository.spec.BookingSpecifications;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Response<BookingDto> createBooking(BookingDto dto) {

        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new NotFoundException(
                        "Business not found with id " + dto.getBusinessId()));

        if (!business.isActive()) {
            throw new BadRequestException("This business account is not active");
        }

        LocalDateTime appointment = LocalDateTime.of(
                dto.getAppointmentDate(),
                dto.getAppointmentTime());

        if (!appointment.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Appointment must be in the future");
        }

        Customer customer = findOrCreateCustomer(
                dto.getCustomerName(),
                dto.getCustomerPhoneNumber());

        if (customer.isOptedOut()) {
            throw new BadRequestException(
                    "This customer has opted out of messages and cannot be booked");
        }

        boolean alreadyBooked = bookingRepository
                .existsByBusinessIdAndCustomerIdAndAppointmentTimeAndStatusNot(
                        business.getId(),
                        customer.getId(),
                        appointment,
                        BookingStatus.CANCELLED);

        if (alreadyBooked) {
            throw new BadRequestException(
                    "This customer already has a booking at that time");
        }

        Booking booking = Booking.builder()
                .business(business)
                .customer(customer)
                .appointmentTime(appointment)
                .service(dto.getService())
                .status(BookingStatus.PENDING)
                .reminderSent(false)
                .build();

        Booking saved = bookingRepository.save(booking);

        return Response.success("Booking created", mapToDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<BookingDto>> getBookingsForBusiness(
            Long businessId,
            LocalDate date,
            LocalDate from,
            LocalDate to,
            String status,
            String search) {

        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found with id " + businessId);
        }

        // a single date wins over a from/to range
        LocalDate effectiveFrom = (date != null) ? date : from;
        LocalDate effectiveTo = (date != null) ? date : to;

        if (effectiveFrom != null && effectiveTo != null
                && effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("'from' date cannot be after 'to' date");
        }

        BookingStatus statusFilter = parseStatus(status);

        LocalDateTime fromTime =
                (effectiveFrom == null) ? null : effectiveFrom.atStartOfDay();

        LocalDateTime toTime =
                (effectiveTo == null) ? null : effectiveTo.atTime(LocalTime.MAX);

        Specification<Booking> spec = BookingSpecifications.forBusiness(businessId)
                .and(BookingSpecifications.withStatus(statusFilter))
                .and(BookingSpecifications.from(fromTime))
                .and(BookingSpecifications.to(toTime))
                .and(BookingSpecifications.search(search));

        List<BookingDto> bookings = bookingRepository
                .findAll(spec, Sort.by(Sort.Direction.ASC, "appointmentTime"))
                .stream()
                .map(this::mapToDto)
                .toList();

        return Response.success("Bookings retrieved", bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<BookingDto> getBookingById(Long id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

        return Response.success("Booking retrieved", mapToDto(booking));
    }

    @Override
    @Transactional
    public Response<BookingDto> updateStatus(Long id, String status) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id " + id));

        BookingStatus newStatus = parseStatus(status);

        if (newStatus == null) {
            throw new BadRequestException("Status is required");
        }

        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);

        return Response.success("Booking status updated", mapToDto(saved));
    }

    // ---------- helpers ----------

    private BookingStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid status. Use PENDING, CONFIRMED, CANCELLED, COMPLETED or NO_SHOW");
        }
    }

    private Customer findOrCreateCustomer(String name, String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> customerRepository.save(
                        Customer.builder()
                                .name(name)
                                .phoneNumber(phoneNumber)
                                .optedOut(false)
                                .build()));
    }

    private BookingDto mapToDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .businessId(booking.getBusiness().getId())
                .businessName(booking.getBusiness().getName())
                .customerName(booking.getCustomer().getName())
                .customerPhoneNumber(booking.getCustomer().getPhoneNumber())
                .appointmentDate(booking.getAppointmentTime().toLocalDate())
                .appointmentTime(booking.getAppointmentTime().toLocalTime())
                .service(booking.getService())
                .status(booking.getStatus().name())
                .reminderSent(booking.isReminderSent())
                .build();
    }
}