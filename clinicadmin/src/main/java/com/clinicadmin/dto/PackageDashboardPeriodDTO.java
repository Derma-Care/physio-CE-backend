package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Counts for a single period (Today / This Week / This Month / This Year /
 * Overall) on the package & sitting dashboard widget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDashboardPeriodDTO {

    private Integer totalBookings;          // total treatment-schedule records created in this period
    private Integer totalPatients;          // distinct patients booked in this period

    private Integer singleSittingPatients;  // distinct patients who took a single-sitting service
    private Integer packagePatients;        // distinct patients who took a multi-sitting package
}