package com.clinicadmin.controller;

import com.clinicadmin.dto.TherapistAnalyticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.TreatmentAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class TreatmentAnalyticsController {

	private final TreatmentAnalyticsService analyticsService;

	// GET /clinic-admin/analytics/treatments/{clinicId}/{branchId}/{type}/{period}
	// e.g. .../CLN001/BR001/All Types/Month
	@GetMapping("/analytics/treatments/{clinicId}/{branchId}/{type}/{period}")
	public ResponseEntity<Response> getTreatmentAnalytics(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String type, @PathVariable String period) {

	Response response= analyticsService.getTreatmentAnalytics(clinicId, branchId, type, period);
	
	return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET
	// /clinic-admin/analytics/treatments/{clinicId}/{branchId}/{fromdate}/{todate}/{type}
	// dates in yyyy-MM-dd, both inclusive. e.g.
	// .../CLN001/BR001/2026-06-01/2026-06-30/Activity
	@GetMapping("/analytics/treatments/{clinicId}/{branchId}/{type}/{fromdate}/{todate}")
	public  ResponseEntity<Response> getTreatmentAnalyticsByDateRange(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String type, @PathVariable String fromdate,
			@PathVariable String todate) {

		Response response=analyticsService.getTreatmentAnalyticsByDateRange(clinicId, branchId, type, fromdate, todate);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

}