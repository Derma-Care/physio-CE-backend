package com.clinicadmin.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.clinicadmin.dto.Response;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.service.RevenueService;
import com.clinicadmin.utils.RevenueResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevenueImpl implements RevenueService {
	
	private final PhysiotherapyFeignClient physiotherapyFeignClient;
	
	 @Override
		public ResponseEntity<RevenueResponse> getRevenueManagement(
				 String clinicId,
				 String branchId,
				 String number){
					 
			try {
				return physiotherapyFeignClient.getRevenueManagement(clinicId, branchId, number);
			}catch(Exception e) { 
				RevenueResponse res = new RevenueResponse();
				res.setMessage(e.getMessage());
				res.setStatus(500);
				res.setSuccess(false);
				return ResponseEntity.status(500).body(res);
			}}
	    
	 @Override
		public ResponseEntity<RevenueResponse> getRevenueManagementByDateRange(
				 String clinicId,
				 String branchId,
				 String startDate,
				 String endDate){
			try {
				return physiotherapyFeignClient.getRevenueManagementByDateRange(clinicId, branchId,startDate, endDate);
			}catch(Exception e) { 
				RevenueResponse res = new RevenueResponse();
				res.setMessage(e.getMessage());
				res.setStatus(500);
				res.setSuccess(false);
				return ResponseEntity.status(500).body(res);
			}
		}
	    
	 @Override
		public ResponseEntity<Response> getRevenueSummary(
				 String clinicId,
				 String branchId){
			
			try {
				return physiotherapyFeignClient.getRevenueSummary(clinicId, branchId);
			}catch(Exception e) { 
				Response res = new Response();
				res.setMessage(e.getMessage());
				res.setStatus(500);
				res.setSuccess(false);
				return ResponseEntity.status(500).body(res);
			}
			
		}
	

}
