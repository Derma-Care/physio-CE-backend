package com.clinicadmin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.clinicadmin.entity.Therapy;

import lombok.Data;

@Data
public class PackageDTO {

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
