package com.parismolapo.bookingreminder.config;

import com.parismolapo.bookingreminder.entity.Role;
import com.parismolapo.bookingreminder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        List.of(ROLE_OWNER, ROLE_ADMIN).forEach(name -> {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(Role.builder().name(name).build());
                log.info("Created role {}", name);
            }
        });
    }
}