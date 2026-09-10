package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Powers the 5 summary cards at the top of the Revenue Analytics screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryDTO {

    private Double totalRevenue;        // sum of finalAmount across matched bills
    private Double totalPaidAmount;      // sum of amountPaying across matched bills
    private Double totalDueAmount;       // sum of remainingDue across matched bills
    private Double totalDiscountAmount;  // sum of discountAmount across matched bills
    private Long totalNumberOfBills;     // count of matched bills
}