package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.RevenueAnalyticsService;

@RestController
@RequestMapping("/clinic-admin")
public class RevenueAnalyticsController {

	@Autowired
	private RevenueAnalyticsService revenueAnalyticsService;

	/**
	 * GET /clinic-admin/payments/revenue-analytics/{clinicId}/{branchId}/{filterType}
	 *
	 * For the predefined filters only.
	 *
	 * filterType - 1=Today, 2=This Week, 3=This Month, 4=This Year, 5=Overall
	 */
	@GetMapping("/payments/revenue-analytics/{clinicId}/{branchId}/{filterType}")
	public ResponseEntity<Response> getRevenueAnalytics(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable Integer filterType) {

		Response response = revenueAnalyticsService.getRevenueAnalytics(clinicId, branchId, filterType);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}

	/**
	 * GET /clinic-admin/payments/revenue-analytics/custom/{clinicId}/{branchId}/{startDate}/{endDate}
	 *
	 * Dedicated endpoint for a custom date range.
	 *
	 * startDate / endDate - yyyy-MM-dd
	 */
	@GetMapping("/payments/revenue-analytics/custom/{clinicId}/{branchId}/{startDate}/{endDate}")
	public ResponseEntity<Response> getRevenueAnalyticsCustomRange(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String startDate,
			@PathVariable String endDate) {

		Response response = revenueAnalyticsService.getRevenueAnalyticsCustomRange(
				clinicId, branchId, startDate, endDate);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}

	/**
	 * GET /clinic-admin/payments/revenue-analytics/dashboard-totals/{clinicId}/{branchId}
	 *
	 * Quick-glance widget: Today / This Week / This Month / This Year paid
	 * amounts, all returned together, computed from actual transaction dates
	 * (not bill-level snapshots).
	 */
	@GetMapping("/payments/revenue-analytics/dashboard-totals/{clinicId}/{branchId}")
	public ResponseEntity<Response> getDashboardTotals(
			@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = revenueAnalyticsService.getDashboardTotals(clinicId, branchId);

		return ResponseEntity.status(response.getStatus() == 0 ? HttpStatus.OK.value() : response.getStatus())
				.body(response);
	}
}