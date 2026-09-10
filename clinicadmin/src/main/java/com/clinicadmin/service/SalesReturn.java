//package com.clinicadmin.service;
//
//import java.time.LocalDate;
//import java.util.List;
//import org.springframework.http.ResponseEntity;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.SalesReturnCreateResponse;
//import com.clinicadmin.dto.SalesReturnRequest;
//import com.clinicadmin.dto.SalesReturnResponse;
//import com.clinicadmin.dto.SalesReturnSummaryResponse;
//import com.clinicadmin.dto.SalesReturnUpdateRequest;
//import com.clinicadmin.utils.ApiResponse;
//
//public interface SalesReturn {
//	
//	public ResponseEntity<ApiResponse<SalesReturnCreateResponse>> create(
//	           SalesReturnRequest request);
//	
//	public ResponseEntity<ApiResponse<SalesReturnResponse>> getByReturnNo(
//	           String returnNo);
//	
//	 public ResponseEntity<ApiResponse<Void>> update(
//	            String returnNo,
//	           SalesReturnUpdateRequest request);
//	 
//	  public ResponseEntity<ApiResponse<Void>> cancel(
//	           String returnNo);
//	  
//	  public ResponseEntity<ApiResponse<List<SalesReturnSummaryResponse>>> filter(
//	           String patientName,
//	           String mobileNo,
//	           String billNo,
//	           String returnType,
//	           LocalDate fromDate,
//	            LocalDate toDate,
//	           String clinicId,
//	          String branchId);
//	  
//	  public ResponseEntity<Response> getAllSalesReturns();
//	  
//	  public ResponseEntity<Response> getAllSalesByClinicAndBranchId(String cid, String bid);
//		    
//		        
//
//}
