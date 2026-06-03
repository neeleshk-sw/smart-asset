package com.smartasset.orchestrator.controller;

import com.smartasset.common.dto.ApiResponse;
import com.smartasset.orchestrator.domain.SagaInstance;
import com.smartasset.orchestrator.workflow.LeasingRequest;
import com.smartasset.orchestrator.workflow.LeasingSagaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final LeasingSagaService leasingSagaService;

    public WorkflowController(LeasingSagaService leasingSagaService) {
        this.leasingSagaService = leasingSagaService;
    }

    @PostMapping("/leasing")
    public ResponseEntity<ApiResponse<SagaInstance>> startLeasingWorkflow(@RequestBody LeasingRequest request) {
        SagaInstance saga = leasingSagaService.executeLeasingWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leasing workflow started", saga));
    }
}
