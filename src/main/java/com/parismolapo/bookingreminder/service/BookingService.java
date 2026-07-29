package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.BookingDto;
import com.parismolapo.bookingreminder.response.Response;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Response<BookingDto> createBooking(BookingDto dto);

    Response<List<BookingDto>> getBookingsForBusiness(
            Long businessId,
            LocalDate date,
            LocalDate from,
            LocalDate to,
            String status,
            String search);

    Response<BookingDto> getBookingById(Long id);

    Response<BookingDto> updateStatus(Long id, String status);
}