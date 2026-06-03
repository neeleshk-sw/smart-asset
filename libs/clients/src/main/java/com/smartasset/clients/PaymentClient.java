package com.smartasset.clients;

import com.smartasset.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "payment-service", url = "${services.payment.url:http://localhost:8085}")
public interface PaymentClient {

    @PostMapping("/api/v1/payments/generate-schedule")
    ApiResponse<List<Map<String, Object>>> generateSchedule(
            @RequestParam UUID contractId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam int termMonths,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate);

    @GetMapping("/api/v1/payments/contract/{contractId}")
    ApiResponse<List<Map<String, Object>>> getPaymentsByContract(@PathVariable UUID contractId);
}
