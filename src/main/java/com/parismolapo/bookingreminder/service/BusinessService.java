package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.BusinessDto;
import com.parismolapo.bookingreminder.response.Response;

import java.util.List;

public interface BusinessService {

    Response<BusinessDto> createBusiness(BusinessDto dto);

    Response<List<BusinessDto>> getAllBusinesses();

    Response<BusinessDto> getBusinessById(Long id);

    Response<BusinessDto> updateBusiness(Long id, BusinessDto dto);

    Response<BusinessDto> setActive(Long id, boolean active);
}