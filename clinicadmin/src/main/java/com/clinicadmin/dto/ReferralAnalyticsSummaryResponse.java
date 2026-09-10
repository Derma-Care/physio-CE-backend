package com.clinicadmin.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ReferralAnalyticsSummaryResponse {

    private Integer totalReferrals = 0;       // selfPatientCount + otherPatientCount
    private Integer selfPatientCount = 0;
    private Double selfRevenue = 0.0;

    private Integer otherPatientCount = 0;
    private Double otherRevenue = 0.0;

    private String topReferringDoctorName;
    private Integer topReferringDoctorPatientCount = 0;

    private List<ReferringDoctorLiteDTO> doctorWise = new ArrayList<>();
}