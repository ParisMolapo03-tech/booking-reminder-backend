package com.parismolapo.bookingreminder.scheduler;

import com.parismolapo.bookingreminder.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailySummaryScheduler {

    private final DailySummaryService dailySummaryService;

    @Scheduled(cron = "0 0 7 * * *", zone = "Africa/Johannesburg")
    public void sendMorningSummaries() {
        dailySummaryService.sendSummariesFor(LocalDate.now());
    }
}