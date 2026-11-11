package ua.kpi.blindpig.customer.dto;

public record AddressDto(Long id, Long customerId, String label, String city, String street, String building, String apartment) {}
