package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Quick-glance dashboard totals - shows Today / This Week / This Month /
 * This Year side by side without needing to call the filtered
 * revenue-analytics endpoint four separate times.
 *
 * Every figure here is computed from actual transaction history entries
 * (i.e. real money collected on that date), NOT from a bill's cumulative
 * "amountPaying" snapshot - so "todayPaidAmount" genuinely means "collected
 * today", not "total paid so far on bills touched today".
 *
 * Note the periods are NOT mutually exclusive - This Week naturally
 * includes Today's collections, This Month includes This Week's, etc.
 * That mirrors how these are normally read on a dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTotalsDTO {

    private Double todayPaidAmount;
    private Double weekPaidAmount;
    private Double monthPaidAmount;
    private Double yearPaidAmount;
    private Double overallPaidAmount;
}