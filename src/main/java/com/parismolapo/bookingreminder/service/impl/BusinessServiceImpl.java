package com.parismolapo.bookingreminder.service.impl;

import com.parismolapo.bookingreminder.dto.BusinessDto;
import com.parismolapo.bookingreminder.entity.Business;
import com.parismolapo.bookingreminder.exception.BadRequestException;
import com.parismolapo.bookingreminder.exception.NotFoundException;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    @Override
    @Transactional
    public Response<BusinessDto> createBusiness(BusinessDto dto) {

        if (businessRepository.existsByWhatsappNumber(dto.getWhatsappNumber())) {
            throw new BadRequestException(
                    "A business is already registered with that WhatsApp number");
        }

        Business business = Business.builder()
                .name(dto.getName().trim())
                .whatsappNumber(dto.getWhatsappNumber().trim())
                .ownerName(dto.getOwnerName().trim())
                .ownerPhoneNumber(dto.getOwnerPhoneNumber().trim())
                .active(dto.getActive() == null || dto.getActive())
                .build();

        Business saved = businessRepository.save(business);

        return Response.success("Business created", mapToDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<BusinessDto>> getAllBusinesses() {

        List<BusinessDto> businesses = businessRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();

        return Response.success("Businesses retrieved", businesses);
    }

    @Override
    @Transactional(readOnly = true)
    public Response<BusinessDto> getBusinessById(Long id) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found with id " + id));

        return Response.success("Business retrieved", mapToDto(business));
    }

    @Override
    @Transactional
    public Response<BusinessDto> updateBusiness(Long id, BusinessDto dto) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found with id " + id));

        String newNumber = dto.getWhatsappNumber().trim();

        boolean numberChanged = !business.getWhatsappNumber().equals(newNumber);

        if (numberChanged && businessRepository.existsByWhatsappNumber(newNumber)) {
            throw new BadRequestException(
                    "A business is already registered with that WhatsApp number");
        }

        business.setName(dto.getName().trim());
        business.setWhatsappNumber(newNumber);
        business.setOwnerName(dto.getOwnerName().trim());
        business.setOwnerPhoneNumber(dto.getOwnerPhoneNumber().trim());

        if (dto.getActive() != null) {
            business.setActive(dto.getActive());
        }

        Business saved = businessRepository.save(business);

        return Response.success("Business updated", mapToDto(saved));
    }

    @Override
    @Transactional
    public Response<BusinessDto> setActive(Long id, boolean active) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found with id " + id));

        business.setActive(active);
        Business saved = businessRepository.save(business);

        return Response.success(
                active ? "Business activated" : "Business deactivated",
                mapToDto(saved));
    }

    // ---------- helpers ----------

    private BusinessDto mapToDto(Business business) {
        return BusinessDto.builder()
                .id(business.getId())
                .name(business.getName())
                .whatsappNumber(business.getWhatsappNumber())
                .ownerName(business.getOwnerName())
                .ownerPhoneNumber(business.getOwnerPhoneNumber())
                .active(business.isActive())
                .createdAt(business.getCreatedAt())
                .build();
    }
}