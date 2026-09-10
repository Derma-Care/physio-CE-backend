package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class VendorTestResponseDTO {

    private String vendorId;
    private String vendorName;

 

    private long totalAssigned;
    private long tested;
    private long pending;
    private long notTested;
    private double yield;


  

    private List<VendorTestDTO> tests;
    
    private List<DiagnosisPatientDTO> patients;
}