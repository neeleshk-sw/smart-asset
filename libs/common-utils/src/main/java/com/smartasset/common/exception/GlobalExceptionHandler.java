package com.smartasset.common.exception;

import com.smartasset.common.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(SmartAssetException.class)
    public ResponseEntity<ApiResponse<Void>> handleSmartAssetException(SmartAssetException ex) {
        log.error("SmartAssetException: {}", ex.getMessage());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof BadRequestException) {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage(), ex.getErrorCode(), getTraceId()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(ApiResponse.error("Validation Failed", "VALIDATION_ERROR", getTraceId()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unhandled Exception: ", ex);
        return new ResponseEntity<>(
                ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR", getTraceId()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        log.error("Access Denied: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error("Access Denied", "FORBIDDEN", getTraceId()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(
            jakarta.persistence.EntityNotFoundException ex) {
        log.error("Entity Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage(), "NOT_FOUND", getTraceId()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return new ResponseEntity<>(ApiResponse.error("Constraint Violation", "BAD_REQUEST", getTraceId()),
                HttpStatus.BAD_REQUEST);
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return null;
    }
}
