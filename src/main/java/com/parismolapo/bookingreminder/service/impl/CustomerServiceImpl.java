package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.dto.BookingDto;
import com.parismolapo.bookingreminder.dto.CustomerDto;
import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.entity.Customer;
import com.parismolapo.bookingreminder.exception.NotFoundException;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.repository.CustomerRepository;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Response<List<CustomerDto>> getAllCustomers() {

        List<CustomerDto> customers = customerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Customer::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .map(this::mapToSummaryDto)
                .toList();

        return Response.success("Customers retrieved", customers);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<CustomerDto>> getCustomersForBusiness(Long businessId) {

        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found with id " + businessId);
        }

        List<CustomerDto> customers = customerRepository.findAllByBusinessId(businessId)
                .stream()
                .sorted(Comparator.comparing(Customer::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .map(this::mapToSummaryDto)
                .toList();

        return Response.success("Customers retrieved", customers);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CustomerDto> getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id " + id));

        List<BookingDto> bookings = bookingRepository
                .findByCustomerIdOrderByAppointmentTimeDesc(id)
                .stream()
                .map(this::mapBookingToDto)
                .toList();

        CustomerDto dto = mapToSummaryDto(customer);
        dto.setTotalBookings(bookingRepository.countByCustomerId(id));
        dto.setCancelledBookings(
                bookingRepository.countByCustomerIdAndStatus(id, BookingStatus.CANCELLED));
        dto.setBookings(bookings);

        return Response.success("Customer retrieved", dto);
    }

    @Override
    @Transactional
    public Response<CustomerDto> setOptedOut(Long id, boolean optedOut) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id " + id));

        customer.setOptedOut(optedOut);
        Customer saved = customerRepository.save(customer);

        log.info("Customer {} opt-out set to {} via API",
                saved.getPhoneNumber(), optedOut);

        return Response.success(
                optedOut
                        ? "Customer opted out of messages"
                        : "Customer opted back in to messages",
                mapToSummaryDto(saved));
    }

    // ---------- helpers ----------

    private CustomerDto mapToSummaryDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .optedOut(customer.isOptedOut())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private BookingDto mapBookingToDto(Booking booking) {
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