package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.ForgotPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.OtpChannelOptionDto;
import com.parismolapo.bookingreminder.dto.ResetPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.SendOtpRequestDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<Response<List<OtpChannelOptionDto>>> forgot(
            @RequestBody @Valid ForgotPasswordRequestDto dto) {
        return ResponseEntity.ok(passwordResetService.getChannels(dto));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Response<String>> sendOtp(
            @RequestBody @Valid SendOtpRequestDto dto) {
        return ResponseEntity.ok(passwordResetService.sendOtp(dto));
    }

    @PostMapping("/reset")
    public ResponseEntity<Response<String>> reset(
            @RequestBody @Valid ResetPasswordRequestDto dto) {
        return ResponseEntity.ok(passwordResetService.resetPassword(dto));
    }
}