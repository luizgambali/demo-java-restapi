package com.example.demo.customers.service;

import com.example.demo.customers.dto.CreateCustomerRequest;
import com.example.demo.customers.dto.CustomerResponse;
import com.example.demo.customers.dto.DeleteCustomerRequest;
import com.example.demo.customers.dto.UpdateCustomerRequest;
import com.example.demo.customers.model.Customer;
import com.example.demo.customers.repository.CustomerRepository;
import com.example.demo.customers.validator.CustomerValidator;
import com.example.demo.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerValidator validator;

    @InjectMocks
    private CustomerService service;

    @Test
    void createsAndValidatesCustomerBeforeSaving() {
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = service.add(createRequest("Maria Silva", "maria@example.com"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(validator).validate(captor.capture());
        verify(repository).save(captor.getValue());
        assertEquals("Maria Silva", response.name());
        assertEquals("maria@example.com", response.email());
        assertEquals(true, response.active());
    }

    @Test
    void returnsCustomerWhenFound() {
        UUID id = UUID.randomUUID();
        Customer customer = customer(id, "Maria Silva", "maria@example.com");
        when(repository.findById(id)).thenReturn(Optional.of(customer));

        CustomerResponse response = service.findById(id);

        assertEquals(id, response.id());
        assertEquals("Maria Silva", response.name());
    }

    @Test
    void rejectsLookupOfMissingCustomer() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(id));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void updatesExistingCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = customer(id, "Nome antigo", "antigo@example.com");
        when(repository.findById(id)).thenReturn(Optional.of(customer));
        when(repository.save(customer)).thenReturn(customer);

        CustomerResponse response = service.update(id, updateRequest("Nome novo", "novo@example.com"));

        verify(validator).validate(customer);
        verify(repository).save(customer);
        assertEquals("Nome novo", response.name());
        assertEquals("novo@example.com", response.email());
    }

    @Test
    void deletesExistingCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = customer(id, "Maria Silva", "maria@example.com");
        when(repository.findById(id)).thenReturn(Optional.of(customer));

        service.delete(new DeleteCustomerRequest(id));

        verify(repository).delete(customer);
    }

    @Test
    void mapsAllCustomersToResponses() {
        Customer customer = customer(UUID.randomUUID(), "Maria Silva", "maria@example.com");
        when(repository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> responses = service.findAll();

        assertEquals(1, responses.size());
        assertEquals(customer.getId(), responses.getFirst().id());
    }

    private CreateCustomerRequest createRequest(String name, String email) {
        return new CreateCustomerRequest(name, "Rua A, 10", "São Paulo", "Brasil", "01000-000",
                "11999999999", email, true);
    }

    private UpdateCustomerRequest updateRequest(String name, String email) {
        return new UpdateCustomerRequest(name, "Rua A, 10", "São Paulo", "Brasil", "01000-000",
                "11999999999", email, true);
    }

    private Customer customer(UUID id, String name, String email) {
        return new Customer(id, name, "Rua A, 10", "São Paulo", "Brasil", "01000-000",
                "11999999999", email, LocalDateTime.of(2026, 1, 1, 10, 0), true);
    }
}
