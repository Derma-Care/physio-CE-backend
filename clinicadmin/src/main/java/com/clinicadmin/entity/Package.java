package com.clinicadmin.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "packages")
public class Package {

    @Id
    private String id;

    private String packageName;
    private String clinicId;
    private String branchId;

    private double packagePrice;
    private double discount;
    private double gst;
    private double otherTaxes;

    private String paymentType;

    private String offerStartDate;
    private String offerEndDate;

    private String description;

    // ✅ Calculated Fields
    private double discountAmount;
    private double afterDiscountPrice;

    private double gstAmount;
    private double otherTaxAmount;

    private double finalPrice;

    private List<Therapy> therapies;

    private String createdBy;
    private LocalDateTime createdAt;


    private String updatedBy;
    private LocalDateTime updatedAt;
}