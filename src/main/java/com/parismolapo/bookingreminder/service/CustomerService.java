package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.CustomerDto;
import com.parismolapo.bookingreminder.response.Response;

import java.util.List;

public interface CustomerService {

    Response<List<CustomerDto>> getAllCustomers();

    Response<List<CustomerDto>> getCustomersForBusiness(Long businessId);

    Response<CustomerDto> getCustomerById(Long id);

    Response<CustomerDto> setOptedOut(Long id, boolean optedOut);
}