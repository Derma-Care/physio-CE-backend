package com.clinicadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PurchaseItemDTO {

    @NotBlank(message = "Product name cannot be empty")
    private String productName;

    @NotBlank(message = "Batch No cannot be empty")
    private String batchNo;

    @NotBlank(message = "Expiry date required")
    private String expiryDate;

    @NotBlank(message = "Pack size required")
    private String packSize;

    @Min(value = 1, message = "Quantity must be >= 1")
    private int quantity;

    @Min(value = 0, message = "Cost price must be >= 0")
    private double costPrice;

    @Min(value = 0, message = "MRP must be >= 0")
    private double mrp;

    @Min(value = 0, message = "Discount % must be >= 0")
    private double discountPercent;

    @Min(value = 0, message = "GST % must be >= 0")
    private double gstPercent;
}
