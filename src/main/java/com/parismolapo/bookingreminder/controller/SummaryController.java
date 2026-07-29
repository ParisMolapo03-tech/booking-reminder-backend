package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final DailySummaryService dailySummaryService;

    @PostMapping("/run")
    public ResponseEntity<Response<String>> run(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = (date == null) ? LocalDate.now() : date;

        dailySummaryService.sendSummariesFor(target);

        return ResponseEntity.ok(
                Response.success("Daily summaries generated for " + target, null));
    }
}