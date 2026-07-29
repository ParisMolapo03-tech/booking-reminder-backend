package com.parismolapo.bookingreminder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Your name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Enter a valid South African mobile number"
    )
    private String phoneNumber;

    // the business being registered at the same time
    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business WhatsApp number is required")
    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Enter a valid South African mobile number"
    )
    private String businessWhatsappNumber;
}