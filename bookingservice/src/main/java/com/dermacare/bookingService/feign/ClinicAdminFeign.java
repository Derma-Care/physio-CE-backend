package com.dermacare.bookingService.feign;

import java.util.List;
import java.util.Map;

import com.dermacare.bookingService.dto.Sittings;
import com.dermacare.bookingService.dto.TreatmentScheduleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dermacare.bookingService.dto.CustomerOnbordingDTO;
import com.dermacare.bookingService.util.Response;


@FeignClient(value = "clinicadmin")
public interface ClinicAdminFeign {
	
	
    // =============================================
    // CUSTOMER ONBOARDING
    // =============================================

    @PostMapping("/clinic-admin/customers/onboard")
    ResponseEntity<Response> onboardCustomer(
            @RequestBody CustomerOnbordingDTO dto);
	
	 @GetMapping("/clinic-admin/customer/patientId/{patientId}/{clinicId}")
	    public ResponseEntity<Response> getCustomerByPatientId(@PathVariable String patientId,@PathVariable String clinicId);
	                                                                                        
	  @GetMapping("/clinic-admin/expenses/today/{clinicId}/{branchId}")
	    public Double getTodayExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId);
	  
	  
	  @GetMapping("/clinic-admin/expenses/weekly/{clinicId}/{branchId}")
	    public Double getWeeklyExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId);
	  
	  @GetMapping("/clinic-admin/expenses/monthly/{clinicId}/{branchId}")
	    public Double getMonthlyExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId); 
	  
	  @GetMapping("/clinic-admin/expenses/custom/{startDate}/{endDate}")
	    public Double customFilter(
	    		@PathVariable String startDate,
	    		@PathVariable String endDate);
	  
	  @GetMapping("/clinic-admin/customers/mobilenumber/{mobilenumber}/name/{name}")
	    public Map<String,String> getCustomerByMobilenumberAndName(@PathVariable String mobilenumber,@PathVariable String name);
	     
	  @GetMapping("/clinic-admin/customer/mobilenumber/{mobilenumber}/{clinicId}")
	    public CustomerOnbordingDTO getCustomerByMobileNumberAndClinicId(@PathVariable String mobilenumber,@PathVariable String clinicId);
	    
	    
	    @GetMapping("/clinic-admin/customer/name/{name}/{clinicId}")
	    public  List<CustomerOnbordingDTO> getCustomerByNameAndClinicId(@PathVariable String name,@PathVariable String clinicId);
	 // ─────────────────────────────────────────────────────────────────
	    // S3 — Get signed URL for a raw S3 key
	    // Used to convert report file keys → accessible signed URLs
	    // before returning BookingResponse to frontend
	    // ─────────────────────────────────────────────────────────────────
	    @GetMapping("/clinic-admin/api/s3/signed-url")
	    String getSignedUrl(@RequestParam("fileKey") String fileKey);
	    
	    
	    @PutMapping("/clinic-admin/makingFalseDoctorSlot/{doctorId}/{branchId}/{date}/{time}")
		public boolean makingFalseDoctorSlot(@PathVariable String doctorId, @PathVariable String branchId,
				@PathVariable String date, @PathVariable String time);
	    
	    @PutMapping("/clinic-admin/updateDoctorSlotWhileBooking/{doctorId}/{branchId}/{date}/{time}")
		public boolean updateDoctorSlotWhileBooking(@PathVariable String doctorId, @PathVariable String branchId,
				@PathVariable String date, @PathVariable String time);
	    
	    @GetMapping("/clinic-admin/deviceId/{clinicId}/{branchId}")
		public String getDeviceId(@PathVariable String clinicId, @PathVariable String branchId);

		@GetMapping("/clinic-admin/deviceIdByCustomerId/{customerId}")
		public String customerDeviceId(@PathVariable String customerId);

		@GetMapping("/clinic-admin/doctor/getDeviceId/{doctorId}")
		String getDoctorDeviceId(@PathVariable String doctorId);
		
		@GetMapping("/clinic-admin/soap-notes/booking/{bookingId}")
		public ResponseEntity<Response> getSoapNotesByBooking(@PathVariable String bookingId);

		 @GetMapping("/clinic-admin/PaymentInfo/bookingId/{bookingId}")    
		public Map<String,Double> getAllPayments(@PathVariable String bookingId);

    @GetMapping("/clinic-admin/booking/{bookingId}")
    public TreatmentScheduleDTO getByBookingId(@PathVariable String bookingId) ;

	@GetMapping("/clinic-admin/treatment-schedule/sittings/clinicId/{clinicId}/branchId/{branchId}/bookingId/{bookingId}/dates/{startDate}/{endDate}")
	public TreatmentScheduleDTO getSittings(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String bookingId,
			@PathVariable String startDate,
			@PathVariable String endDate);


	@PostMapping("/clinic-admin/filtereByDates/{clinicId}/{branchId}/{startDate}/{endDate}")
	public List<TreatmentScheduleDTO> getFilteredSittings(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String startDate,
			@PathVariable String endDate,
			@RequestBody List<String> ids);

}
