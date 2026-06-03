package com.smartasset.payment.controller;

import com.smartasset.payment.domain.Payment;
import com.smartasset.payment.service.PaymentService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findById(id)));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findByContractId(contractId)));
    }

    @PostMapping("/generate-schedule")
    public ResponseEntity<ApiResponse<List<Payment>>> generateSchedule(
            @RequestParam UUID contractId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam int termMonths,
            @RequestParam LocalDate startDate) {
        List<Payment> schedule = paymentService.generateSchedule(contractId, totalAmount, termMonths, startDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment schedule generated", schedule));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Payment>> markAsPaid(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Payment marked as paid", paymentService.markAsPaid(id)));
    }

    @PostMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<Payment>> markAsOverdue(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Payment marked as overdue", paymentService.markAsOverdue(id)));
    }
}
