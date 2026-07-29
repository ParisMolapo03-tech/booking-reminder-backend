package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.CustomerDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.security.OwnershipGuard;
import com.parismolapo.bookingreminder.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final OwnershipGuard ownershipGuard;

    @GetMapping
    public ResponseEntity<Response<List<CustomerDto>>> getAll() {
        // admin only - enforced in SecurityConfig
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Response<List<CustomerDto>>> getForBusiness(
            @PathVariable Long businessId) {

        ownershipGuard.checkBusinessAccess(businessId);

        return ResponseEntity.ok(customerService.getCustomersForBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CustomerDto>> getById(@PathVariable Long id) {

        Response<CustomerDto> customer = customerService.getCustomerById(id);

        if (!ownershipGuard.isAdmin()) {
            Long callerBusinessId = ownershipGuard.currentBusinessId();

            boolean hasBookingWithCaller = customer.getData().getBookings() != null
                    && customer.getData().getBookings().stream()
                    .anyMatch(b -> b.getBusinessId().equals(callerBusinessId));

            if (!hasBookingWithCaller) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "This customer is not one of your clients");
            }
        }

        return ResponseEntity.ok(customer);
    }

    @PatchMapping("/{id}/opt-out")
    public ResponseEntity<Response<CustomerDto>> setOptedOut(
            @PathVariable Long id,
            @RequestParam boolean optedOut) {

        Response<CustomerDto> customer = customerService.getCustomerById(id);

        if (!ownershipGuard.isAdmin()) {
            Long callerBusinessId = ownershipGuard.currentBusinessId();

            boolean hasBookingWithCaller = customer.getData().getBookings() != null
                    && customer.getData().getBookings().stream()
                    .anyMatch(b -> b.getBusinessId().equals(callerBusinessId));

            if (!hasBookingWithCaller) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "This customer is not one of your clients");
            }
        }

        return ResponseEntity.ok(customerService.setOptedOut(id, optedOut));
    }
}