package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full payload for the Package & Sitting Analytics dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageSittingAnalyticsResponseDTO {

    private String filterApplied; // e.g. "Today", "This Month", "Custom (2026-07-01 to 2026-07-31)", "Overall"
    private String rangeStart;    // yyyy-MM-dd, null when Overall
    private String rangeEnd;      // yyyy-MM-dd, null when Overall

    private Integer totalPackagesAndSittingTypes; // distinct packageId count
    private Integer totalPatientsOverall;          // distinct patients across all packages
    private Integer totalBookingsOverall;          // total treatment-schedule records

    private Integer totalSingleSittingPatients; // distinct patients who took a single-sitting service
    private Integer totalPackagePatients;       // distinct patients who took a multi-sitting package

    private SittingStatusCountsDTO overallStatusCounts; // aggregated across everything

    private List<PackageAnalyticsDTO> packages; // one entry per distinct packageId
}