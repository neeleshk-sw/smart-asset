package com.smartasset.customer.controller;

import com.smartasset.customer.domain.Customer;
import com.smartasset.customer.service.CustomerService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findByEmail(email)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        Customer created = customerService.create(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable UUID id, @RequestBody Customer customer) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.update(id, customer)));
    }

    @PostMapping("/{id}/verify-kyc")
    public ResponseEntity<ApiResponse<Customer>> verifyKyc(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("KYC verified", customerService.verifyKyc(id)));
    }

    @PostMapping("/{id}/reject-kyc")
    public ResponseEntity<ApiResponse<Customer>> rejectKyc(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("KYC rejected", customerService.rejectKyc(id)));
    }
}
