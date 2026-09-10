//package com.clinicadmin.service.impl;
//
//import java.time.LocalDate;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.SalesReturnCreateResponse;
//import com.clinicadmin.dto.SalesReturnRequest;
//import com.clinicadmin.dto.SalesReturnResponse;
//import com.clinicadmin.dto.SalesReturnSummaryResponse;
//import com.clinicadmin.dto.SalesReturnUpdateRequest;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.SalesReturn;
//import com.clinicadmin.utils.ApiResponse;
//
//import feign.FeignException;
//
//
//@Service
//public class SalesReturnImpl implements SalesReturn {
//	
//	@Autowired
//	public PharmacyManagementFeignClient pharmacyManagementFeignClient;
//	
//	public ResponseEntity<ApiResponse<SalesReturnCreateResponse>> create(
//           SalesReturnRequest request){
//		 ResponseEntity<ApiResponse<SalesReturnCreateResponse>> res = null;
//		try {
//			res = pharmacyManagementFeignClient.create(request);
//		}catch(FeignException e) {}
//		return res;
//		
//	}
//	
//	
//	    public ResponseEntity<ApiResponse<SalesReturnResponse>> getByReturnNo(
//	           String returnNo){
//	    	ResponseEntity<ApiResponse<SalesReturnResponse>> res = null;
//	    	try {
//	    		res = pharmacyManagementFeignClient.getByReturnNo(returnNo);
//	    	}catch(FeignException e) {}
//	    	return res;
//	    }
//	    
//	    
//	    public ResponseEntity<Response> getAllSalesReturns(){
//	    	 ResponseEntity<Response> res = null;
//		    	try {
//		    		res = pharmacyManagementFeignClient.getAllSalesReturns();
//		    	}catch(FeignException e) {}
//		    	return res;
//		    }
//	    
//	    
//	    public ResponseEntity<Response> getAllSalesByClinicAndBranchId(String cid, String bid){
//	    	 ResponseEntity<Response> res = null;
//		    	try {
//		    		res = pharmacyManagementFeignClient.getAllSalesByClinicAndBranchId(cid, bid);
//		    	}catch(FeignException e) {}
//		    	return res;
//		    }
//		    
//	   	
//	    public ResponseEntity<ApiResponse<Void>> update(
//	            String returnNo,
//	           SalesReturnUpdateRequest request){
//	    	ResponseEntity<ApiResponse<Void>> res = null;
//	    	try {
//	    		res = pharmacyManagementFeignClient.update(returnNo, request);
//	    	}catch(FeignException e) {}
//	    	return res;
//	    }
//	    
//	    
//	 
//	    public ResponseEntity<ApiResponse<Void>> cancel(
//	           String returnNo){
//	    	ResponseEntity<ApiResponse<Void>> res = null;
//	    	try {
//	    		res = pharmacyManagementFeignClient.cancel(returnNo);
//	    	}catch (FeignException e) {}
//				return res;
//			
//	    }
//	    
//	    
//	   
//	    public ResponseEntity<ApiResponse<List<SalesReturnSummaryResponse>>> filter(
//	           String patientName,
//	           String mobileNo,
//	           String billNo,
//	           String returnType,
//	           LocalDate fromDate,
//	            LocalDate toDate,
//	           String clinicId,
//	          String branchId){
//	    	ResponseEntity<ApiResponse<List<SalesReturnSummaryResponse>>> res = null;
//	    	try {
//	    		res = pharmacyManagementFeignClient.filter(patientName, mobileNo, billNo, returnType, fromDate, toDate, clinicId, branchId);
//	    	}catch (FeignException e) {}
//			return res;
//	    
//	    }
//	   
//}
