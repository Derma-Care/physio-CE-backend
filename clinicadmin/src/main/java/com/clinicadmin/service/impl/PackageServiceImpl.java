package com.clinicadmin.service.impl;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PackageDTO;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.Package;
import com.clinicadmin.repository.PackageRepository;
import com.clinicadmin.service.PackageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Autowired
    private PackageRepository repository;

    // ================= CREATE =================
    @Override
    public ResponseStructure<PackageDTO> createPackage(PackageDTO dto) {

        log.info("Creating package for clinicId: {}, branchId: {}", dto.getClinicId(), dto.getBranchId());

        validatePackage(dto);

        Package entity = mapToEntity(dto);

        calculatePrice(entity);

        entity.setCreatedAt(LocalDateTime.now());

        Package saved = repository.save(entity);

        return ResponseStructure.buildResponse(
                mapToDTO(saved),
                "Package Created Successfully",
                HttpStatus.CREATED,
                201
        );
    }

    // ================= GET BY ID =================
    @Override
    public ResponseStructure<PackageDTO> getPackageById(String id) {

        log.info("Fetching package id: {}", id);

        Package entity = getPackageOrThrow(id);

        return ResponseStructure.buildResponse(
                mapToDTO(entity),
                "Package fetched successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= GET ALL =================
    @Override
    public ResponseStructure<List<PackageDTO>> getAllPackages() {

        log.info("Fetching all packages");

        List<PackageDTO> list = repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseStructure.buildResponse(
                list,
                "All packages fetched",
                HttpStatus.OK,
                200
        );
    }

    // ================= GET BY CLINIC & BRANCH =================
    @Override
    public ResponseStructure<List<PackageDTO>> getByClinicAndBranch(String clinicId, String branchId) {

        log.info("Fetching packages for clinicId: {}, branchId: {}", clinicId, branchId);

        List<PackageDTO> list = repository.findByClinicIdAndBranchId(clinicId, branchId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseStructure.buildResponse(
                list,
                "Packages fetched successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= GET BY CLINIC + BRANCH + PACKAGE =================
    @Override
    public ResponseStructure<PackageDTO> getByClinicBranchAndPackageId(
            String clinicId, String branchId, String packageId) {

        log.info("Fetching package with clinicId: {}, branchId: {}, packageId: {}",
                clinicId, branchId, packageId);

        Package entity = getPackageOrThrow(packageId);

        if (!entity.getClinicId().equals(clinicId) ||
                !entity.getBranchId().equals(branchId)) {

            throw new RuntimeException("Package does not belong to given clinic/branch");
        }

        return ResponseStructure.buildResponse(
                mapToDTO(entity),
                "Package fetched successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= UPDATE =================
    @Override
    public ResponseStructure<PackageDTO> updatepackagebyid(String id, PackageDTO dto) {

        log.info("Updating package id: {}", id);

        Package entity = getPackageOrThrow(id);

        
        if (dto.getPackageName() != null) {
            entity.setPackageName(dto.getPackageName());
        }

        if (dto.getClinicId() != null) {
            entity.setClinicId(dto.getClinicId());
        }

        if (dto.getBranchId() != null) {
            entity.setBranchId(dto.getBranchId());
        }

        if (dto.getPackagePrice() > 0) {
            entity.setPackagePrice(dto.getPackagePrice());
        }

        if (dto.getDiscount() >= 0) {
            entity.setDiscount(dto.getDiscount());
        }

        if (dto.getGst() >= 0) {
            entity.setGst(dto.getGst());
        }

        if (dto.getOtherTaxes() >= 0) {
            entity.setOtherTaxes(dto.getOtherTaxes());
        }

        if (dto.getPaymentType() != null) {
            entity.setPaymentType(dto.getPaymentType());
        }

        if (dto.getOfferStartDate() != null) {
            entity.setOfferStartDate(dto.getOfferStartDate());
        }

        if (dto.getOfferEndDate() != null) {
            entity.setOfferEndDate(dto.getOfferEndDate());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        if (dto.getTherapies() != null && !dto.getTherapies().isEmpty()) {
            entity.setTherapies(dto.getTherapies());
        }

        if (dto.getUpdatedBy() != null) {
            entity.setUpdatedBy(dto.getUpdatedBy());
        }

        
        entity.setCreatedAt(LocalDateTime.now());

        calculatePrice(entity);

        Package updated = repository.save(entity);

        return ResponseStructure.buildResponse(
                mapToDTO(updated),
                "Package updated successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= DELETE =================
    @Override
    public ResponseStructure<String> deletepackagebyid(String id) {

        log.info("Deleting package id: {}", id);

        Package entity = getPackageOrThrow(id);

        repository.delete(entity);

        return ResponseStructure.buildResponse(
                "Deleted Successfully",
                "Package deleted",
                HttpStatus.OK,
                200
        );
    }

    // ================= VALIDATION =================
    private void validatePackage(PackageDTO dto) {

        if (dto.getPackagePrice() <= 0) {
            throw new RuntimeException("Package price must be greater than 0");
        }

        if (dto.getDiscount() < 0 || dto.getDiscount() > 100) {
            throw new RuntimeException("Invalid discount value");
        }

        if (dto.getGst() < 0 || dto.getOtherTaxes() < 0) {
            throw new RuntimeException("Tax values cannot be negative");
        }
    }

    // ================= COMMON FETCH =================
    private Package getPackageOrThrow(String id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
    }

    

    private void calculatePrice(Package entity) {

        double price = entity.getPackagePrice();

        // Discount
        double discountAmount = (price * entity.getDiscount()) / 100;
        double afterDiscount = price - discountAmount;

        // Taxes
        double gstAmount = (afterDiscount * entity.getGst()) / 100;
        double otherTaxAmount = (afterDiscount * entity.getOtherTaxes()) / 100;

        double finalPrice = afterDiscount + gstAmount + otherTaxAmount;

        // Set all values
        entity.setDiscountAmount(discountAmount);
        entity.setAfterDiscountPrice(afterDiscount);   // ⭐ NEW FIELD
        entity.setGstAmount(gstAmount);
        entity.setOtherTaxAmount(otherTaxAmount);
        entity.setFinalPrice(finalPrice);
    }

    // ================= MAPPER (NO BUILDER) =================
    private Package mapToEntity(PackageDTO dto) {

        Package entity = new Package();
        entity.setId(dto.getId());
        entity.setPackageName(dto.getPackageName());
        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());
        entity.setPackagePrice(dto.getPackagePrice());
        entity.setDiscount(dto.getDiscount());
        entity.setGst(dto.getGst());
        entity.setOtherTaxes(dto.getOtherTaxes());
        entity.setPaymentType(dto.getPaymentType());
        entity.setOfferStartDate(dto.getOfferStartDate());
        entity.setOfferEndDate(dto.getOfferEndDate());
        entity.setDescription(dto.getDescription());
        entity.setTherapies(dto.getTherapies());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }

    private PackageDTO mapToDTO(Package entity) {

        PackageDTO dto = new PackageDTO();

        dto.setId(entity.getId());
        dto.setPackageName(entity.getPackageName());
        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());
        dto.setPackagePrice(entity.getPackagePrice());
        dto.setDiscount(entity.getDiscount());
       
        dto.setGst(entity.getGst());
        dto.setOtherTaxes(entity.getOtherTaxes());
        dto.setPaymentType(entity.getPaymentType());
        dto.setOfferStartDate(entity.getOfferStartDate());
        dto.setOfferEndDate(entity.getOfferEndDate());
        dto.setDescription(entity.getDescription());

        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setAfterDiscountPrice(entity.getAfterDiscountPrice());
        dto.setGstAmount(entity.getGstAmount());
        dto.setOtherTaxAmount(entity.getOtherTaxAmount());
        dto.setFinalPrice(entity.getFinalPrice());

        dto.setTherapies(entity.getTherapies());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}