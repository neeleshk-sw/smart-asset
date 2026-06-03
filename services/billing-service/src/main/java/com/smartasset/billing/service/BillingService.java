package com.smartasset.billing.service;

import com.smartasset.billing.domain.Invoice;
import com.smartasset.billing.repository.InvoiceRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> findAll() { return invoiceRepository.findAll(); }

    public Invoice findById(UUID id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    public List<Invoice> findByCustomerId(UUID customerId) { return invoiceRepository.findByCustomerId(customerId); }

    public Invoice create(Invoice invoice) {
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice {} for customer {}", saved.getId(), saved.getCustomerId());
        return saved;
    }

    public Invoice markAsPaid(UUID id) {
        Invoice invoice = findById(id);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        return invoiceRepository.save(invoice);
    }
}
