package com.clinicadmin.dto;


import lombok.Data;

@Data
public class TherapistLoginResponseDTO {

    private String therapistId;
    private String clinicId;
    private String branchId;
    private String therapistName;
    private String physioType;
}