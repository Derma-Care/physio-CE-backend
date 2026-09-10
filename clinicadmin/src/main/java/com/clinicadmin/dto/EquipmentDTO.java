package com.clinicadmin.dto;

import java.time.LocalDate;

import com.clinicadmin.entity.VendorDetails;

import lombok.Data;

@Data
public class EquipmentDTO {

    private String equipmentId;
    private String clinicId;
    private String branchId;
    private String name;
    private String category;
    private String type;
    private String brand;
    private String model;
    private String serialNo;
    private String status;
    private String department;

    private LocalDate purchaseDate;
    private LocalDate warrantyExpiry;
    private LocalDate amcStartDate;
    private LocalDate amcEndDate;

    private Double purchaseCost;
    private Double currentValue;

    private LocalDate nextServiceDate;
    private LocalDate lastServiceDate;

    private String assignedStaff;
    private String imageUrl;
    private String notes;

    private VendorDetails vendorDetails;
}