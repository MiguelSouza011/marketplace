package io.miguelsouza.marketplace.ticketing.application;

import io.miguelsouza.marketplace.common.infrastructure.event.dto.CustomerCreated;
import io.miguelsouza.marketplace.ticketing.domain.Customer;
import io.miguelsouza.marketplace.ticketing.domain.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCustomerUseCase {
    private final CustomerRepository customerRepository;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void execute(CustomerCreated event) {
        var customer = new Customer(event.id(), event.name());
        customerRepository.save(customer);
    }
}
