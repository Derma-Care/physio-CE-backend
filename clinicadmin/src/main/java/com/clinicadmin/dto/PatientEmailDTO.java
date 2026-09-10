package com.clinicadmin.dto;

import lombok.Data;

@Data
public class PatientEmailDTO {

    private String title;

    private String patientName;

    private String patientMail;

    private String pdfFile;

    private String hospitalId;   // ✅ changed from patientId
}