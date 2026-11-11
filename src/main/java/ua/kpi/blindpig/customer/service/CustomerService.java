package ua.kpi.blindpig.customer.service;

import ua.kpi.blindpig.customer.dto.*;
import java.util.List;

public interface CustomerService {
    /** Unique email; duplicate -> BusinessConflictException. */
    CustomerDto create(SaveCustomerRequest request);
    CustomerDto getById(Long customerId);
    List<CustomerDto> list();
    List<CustomerDto> search(String nameOrEmail);
    CustomerDto update(Long customerId, SaveCustomerRequest request);
    void delete(Long customerId);
}
