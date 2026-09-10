package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight referring-doctor row for dashboard/table views —
 * same totals as ReferringDoctorSummaryDTO, without the patient list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferringDoctorLiteDTO {

    private String doctorRefCode;
    private String referredByName;
    private Integer patientCount = 0;
    private Double revenue = 0.0;
}