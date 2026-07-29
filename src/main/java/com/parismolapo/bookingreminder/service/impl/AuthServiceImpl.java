package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.config.RoleInitializer;
import com.parismolapo.bookingreminder.dto.AuthResponseDto;
import com.parismolapo.bookingreminder.dto.LoginRequestDto;
import com.parismolapo.bookingreminder.dto.RegisterRequestDto;
import com.parismolapo.bookingreminder.entity.Business;
import com.parismolapo.bookingreminder.entity.Role;
import com.parismolapo.bookingreminder.entity.User;
import com.parismolapo.bookingreminder.exception.BadRequestException;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.repository.RoleRepository;
import com.parismolapo.bookingreminder.repository.UserRepository;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.security.JwtUtils;
import com.parismolapo.bookingreminder.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_LOGIN_ERROR = "Invalid credentials";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public Response<String> register(RegisterRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with that email already exists");
        }

        String businessNumber = dto.getBusinessWhatsappNumber().trim();

        if (businessRepository.existsByWhatsappNumber(businessNumber)) {
            throw new BadRequestException(
                    "A business is already registered with that WhatsApp number");
        }

        Role ownerRole = roleRepository.findByName(RoleInitializer.ROLE_OWNER)
                .orElseThrow(() -> new IllegalStateException(
                        "OWNER role is missing. Check RoleInitializer."));

        Business business = businessRepository.save(Business.builder()
                .name(dto.getBusinessName().trim())
                .whatsappNumber(businessNumber)
                .ownerName(dto.getName().trim())
                .ownerPhoneNumber(dto.getPhoneNumber().trim())
                .active(true)
                .build());

        User user = User.builder()
                .name(dto.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber().trim())
                .active(true)
                .business(business)
                .roles(List.of(ownerRole))
                .build();

        userRepository.save(user);

        log.info("Registered new owner {} for business {}", email, business.getName());

        return Response.success("Registration successful. You can now log in.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<AuthResponseDto> login(LoginRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(GENERIC_LOGIN_ERROR));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException(GENERIC_LOGIN_ERROR);
        }

        if (!user.isActive()) {
            throw new BadRequestException(
                    "This account is inactive. Please contact support.");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .businessId(user.getBusiness() == null ? null : user.getBusiness().getId())
                .businessName(user.getBusiness() == null ? null : user.getBusiness().getName())
                .build();

        return Response.success("Login successful", authResponse);
    }
}