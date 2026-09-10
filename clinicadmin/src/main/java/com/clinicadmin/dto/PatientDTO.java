package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class PatientDTO {

    private String patientId;
    private String patientName;
    private String mobileNumber;
    private Integer age;
    private String gender;
}