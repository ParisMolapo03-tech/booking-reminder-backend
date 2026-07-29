package com.parismolapo.bookingreminder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncomingMessageDto {

    @NotBlank(message = "Sender phone number is required")
    private String fromPhoneNumber;

    @NotBlank(message = "Message text is required")
    private String message;
}