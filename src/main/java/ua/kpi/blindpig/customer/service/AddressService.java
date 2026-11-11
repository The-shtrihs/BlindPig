package ua.kpi.blindpig.customer.service;

import ua.kpi.blindpig.customer.dto.*;
import java.util.List;

public interface AddressService {
    AddressDto create(Long customerId, SaveAddressRequest request);
    /** Both customerId and addressId must match; otherwise 404. */
    AddressDto getById(Long customerId, Long addressId);
    List<AddressDto> listForCustomer(Long customerId);
    AddressDto update(Long customerId, Long addressId, SaveAddressRequest request);
    void delete(Long customerId, Long addressId);
}
