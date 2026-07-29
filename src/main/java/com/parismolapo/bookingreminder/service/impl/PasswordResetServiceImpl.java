package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.dto.ForgotPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.OtpChannelOptionDto;
import com.parismolapo.bookingreminder.dto.ResetPasswordRequestDto;
import com.parismolapo.bookingreminder.dto.SendOtpRequestDto;
import com.parismolapo.bookingreminder.entity.OtpChannel;
import com.parismolapo.bookingreminder.entity.PasswordResetOtp;
import com.parismolapo.bookingreminder.entity.User;
import com.parismolapo.bookingreminder.exception.BadRequestException;
import com.parismolapo.bookingreminder.exception.NotFoundException;
import com.parismolapo.bookingreminder.messaging.EmailSender;
import com.parismolapo.bookingreminder.messaging.MessageSender;
import com.parismolapo.bookingreminder.repository.PasswordResetOtpRepository;
import com.parismolapo.bookingreminder.repository.UserRepository;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int OTP_VALID_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final String GENERIC_INVALID = "Invalid or expired code";

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final MessageSender messageSender;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public Response<List<OtpChannelOptionDto>> getChannels(ForgotPasswordRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "No account found with that email address"));

        List<OtpChannelOptionDto> options = new ArrayList<>();

        options.add(OtpChannelOptionDto.builder()
                .channel(OtpChannel.EMAIL.name())
                .maskedDestination(maskEmail(user.getEmail()))
                .build());

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            options.add(OtpChannelOptionDto.builder()
                    .channel(OtpChannel.WHATSAPP.name())
                    .maskedDestination(maskPhone(user.getPhoneNumber()))
                    .build());
        }

        return Response.success("Choose where to send your code", options);
    }

    @Override
    @Transactional
    public Response<String> sendOtp(SendOtpRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "No account found with that email address"));

        OtpChannel channel = parseChannel(dto.getChannel());

        if (channel == OtpChannel.WHATSAPP
                && (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank())) {
            throw new BadRequestException("No phone number on file for this account");
        }

        // any previous unused code becomes invalid
        otpRepository.invalidateAllForEmail(email);

        String code = generateOtp();

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(code))
                .channel(channel)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES))
                .used(false)
                .attempts(0)
                .build();

        otpRepository.save(otp);

        deliver(user, channel, code);

        log.info("Password reset code sent to {} via {}", email, channel);

        return Response.success(
                "A 6 digit code has been sent. It expires in "
                        + OTP_VALID_MINUTES + " minutes.",
                null);
    }

    @Override
    @Transactional
    public Response<String> resetPassword(ResetPasswordRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(GENERIC_INVALID));

        PasswordResetOtp otp = otpRepository
                .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(GENERIC_INVALID));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setUsed(true);
            otpRepository.save(otp);
            throw new BadRequestException(GENERIC_INVALID);
        }

        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpRepository.save(otp);
            log.warn("Too many reset attempts for {}", email);
            throw new BadRequestException(
                    "Too many incorrect attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(dto.getOtp(), otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new BadRequestException(GENERIC_INVALID);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);

        notifyPasswordChanged(user);

        log.info("Password reset completed for {}", email);

        return Response.success(
                "Your password has been reset. You can now log in.", null);
    }

    // ---------- helpers ----------

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private OtpChannel parseChannel(String channel) {
        try {
            return OtpChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Choose either EMAIL or WHATSAPP");
        }
    }

    private void deliver(User user, OtpChannel channel, String code) {

        String message = String.format(
                "Your password reset code is %s. It expires in %d minutes. "
                        + "If you did not request this, ignore this message.",
                code, OTP_VALID_MINUTES);

        if (channel == OtpChannel.EMAIL) {
            emailSender.send(user.getEmail(), "Your password reset code", message);
        } else {
            messageSender.send(user.getPhoneNumber(), message);
        }
    }

    private void notifyPasswordChanged(User user) {

        String warning = "Your password was just changed. "
                + "If this was not you, contact support immediately.";

        emailSender.send(user.getEmail(), "Your password was changed", warning);
    }

    private String maskEmail(String email) {

        int at = email.indexOf('@');

        if (at <= 0) {
            return "****";
        }

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        String maskedLocal = local.length() <= 2
                ? local.charAt(0) + "*"
                : local.charAt(0) + "*".repeat(local.length() - 2)
                  + local.charAt(local.length() - 1);

        int dot = domain.indexOf('.');
        String maskedDomain = (dot <= 0)
                ? "*".repeat(domain.length())
                : domain.charAt(0) + "*".repeat(Math.max(dot - 1, 1))
                  + domain.substring(dot);

        return maskedLocal + "@" + maskedDomain;
    }

    private String maskPhone(String phone) {

        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.length() < 2) {
            return "Number ending **";
        }

        return "Number ending **" + digits.substring(digits.length() - 2);
    }

    private Optional<User> silent(String email) {
        return userRepository.findByEmail(email);
    }
}