package com.clinicadmin.service;

import org.springframework.http.ResponseEntity;

import com.clinicadmin.dto.Response;
import com.clinicadmin.utils.RevenueResponse;

public interface RevenueService {
	
	
	public ResponseEntity<RevenueResponse> getRevenueManagement(
			 String clinicId,
			 String branchId,
			 String number);
	
	public ResponseEntity<RevenueResponse> getRevenueManagementByDateRange(
			 String clinicId,
			 String branchId,
			 String startDate,
			 String endDate);
	
	public ResponseEntity<Response> getRevenueSummary(
			 String clinicId,
			 String branchId);
		
	

}
