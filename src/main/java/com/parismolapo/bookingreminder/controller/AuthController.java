package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.AuthResponseDto;
import com.parismolapo.bookingreminder.dto.LoginRequestDto;
import com.parismolapo.bookingreminder.dto.RegisterRequestDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<String>> register(
            @RequestBody @Valid RegisterRequestDto dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<AuthResponseDto>> login(
            @RequestBody @Valid LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}