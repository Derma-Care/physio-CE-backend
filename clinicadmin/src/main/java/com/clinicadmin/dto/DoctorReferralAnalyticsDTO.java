package com.clinicadmin.dto;

import lombok.Data;

@Data
public class DoctorReferralAnalyticsDTO {

    private String referralId;
//    private String doctorId;
    private String doctorName;
    private String clinicHospitalName;
    private String specialization;
    private String contactInfo;
    private Integer patientsReferred;
    private Double revenueGenerated;
}