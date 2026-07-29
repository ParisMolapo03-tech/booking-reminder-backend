package com.parismolapo.bookingreminder.repository;

import com.parismolapo.bookingreminder.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("select distinct b.customer from Booking b where b.business.id = :businessId")
    List<Customer> findAllByBusinessId(@Param("businessId") Long businessId);
}