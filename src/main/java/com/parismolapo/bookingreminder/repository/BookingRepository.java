package com.parismolapo.bookingreminder.repository;

import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBusinessId(Long businessId);

    List<Booking> findByReminderSentFalseAndStatusNotAndAppointmentTimeBetween(
            BookingStatus status,
            LocalDateTime start,
            LocalDateTime end);

    List<Booking> findByBusinessIdAndAppointmentTimeBetween(
            Long businessId,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByBusinessIdAndCustomerIdAndAppointmentTimeAndStatusNot(
            Long businessId,
            Long customerId,
            LocalDateTime appointmentTime,
            BookingStatus status);

    List<Booking> findByCustomerIdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(
            Long customerId,
            LocalDateTime after);

    List<Booking> findByCustomerIdOrderByAppointmentTimeDesc(Long customerId);

    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, BookingStatus status);
}