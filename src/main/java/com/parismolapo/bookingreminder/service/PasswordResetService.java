package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.ForgotPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.OtpChannelOptionDto;
import com.parismolapo.bookingreminder.dto.ResetPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.SendOtpRequestDto;
import com.parismolapo.bookingreminder.response.Response;

import java.util.List;

public interface PasswordResetService {

    Response<List<OtpChannelOptionDto>> getChannels(ForgotPasswordRequestDto dto);

    Response<String> sendOtp(SendOtpRequestDto dto);

    Response<String> resetPassword(ResetPasswordRequestDto dto);
}