package com.parismolapo.bookingreminder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class BusinessDto {

    private Long id;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be 2-100 characters")
    private String name;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Enter a valid South African mobile number"
    )
    private String whatsappNumber;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Owner phone number is required")
    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Enter a valid South African mobile number"
    )
    private String ownerPhoneNumber;

    private Boolean active;

    // output only
    private LocalDateTime createdAt;
}