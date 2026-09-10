package com.clinicadmin.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ReferralPatientSummaryDTO {

    private Integer selfPatientCount = 0;
    private Double selfRevenue = 0.0;

    private Integer otherPatientCount = 0;
    private Double otherRevenue = 0.0;

    private List<ReferringDoctorSummaryDTO> doctorWise = new ArrayList<>();
}