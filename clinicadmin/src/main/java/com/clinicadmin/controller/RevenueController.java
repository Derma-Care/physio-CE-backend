package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.RevenueService;
import com.clinicadmin.utils.RevenueResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class RevenueController {
	
	private final RevenueService revenueService;
	
	 @GetMapping("/revenue-management/{clinicId}/{branchId}/{number}")
		public ResponseEntity<RevenueResponse> getRevenueManagement(
				@PathVariable String clinicId,
				@PathVariable String branchId,
				@PathVariable String number){
		return revenueService.getRevenueManagement(clinicId, branchId, number);
	}
	
	  @GetMapping("/revenue-management/date-range/{clinicId}/{branchId}/{startDate}/{endDate}")
		public ResponseEntity<RevenueResponse> getRevenueManagementByDateRange(
				@PathVariable String clinicId,
				@PathVariable String branchId,
				@PathVariable String startDate,
				@PathVariable String endDate){
		return revenueService.getRevenueManagementByDateRange(clinicId, branchId, startDate,endDate);
	}
	
	  @GetMapping("/revenue-summary/{clinicId}/{branchId}")
		public ResponseEntity<Response> getRevenueSummary(
				@PathVariable String clinicId,
				@PathVariable String branchId){
		return revenueService.getRevenueSummary(clinicId, branchId);
	}
}
