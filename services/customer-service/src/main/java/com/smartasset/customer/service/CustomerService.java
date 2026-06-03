package com.smartasset.customer.service;

import com.smartasset.customer.domain.Customer;
import com.smartasset.common.events.CustomerEvent;
import com.smartasset.customer.repository.CustomerRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import com.smartasset.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private static final String CUSTOMER_TOPIC = "customer-events";

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, CustomerEvent> kafkaTemplate;

    public CustomerService(CustomerRepository customerRepository, KafkaTemplate<String, CustomerEvent> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new BadRequestException("Customer with email " + customer.getEmail() + " already exists");
        }
        Customer saved = customerRepository.save(customer);
        log.info("Created customer: {}", saved.getId());
        kafkaTemplate.send(CUSTOMER_TOPIC, saved.getId().toString(), CustomerEvent.created(saved.getId()));
        return saved;
    }

    @Transactional
    public Customer update(UUID id, Customer customerDetails) {
        Customer customer = findById(id);
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer verifyKyc(UUID id) {
        Customer customer = findById(id);
        customer.setKycStatus(Customer.KycStatus.VERIFIED);
        Customer saved = customerRepository.save(customer);
        log.info("Customer {} KYC verified", id);
        kafkaTemplate.send(CUSTOMER_TOPIC, id.toString(), CustomerEvent.verified(id));
        return saved;
    }

    @Transactional
    public Customer rejectKyc(UUID id) {
        Customer customer = findById(id);
        customer.setKycStatus(Customer.KycStatus.REJECTED);
        return customerRepository.save(customer);
    }
}
