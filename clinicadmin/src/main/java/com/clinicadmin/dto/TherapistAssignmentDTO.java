package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class TherapistAssignmentDTO {

    private String id;

    private String clinicId;
    private String branchId;

    private String therapistRecordId;

    private String assignTherapistId;
    private String assignTherapistName;

    private String assignedTherapistId;
    private String assignedTherapistName;
    private List<String> services;

    private String assignedStatus;
}