package com.clinicadmin.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestOrderDTO {

    private String id;

    private String patientId;
    private String patientName;
    private String mobileNumber;

    private String clinicId;
    private String branchId;

    private String bookingId;

    private String doctorId;
    private String doctorName;

    private String testName;
    private String testNameId;

    private String vendorId;
    private String vendorName;
    private String vendorEmail;

    private String status;

    private Instant orderedAt;
}