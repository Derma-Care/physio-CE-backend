package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface PackageAnalyticsService {

	/**
	 * Full package & sitting analytics dashboard for a clinic+branch, grouped by
	 * packageId, with patient-level and sitting-status breakdowns.
	 *
	 * @param filterType 1=Today, 2=This Week, 3=This Month, 4=This Year, 5=Overall.
	 *                   Defaults to 5 (Overall) if null. Filtering is based on when
	 *                   each patient was booked into the package/sitting
	 *                   (TreatmentSchedule.createdAt).
	 */
	Response getPackageAnalytics(String clinicId, String branchId, Integer filterType);

	/**
	 * Same as above but for a custom date range (format yyyy-MM-dd).
	 */
	Response getPackageAnalyticsCustomRange(String clinicId, String branchId, String startDate, String endDate);

	/**
	 * Quick-glance dashboard widget: Today / This Week / This Month / This Year /
	 * Overall counts, all returned together - total patients, total bookings, and
	 * the single-sitting vs multi-sitting-package split for each period.
	 */
	Response getPackageDashboardTotals(String clinicId, String branchId);
}