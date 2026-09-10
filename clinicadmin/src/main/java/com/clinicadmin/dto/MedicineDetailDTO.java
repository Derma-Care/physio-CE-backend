package com.clinicadmin.dto;

import lombok.Data;

@Data
public class MedicineDetailDTO {

    private String id;

    private String productName;
    private String batchNo;
    private String expDate;

    private int quantity;
    private int packSize;
    private int free;

    private double gstPercent;
    private double cgstPercent;
    private double sgstPercent;
    private double costPrice;
    private double mrp;
    private double discPercent;
}

