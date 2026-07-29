package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.AuthResponseDto;
import com.parismolapo.bookingreminder.dto.LoginRequestDto;
import com.parismolapo.bookingreminder.dto.RegisterRequestDto;
import com.parismolapo.bookingreminder.response.Response;

public interface AuthService {

    Response<String> register(RegisterRequestDto dto);

    Response<AuthResponseDto> login(LoginRequestDto dto);
}