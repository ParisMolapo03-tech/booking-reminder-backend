package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.entity.Business;
import com.parismolapo.bookingreminder.messaging.MessageSender;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySummaryServiceImpl implements DailySummaryService {

    private final BusinessRepository businessRepository;
    private final BookingRepository bookingRepository;
    private final MessageSender messageSender;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public void sendSummariesFor(LocalDate date) {

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<Business> businesses = businessRepository.findByActiveTrue();

        log.info("Building daily summaries for {} active business(es) on {}",
                businesses.size(), date);

        for (Business business : businesses) {

            List<Booking> bookings = bookingRepository
                    .findByBusinessIdAndAppointmentTimeBetween(
                            business.getId(), dayStart, dayEnd)
                    .stream()
                    .sorted(Comparator.comparing(Booking::getAppointmentTime))
                    .toList();

            String message = buildSummary(business, date, bookings);

            messageSender.send(business.getOwnerPhoneNumber(), message);
        }
    }

    private String buildSummary(Business business, LocalDate date, List<Booking> bookings) {

        StringBuilder sb = new StringBuilder();

        sb.append("Good morning ")
                .append(business.getOwnerName() == null ? "" : business.getOwnerName())
                .append("\n")
                .append(business.getName())
                .append(" - ")
                .append(date.format(DATE_FORMAT))
                .append("\n\n");

        List<Booking> active = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList();

        if (active.isEmpty()) {
            sb.append("You have no bookings today.");
            return sb.toString();
        }

        long confirmed = active.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();

        long awaiting = active.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();

        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();

        sb.append("You have ").append(active.size()).append(" booking(s) today:\n");

        for (Booking b : active) {
            sb.append("  ")
                    .append(b.getAppointmentTime().format(TIME_FORMAT))
                    .append("  ")
                    .append(b.getCustomer().getName())
                    .append(b.getService() == null ? "" : " - " + b.getService())
                    .append("  [")
                    .append(b.getStatus().name())
                    .append("]\n");
        }

        sb.append("\nConfirmed: ").append(confirmed)
                .append("  |  Awaiting reply: ").append(awaiting)
                .append("  |  Cancelled: ").append(cancelled);

        return sb.toString();
    }
}