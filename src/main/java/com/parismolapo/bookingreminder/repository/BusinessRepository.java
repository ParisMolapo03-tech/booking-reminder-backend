package com.parismolapo.bookingreminder.repository;

import com.parismolapo.bookingreminder.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByWhatsappNumber(String whatsappNumber);

    boolean existsByWhatsappNumber(String whatsappNumber);

    List<Business> findByActiveTrue();
}