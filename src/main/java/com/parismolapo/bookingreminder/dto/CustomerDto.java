package com.parismolapo.bookingreminder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private Boolean optedOut;
    private LocalDateTime createdAt;

    // detail view only - left null in list views to avoid N+1 queries
    private Long totalBookings;
    private Long cancelledBookings;
    private List<BookingDto> bookings;
}