package com.smartasset.document.service;

import com.smartasset.document.domain.Document;
import com.smartasset.document.repository.DocumentRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    public List<Document> findByReferenceId(UUID referenceId) {
        return documentRepository.findByReferenceId(referenceId);
    }

    public Document create(Document document) {
        Document saved = documentRepository.save(document);
        log.info("Created document: {} for reference {}", saved.getId(), saved.getReferenceId());
        return saved;
    }

    public void delete(UUID id) {
        Document document = findById(id);
        documentRepository.delete(document);
        log.info("Deleted document: {}", id);
    }
}
