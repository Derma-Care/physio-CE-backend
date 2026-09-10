package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface RevenueAnalyticsService {

	/**
	 * @param clinicId   required
	 * @param branchId   required
	 * @param filterType 1=Today, 2=This Week, 3=This Month, 4=This Year, 5=Overall.
	 *                   Defaults to 5 (Overall) if null.
	 */
	Response getRevenueAnalytics(String clinicId, String branchId, Integer filterType);

	/**
	 * Dedicated custom date-range entry point.
	 *
	 * @param clinicId  required
	 * @param branchId  required
	 * @param startDate required, yyyy-MM-dd
	 * @param endDate   required, yyyy-MM-dd
	 */
	Response getRevenueAnalyticsCustomRange(String clinicId, String branchId, String startDate, String endDate);

	/**
	 * Quick-glance dashboard widget: Today / This Week / This Month / This Year
	 * paid-amount totals, all computed at once from actual transaction dates.
	 */
	Response getDashboardTotals(String clinicId, String branchId);
}