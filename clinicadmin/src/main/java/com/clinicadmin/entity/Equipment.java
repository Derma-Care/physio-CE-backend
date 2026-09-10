
package com.clinicadmin.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "equipment")
public class Equipment {

    @Id
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


