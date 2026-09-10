package com.clinicadmin.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class DiagnosisPatientDTO {

    private String patientId;

    private String patientName;

    private String mobileNumber;

    private String bookingId;

    private String doctorId;

    private String doctorName;

    private String vendorId;

    private String vendorName;

    private String status;

    private Instant orderedAt;

}