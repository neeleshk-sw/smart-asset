package com.smartasset.customer.config;

import com.smartasset.customer.domain.Customer;
import com.smartasset.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

@Configuration
@Profile("local")
public class LocalDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataLoader.class);

    private final CustomerRepository customerRepository;

    public LocalDataLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Customer database already seeded. Skipping local data loading.");
            return;
        }

        log.info("Seeding local customer data...");

        Customer customer1 = new Customer();
        customer1.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setEmail("john.doe@example.com");
        customer1.setPhone("+1234567890");
        customer1.setAddress("123 Main St, New York, NY 10001");
        customer1.setKycStatus(Customer.KycStatus.VERIFIED);

        Customer customer2 = new Customer();
        customer2.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setPhone("+1987654321");
        customer2.setAddress("456 Oak Ave, San Francisco, CA 94107");
        customer2.setKycStatus(Customer.KycStatus.PENDING);

        customerRepository.saveAll(List.of(customer1, customer2));

        log.info("Local customer data seeded successfully.");
    }
}
