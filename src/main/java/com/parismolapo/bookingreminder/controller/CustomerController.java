package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.CustomerDto;
import com.parismolapo.bookingreminder.response.Response;
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

    @GetMapping
    public ResponseEntity<Response<List<CustomerDto>>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Response<List<CustomerDto>>> getForBusiness(
            @PathVariable Long businessId) {
        return ResponseEntity.ok(customerService.getCustomersForBusiness(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CustomerDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PatchMapping("/{id}/opt-out")
    public ResponseEntity<Response<CustomerDto>> setOptedOut(
            @PathVariable Long id,
            @RequestParam boolean optedOut) {
        return ResponseEntity.ok(customerService.setOptedOut(id, optedOut));
    }
}