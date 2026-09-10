package com.clinicadmin.dto;

import lombok.Data;

@Data
public class DiagnosisDashboardDTO {

    private long totalPatients;
    private long totalDiagnosisCenters;

    private long tested;
    private long pending;
    private long notTested;
}