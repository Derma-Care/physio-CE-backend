package com.clinicadmin.dto;

import lombok.Data;

@Data
public class PatientRatingDTO {

    private String patientId;
    private String patientName;
    private String mobileNumber;

    private String rating;
    private String whatWentWell;
    private String improvements;
}