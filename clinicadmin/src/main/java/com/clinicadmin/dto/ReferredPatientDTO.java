package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single patient row that came in through a referral.
 * One of these is created per booking under a referring doctor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferredPatientDTO {

    private String bookingId;
    private String patientId;
    private String patientName;
    private String patientMobileNumber;
    private String doctorName;      // doctor who actually treated this patient
    private String packageType;
    private String packageName;
    private Double cost;            // finalAmount from Payment
    private Double paidAmount;      // amountPaying from Payment
    private Double dueAmount;       // remainingDue from Payment
}