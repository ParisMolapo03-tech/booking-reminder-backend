package com.parismolapo.bookingreminder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto {

    private Long id;

    @NotNull(message = "Business id is required")
    private Long businessId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone number is required")
    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Enter a valid South African mobile number"
    )
    private String customerPhoneNumber;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    private String service;

    // output only
    private String businessName;
    private String status;
    private Boolean reminderSent;
}