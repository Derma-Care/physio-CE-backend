package com.clinicadmin.feignclient;

import java.util.List;
import java.util.Map;

import com.clinicadmin.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "bookingservice")
public interface BookingFeign {

	@GetMapping("/api/v1/getBookedServiceById/{id}")
	public ResponseEntity<ResponseStructure< Map<String, Object>>> getBookedService(@PathVariable String id);
	
	
	@GetMapping("/api/v1/getAppointmentByPatientId/{patientId}")
	public ResponseEntity<?> getAppointmentByPatientId(@PathVariable String patientId);
	
	//---------------------------to get patientdetails by bookingId,pateintId,mobileNumber---------------------------
	@GetMapping("/api/v1/getPatientDetailsForConsetForm/{bookingId}/{patientId}/{mobileNumber}")
	public ResponseEntity<Response> getPatientDetailsForConsentForm(@PathVariable String bookingId,@PathVariable String patientId,@PathVariable String mobileNumber);

		
	@PutMapping("/api/v1/updateAppointment")
	public ResponseEntity<?> updateAppointment(@RequestBody BookingResponse bookingResponse );
	
//	@PostMapping("/api/v1/bookService")
//	public ResponseEntity<ResponseStructure<BookingResponse>> bookService(@RequestBody BookingRequset req);
	
	@DeleteMapping("/api/v1/deleteService/{id}")
	//@CircuitBreaker(name = "circuitBreaker", fallbackMethod = "deleteBookedServiceFallBack")
	public ResponseEntity<ResponseStructure<BookingResponse>> deleteBookedService(@PathVariable String id);
	
	@GetMapping("/api/v1/getBookedServicesByMobileNumber/{mobileNumber}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getCustomerBookedServices(
			@PathVariable String mobileNumber);
	
	@GetMapping("/api/v1/getAllBookedServices")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedService();
	
	@GetMapping("/api/v1/getAllBookedServices/{doctorId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByDoctorId(@PathVariable String doctorId);

	@GetMapping("/api/v1/getBookedServicesByServiceId/{serviceId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByServiceId(@PathVariable String serviceId);
	
	@GetMapping("/api/v1/getBookedServicesByClinicId/{clinicId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByClinicId(@PathVariable String clinicId);

	
	@GetMapping("/api/v1/getInProgressAppointments/{mobilenumber}")
	public ResponseEntity<?> inProgressAppointments(@PathVariable String mobilenumber);
	
	@GetMapping("/api/v1/getAllBookedServicesByBranchId/{branchId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedServicesByBranchId(@PathVariable String branchId);
	
	@GetMapping("/api/v1/getBookedServicesByClinicIdWithBranchId/{clinicId}/{branchId}")
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getBookedServicesByClinicIdWithBranchId(
	        @PathVariable String clinicId,
	        @PathVariable String branchId);
	
	@GetMapping("/api/v1/appointments/byIds/{clinicId}/{branchId}")
	public ResponseEntity<?> retrieveOneWeekAppointments(@PathVariable String clinicId,@PathVariable String branchId);
	
	@GetMapping("/api/v1/appointments/byIdsAndDate/{clinicId}/{branchId}/{date}")
	public ResponseEntity<?> retrieveAppointnmentsByServiceDate(@PathVariable String clinicId,@PathVariable String branchId,@PathVariable String date);
	
	@PutMapping("/api/v1/update/bookingId")
	public ResponseEntity<ResponseStructure<BookingResponse>>  updateAppointmentBasedOnBookingId(@RequestBody BookingResponse bookingResponse );
	
	@PostMapping("/api/v1/appointments/serviceDate/serviceTime/DoctorId")
	public BookingResponse blockingSlot(@RequestBody TempBlockingSlot temp);
	
	@GetMapping("/api/v1/appointments/byInput/{input}/{clinicId}")	
	public ResponseEntity<?> retrieveAppointnmentsByInput(@PathVariable String input,@PathVariable String clinicId);
//-----------------------------New Api for clinic admin  coummunicating from booking service for front end--------------------	
	@GetMapping("/api/v1/appointments/patientId/{patientId}")	
	public  ResponseEntity<?> getBookingByPatientId(@PathVariable String patientId);
	
	@PostMapping("/api/v1/bookService")
	public ResponseEntity<Response> bookService(@RequestBody BookingResponse req);
	
	@GetMapping("/api/v1/appointments/Inprogress/patientId/{patientId}")
	public ResponseEntity<?> getInprogressAppointmentsByPatientId(@PathVariable String patientId);
	
	@GetMapping("/api/v1/appointments/Inprogress/patientId/{patientId}/{clinicId}")
	public ResponseEntity<?> getInprogressAppointmentsByPatientIdAndClinicId(@PathVariable String patientId,@PathVariable String clinicId );
	
	
	@GetMapping("/api/v1/report/{clinicId}/{branchId}/{number}/{startDate}/{endDate}")
	public ResponseEntity<Response> getReport(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable Integer number,
			@PathVariable String startDate,
			@PathVariable String endDate);
	
	@PostMapping("/api/v1/bookPhysioAppointment")
	public  ResponseEntity<Response> bookPhysioAppointment(@RequestBody BookingRequset req);

    @GetMapping("/api/v1/customDate/{clinicId}/{branchId}/{date}")
    public ResponseEntity<Response> getCustomDateBookings(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String date);

    @GetMapping("/api/v1/upcoming/{clinicId}/{branchId}/{option}")
	    public ResponseEntity<Response> getUpcomingBookings(
	    		  @PathVariable String clinicId,
	    		  @PathVariable String branchId,
	    		  @PathVariable int option);
	  
	  @GetMapping("/api/v1//basedOnDate/{clinicId}/{branchId}/{date}")
	    public ResponseEntity<Response> getPhysioBookingBasedOnDate(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
	            @PathVariable String date);
	  
	  @GetMapping("/api/v1/customeRange/{clinicId}/{branchId}/{start}/{end}")
	    public ResponseEntity<Response> getPhysioBookingsByCustomeRange(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
	            @PathVariable String start,
	            @PathVariable String end);
	  
	  @GetMapping("/api/v1/getBookingById/{bookingId}")
	    public ResponseEntity<Response> getBookingById(@PathVariable String bookingId);
	    
	  
	  @GetMapping("/api/v1/getTodayBookings/{clincId}/{branchId}")
		public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getTodayBookings(@PathVariable String clincId,@PathVariable String branchId);
		
	  
	  @GetMapping("/api/v1/in-progress/appointments/{patientId}/{bookingId}")
		public ResponseEntity<?> getInProgressAppointmentByPatientIdAndBookingId(@PathVariable String patientId,@PathVariable String bookingId);
		
	  @GetMapping("/api/v1/reports/patientId/{patientId}")
	    public ResponseEntity<Response> getReportsByPatientId(@PathVariable String patientId);
	 
	  @GetMapping("/api/v1/deleteReport/{bookingId}/{index}")
	    public void deleteReport(@PathVariable String bookingId,@PathVariable String index);
	  
	  @GetMapping("/api/v1/filter/status/{clinicId}/{branchId}")
	    public ResponseEntity<Response> getFilteredBookingsByStatus(
	            @PathVariable String clinicId,
	            @PathVariable String branchId);

}