package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.BookingDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Response<BookingDto>> create(
            @RequestBody @Valid BookingDto dto) {
        return ResponseEntity.ok(bookingService.createBooking(dto));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Response<List<BookingDto>>> getForBusiness(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(bookingService.getBookingsForBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<BookingDto>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<BookingDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }
}