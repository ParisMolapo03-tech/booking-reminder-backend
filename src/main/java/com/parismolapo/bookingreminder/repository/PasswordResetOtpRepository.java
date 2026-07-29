package com.parismolapo.bookingreminder.repository;

import com.parismolapo.bookingreminder.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("update PasswordResetOtp o set o.used = true "
            + "where o.email = :email and o.used = false")
    void invalidateAllForEmail(@Param("email") String email);

    @Modifying
    @Query("delete from PasswordResetOtp o where o.expiresAt < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}