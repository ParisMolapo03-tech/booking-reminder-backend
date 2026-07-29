package com.parismolapo.bookingreminder.scheduler;

import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.messaging.MessageSender;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final MessageSender messageSender;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendDueReminders() {

        LocalDateTime windowStart = LocalDateTime.now().plusHours(23);
        LocalDateTime windowEnd = LocalDateTime.now().plusHours(25);

        List<Booking> due = bookingRepository
                .findByReminderSentFalseAndStatusNotAndAppointmentTimeBetween(
                        BookingStatus.CANCELLED,
                        windowStart,
                        windowEnd);

        if (due.isEmpty()) {
            log.debug("No reminders due");
            return;
        }

        log.info("Found {} reminder(s) to send", due.size());

        for (Booking booking : due) {

            if (booking.getCustomer().isOptedOut()) {
                log.info("Skipping opted-out customer {}",
                        booking.getCustomer().getPhoneNumber());
                continue;
            }

            String message = buildReminder(booking);

            messageSender.send(booking.getCustomer().getPhoneNumber(), message);

            booking.setReminderSent(true);
            booking.setReminderSentAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }
    }

    private String buildReminder(Booking booking) {
        return String.format(
                "Hi %s, reminder of your appointment at %s on %s at %s%s. "
                        + "Reply YES to confirm or CANCEL if you cannot make it. "
                        + "Reply STOP to opt out of messages.",
                booking.getCustomer().getName(),
                booking.getBusiness().getName(),
                booking.getAppointmentTime().format(DATE_FORMAT),
                booking.getAppointmentTime().format(TIME_FORMAT),
                booking.getService() == null ? "" : " for " + booking.getService()
        );
    }
}