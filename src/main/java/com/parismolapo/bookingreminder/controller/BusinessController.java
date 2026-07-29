package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.BusinessDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<Response<BusinessDto>> create(
            @RequestBody @Valid BusinessDto dto) {
        return ResponseEntity.ok(businessService.createBusiness(dto));
    }

    @GetMapping
    public ResponseEntity<Response<List<BusinessDto>>> getAll() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<BusinessDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<BusinessDto>> update(
            @PathVariable Long id,
            @RequestBody @Valid BusinessDto dto) {
        return ResponseEntity.ok(businessService.updateBusiness(id, dto));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Response<BusinessDto>> setActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(businessService.setActive(id, active));
    }
}