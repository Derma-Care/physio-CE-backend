package com.clinicadmin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the "Patient Revenue Analytics" table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRevenueDTO {

    private String patientId;
    private String patientName;

    private Double totalBillAmount; // sum of finalAmount across the patient's matched bills
    private Double paidAmount;      // sum of amountPaying across the patient's matched bills
    private Double dueAmount;       // totalBillAmount - paidAmount (floored at 0)

    private Integer numberOfVisits; // count of matched bills for this patient
    private LocalDateTime lastPaymentDate; // date of the most recent single transaction
    private Double lastPaidAmount;         // amount of that most recent single transaction (not cumulative)
}