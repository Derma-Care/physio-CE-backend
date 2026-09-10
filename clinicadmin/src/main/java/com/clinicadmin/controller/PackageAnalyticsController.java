package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PackageAnalyticsService;

@RestController
@RequestMapping("/clinic-admin")
public class PackageAnalyticsController {

	@Autowired
	private PackageAnalyticsService packageAnalyticsService;

	/**
	 * GET /clinic-admin/package-analytics/{clinicId}/{branchId}/{filterType}
	 *
	 * filterType - 1=Today, 2=This Week, 3=This Month, 4=This Year, 5=Overall
	 *
	 * Full package & sitting analytics: for every package (and every
	 * single-sitting service) booked in the selected period - how many patients
	 * took it, sitting status breakdown, remaining sittings owed, and a
	 * per-patient drill-down list. Filtering is based on when each patient was
	 * booked in (TreatmentSchedule.createdAt).
	 */
	@GetMapping("/package-analytics/{clinicId}/{branchId}/{filterType}")
	public ResponseEntity<Response> getPackageAnalytics(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable Integer filterType) {

		Response response = packageAnalyticsService.getPackageAnalytics(clinicId, branchId, filterType);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}

	/**
	 * GET /clinic-admin/package-analytics/custom/{clinicId}/{branchId}/{startDate}/{endDate}
	 *
	 * Same as above but for a custom date range. startDate/endDate - yyyy-MM-dd.
	 */
	@GetMapping("/package-analytics/custom/{clinicId}/{branchId}/{startDate}/{endDate}")
	public ResponseEntity<Response> getPackageAnalyticsCustomRange(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String startDate,
			@PathVariable String endDate) {

		Response response = packageAnalyticsService.getPackageAnalyticsCustomRange(
				clinicId, branchId, startDate, endDate);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}

	/**
	 * GET /clinic-admin/package-analytics/dashboard-totals/{clinicId}/{branchId}
	 *
	 * Quick-glance widget: Today / This Week / This Month / This Year / Overall
	 * counts, all returned together - total patients, total bookings, and the
	 * single-sitting vs multi-sitting-package split for each period.
	 */
	@GetMapping("/package-analytics/dashboard-totals/{clinicId}/{branchId}")
	public ResponseEntity<Response> getPackageDashboardTotals(
			@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = packageAnalyticsService.getPackageDashboardTotals(clinicId, branchId);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}
}