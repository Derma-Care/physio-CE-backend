package com.clinicadmin.dto;

import lombok.Data;

@Data
public class DoctorPatientFeedbackDTO {
    private String patientId;
    private String patientName;
    private String mobileNumber;
    private String rating;
    private String whatWentWell;
}