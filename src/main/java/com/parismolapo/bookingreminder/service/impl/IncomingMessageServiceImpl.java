package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.dto.IncomingMessageDto;
import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.entity.Customer;
import com.parismolapo.bookingreminder.messaging.MessageSender;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import com.parismolapo.bookingreminder.repository.CustomerRepository;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.IncomingMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingMessageServiceImpl implements IncomingMessageService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final MessageSender messageSender;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public Response<String> handleIncoming(IncomingMessageDto dto) {

        String from = dto.getFromPhoneNumber().trim();
        String keyword = dto.getMessage().trim().toUpperCase();

        Optional<Customer> found = customerRepository.findByPhoneNumber(from);

        if (found.isEmpty()) {
            log.info("Message from unknown number {}", from);
            messageSender.send(from,
                    "Sorry, we do not have a booking linked to this number.");
            return Response.success("Unknown sender", "IGNORED");
        }

        Customer customer = found.get();

        // STOP is handled first and always, even if they have no bookings
        if (keyword.equals("STOP")) {
            return handleStop(customer);
        }

        Optional<Booking> next = findNextBooking(customer);

        if (next.isEmpty()) {
            messageSender.send(from,
                    "You have no upcoming bookings with us at the moment.");
            return Response.success("No upcoming booking", "NO_BOOKING");
        }

        Booking booking = next.get();

        return switch (keyword) {
            case "YES", "Y", "CONFIRM" -> handleConfirm(customer, booking);
            case "CANCEL", "NO", "N" -> handleCancel(customer, booking);
            default -> handleUnknown(customer);
        };
    }

    // ---------- handlers ----------

    private Response<String> handleStop(Customer customer) {

        customer.setOptedOut(true);
        customerRepository.save(customer);

        log.info("Customer {} opted out", customer.getPhoneNumber());

        messageSender.send(customer.getPhoneNumber(),
                "You have been opted out and will not receive further messages "
                        + "from us. Your existing bookings still stand.");

        return Response.success("Customer opted out", "OPTED_OUT");
    }

    private Response<String> handleConfirm(Customer customer, Booking booking) {

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        messageSender.send(customer.getPhoneNumber(),
                String.format("Thank you %s, your appointment on %s at %s is confirmed.",
                        customer.getName(),
                        booking.getAppointmentTime().format(DATE_FORMAT),
                        booking.getAppointmentTime().format(TIME_FORMAT)));

        return Response.success("Booking confirmed", "CONFIRMED");
    }

    private Response<String> handleCancel(Customer customer, Booking booking) {

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        messageSender.send(customer.getPhoneNumber(),
                String.format("Your appointment on %s at %s has been cancelled. "
                                + "Contact us any time to rebook.",
                        booking.getAppointmentTime().format(DATE_FORMAT),
                        booking.getAppointmentTime().format(TIME_FORMAT)));

        // alert the owner so she can fill the slot
        String ownerNumber = booking.getBusiness().getOwnerPhoneNumber();

        if (ownerNumber != null) {
            messageSender.send(ownerNumber,
                    String.format("CANCELLATION: %s has cancelled their %s appointment "
                                    + "on %s at %s. The slot is now free.",
                            customer.getName(),
                            booking.getService() == null ? "" : booking.getService(),
                            booking.getAppointmentTime().format(DATE_FORMAT),
                            booking.getAppointmentTime().format(TIME_FORMAT)));
        }

        return Response.success("Booking cancelled", "CANCELLED");
    }

    private Response<String> handleUnknown(Customer customer) {

        messageSender.send(customer.getPhoneNumber(),
                "Sorry, we did not understand that. Reply YES to confirm, "
                        + "CANCEL to cancel, or STOP to opt out of messages.");

        return Response.success("Unrecognised keyword", "UNKNOWN");
    }

    // ---------- helpers ----------

    private Optional<Booking> findNextBooking(Customer customer) {

        List<Booking> upcoming = bookingRepository
                .findByCustomerIdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(
                        customer.getId(), LocalDateTime.now());

        return upcoming.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .findFirst();
    }
}