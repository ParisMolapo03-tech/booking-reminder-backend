package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.BookingDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.security.OwnershipGuard;
import com.parismolapo.bookingreminder.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final OwnershipGuard ownershipGuard;

    @PostMapping
    public ResponseEntity<Response<BookingDto>> create(
            @RequestBody @Valid BookingDto dto) {

        ownershipGuard.checkBusinessAccess(dto.getBusinessId());

        return ResponseEntity.ok(bookingService.createBooking(dto));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Response<List<BookingDto>>> getForBusiness(
            @PathVariable Long businessId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String status,

            @RequestParam(required = false) String search) {

        ownershipGuard.checkBusinessAccess(businessId);

        return ResponseEntity.ok(bookingService.getBookingsForBusiness(
                businessId, date, from, to, status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<BookingDto>> getById(@PathVariable Long id) {

        Response<BookingDto> booking = bookingService.getBookingById(id);

        ownershipGuard.checkBusinessAccess(booking.getData().getBusinessId());

        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<BookingDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Response<BookingDto> existing = bookingService.getBookingById(id);

        ownershipGuard.checkBusinessAccess(existing.getData().getBusinessId());

        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }
}