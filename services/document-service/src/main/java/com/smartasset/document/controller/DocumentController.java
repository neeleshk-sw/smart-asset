package com.smartasset.document.controller;

import com.smartasset.document.domain.Document;
import com.smartasset.document.service.DocumentService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Document>>> getAllDocuments() {
        return ResponseEntity.ok(ApiResponse.success(documentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocumentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.findById(id)));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<List<Document>>> getDocumentsByReference(@PathVariable UUID referenceId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.findByReferenceId(referenceId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Document>> createDocument(@RequestBody Document document) {
        Document created = documentService.create(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document created", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
