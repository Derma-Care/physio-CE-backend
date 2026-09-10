package com.clinicadmin.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.clinicadmin.dto.Response;
import com.clinicadmin.utils.RevenueResponse;


@FeignClient(name = "physiotherapydoctor-service")
public interface PhysiotherapyFeignClient {

    @PutMapping("/api/physiotherapy-doctor/updateSessionFromTherapist/{therapistRecordId}/{sessionId}")
    void updateSessionStatus(
            @PathVariable("therapistRecordId") String therapistRecordId,
            @PathVariable("sessionId") String sessionId
    );

    @GetMapping("/api/physiotherapy-doctor/payment/{bookingId}")
    Response getPayment(
            @PathVariable("bookingId") String bookingId
    );

    @GetMapping("/api/physiotherapy-doctor/get-record/{clinicId}/{branchId}/{patientId}/{bookingId}/{therapistRecordId}")
    Response getRecord(
            @PathVariable("clinicId") String clinicId,
            @PathVariable("branchId") String branchId,
            @PathVariable("patientId") String patientId,
            @PathVariable("bookingId") String bookingId,
            @PathVariable("therapistRecordId") String therapistRecordId
    );
    
    @GetMapping("/api/physiotherapy-doctor/getPayments/{clinicId}/{branchId}")
    Response getPayments(
            @PathVariable String clinicId,
            @PathVariable String branchId);
    
    @GetMapping("/api/physiotherapy-doctor/today-session-count/{clinicId}/{branchId}/{therapistId}")
	public int getTodaySessionCount(
	        @PathVariable String clinicId,
	        @PathVariable String branchId,
	        @PathVariable String therapistId);
    
    @GetMapping("/api/physiotherapy-doctor/assigned-therapist/{therapistRecordId}")
    Response getAssignedTherapistDetails(
            @PathVariable String therapistRecordId);
    
    @GetMapping("/api/physiotherapy-doctor/revenue-management/{clinicId}/{branchId}/{number}")
	public ResponseEntity<RevenueResponse> getRevenueManagement(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String number);
    
    @GetMapping("/api/physiotherapy-doctor/revenue-management/date-range/{clinicId}/{branchId}/{startDate}/{endDate}")
	public ResponseEntity<RevenueResponse> getRevenueManagementByDateRange(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String startDate,
			@PathVariable String endDate);
    
    @GetMapping("/api/physiotherapy-doctor/revenue-summary/{clinicId}/{branchId}")
	public ResponseEntity<Response> getRevenueSummary(
			@PathVariable String clinicId,
			@PathVariable String branchId);


    
    


}