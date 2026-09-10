package com.clinicadmin.dto;

import lombok.Data;

@Data
public class DiagnosisCenterDTO {

    private String vendorId;

    private String vendorName;

    private long totalPatients;

    private long testedPatients;
    
    private long notTestedPatients;
    
    private long pendingPatients;
    
    private double yield;

}