package com.smartasset.customer.service;

import com.smartasset.customer.domain.Customer;
import com.smartasset.common.events.CustomerEvent;
import com.smartasset.customer.repository.CustomerRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import com.smartasset.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KafkaTemplate<String, CustomerEvent> kafkaTemplate;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, kafkaTemplate);
    }

    @Test
    void findAll_ShouldReturnList() {
        Customer c1 = new Customer();
        Customer c2 = new Customer();
        when(customerRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Customer> result = customerService.findAll();

        assertThat(result).hasSize(2);
        verify(customerRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_WhenDoesNotExist_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ShouldSaveCustomerAndSendKafkaEvent() {
        Customer customer = new Customer();
        UUID id = UUID.randomUUID();
        customer.setId(id);
        customer.setEmail("test@example.com");

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.create(customer);

        assertThat(result).isEqualTo(customer);
        verify(customerRepository).save(customer);

        ArgumentCaptor<CustomerEvent> eventCaptor = ArgumentCaptor.forClass(CustomerEvent.class);
        verify(kafkaTemplate).send(eq("customer-events"), eq(id.toString()), eventCaptor.capture());

        CustomerEvent event = eventCaptor.getValue();
        assertThat(event.getCustomerId()).isEqualTo(id);
        assertThat(event.getEventType()).isEqualTo("CUSTOMER_CREATED");
        assertThat(event.getKycStatus()).isEqualTo("PENDING");
    }

    @Test
    void create_WhenEmailExists_ShouldThrowException() {
        Customer customer = new Customer();
        customer.setEmail("existing@example.com");
        when(customerRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new Customer()));

        assertThatThrownBy(() -> customerService.create(customer))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void verifyKyc_ShouldUpdateStatusAndSendKafkaEvent() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(id);
        customer.setKycStatus(Customer.KycStatus.PENDING);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.verifyKyc(id);

        assertThat(result.getKycStatus()).isEqualTo(Customer.KycStatus.VERIFIED);
        verify(customerRepository).save(customer);

        ArgumentCaptor<CustomerEvent> eventCaptor = ArgumentCaptor.forClass(CustomerEvent.class);
        verify(kafkaTemplate).send(eq("customer-events"), eq(id.toString()), eventCaptor.capture());

        CustomerEvent event = eventCaptor.getValue();
        assertThat(event.getCustomerId()).isEqualTo(id);
        assertThat(event.getEventType()).isEqualTo("CUSTOMER_VERIFIED");
        assertThat(event.getKycStatus()).isEqualTo("VERIFIED");
    }
}
