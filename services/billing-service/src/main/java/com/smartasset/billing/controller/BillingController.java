package com.smartasset.billing.controller;

import com.smartasset.billing.domain.Invoice;
import com.smartasset.billing.service.BillingService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Invoice>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(billingService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Invoice>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(billingService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Invoice>>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(billingService.findByCustomerId(customerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Invoice>> create(@RequestBody Invoice invoice) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Invoice created", billingService.create(invoice)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Invoice>> markAsPaid(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice paid", billingService.markAsPaid(id)));
    }
}
