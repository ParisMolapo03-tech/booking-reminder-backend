package com.parismolapo.bookingreminder.service;

import java.time.LocalDate;

public interface DailySummaryService {

    void sendSummariesFor(LocalDate date);
}