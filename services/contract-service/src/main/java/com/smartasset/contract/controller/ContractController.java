package com.smartasset.contract.controller;

import com.smartasset.contract.domain.Contract;
import com.smartasset.contract.service.ContractService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Contract>>> getAllContracts() {
        return ResponseEntity.ok(ApiResponse.success(contractService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Contract>> getContractById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Contract>>> getContractsByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(contractService.findByCustomerId(customerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Contract>> createContract(@RequestBody Contract contract) {
        Contract created = contractService.create(contract);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Contract created", created));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Contract>> submitForApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Contract submitted for approval", contractService.submitForApproval(id)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Contract>> approveContract(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Contract approved", contractService.approve(id)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Contract>> activateContract(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Contract activated", contractService.activate(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Contract>> cancelContract(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Contract cancelled", contractService.cancel(id)));
    }
}
