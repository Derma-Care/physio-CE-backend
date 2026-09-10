package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Quick-glance package/sitting dashboard: Today / This Week / This Month /
 * This Year / Overall shown side by side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDashboardTotalsDTO {

    private PackageDashboardPeriodDTO today;
    private PackageDashboardPeriodDTO week;
    private PackageDashboardPeriodDTO month;
    private PackageDashboardPeriodDTO year;
    private PackageDashboardPeriodDTO overall;
}