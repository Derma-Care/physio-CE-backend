package com.dermacare.bookingService.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dermacare.bookingService.dto.BookingRequset;
import com.dermacare.bookingService.dto.BookingResponse;
import com.dermacare.bookingService.dto.ReportsDTO;
import com.dermacare.bookingService.dto.TempBlockingSlot;
import com.dermacare.bookingService.service.BookingService_Service;
import com.dermacare.bookingService.util.Response;
import com.dermacare.bookingService.util.ResponseStructure;

@RestController
@RequestMapping("/v1")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BookingServiceController {


	@Autowired
	private BookingService_Service service;


	@PostMapping("/bookService")
	public  ResponseEntity<?> bookService(@RequestBody BookingResponse req) {
		return service.addService(req);}
		

	@DeleteMapping("/deleteService/{id}")
	public ResponseEntity<ResponseStructure<BookingResponse>> deleteBookedService(@PathVariable String id) {
		BookingResponse response = service.deleteService(id);
		if(response != null) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(response, "Booked Service Fetched Sucessfully",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}
			else {
				return new ResponseEntity<>(ResponseStructure.buildResponse(null, "Booked Service Not Found",
						HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}}
	
	
	@GetMapping("/getTodayBookings/{clincId}/{branchId}")
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getTodayBookings(@PathVariable String clincId,@PathVariable String branchId) {
		List<Map<String,Object>> response = service.getTodayBookings(clincId, branchId);
		
		if (response != null && !response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(response, "Booked Service Fetched Sucessfully",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}
			else {
				return new ResponseEntity<>(ResponseStructure.buildResponse(null, "Booked Service Not Found",
						HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}}
	
	

	@GetMapping("/getBookedServiceById/{id}")
	public ResponseEntity<ResponseStructure< Map<String, Object> >> getBookedService(@PathVariable String id) { 
		 Map<String, Object>  response = service.getBookedService(id);
		if(response != null) {
		return new ResponseEntity<>(ResponseStructure.buildResponse(response, "Booked Service Fetched Sucessfully",
				HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}
		else {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null, "Booked Service Not Found",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);}
	}

	
	@GetMapping("/getBookedServicesByMobileNumber/{mobileNumber}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getCustomerBookedServices(
			@PathVariable String mobileNumber) {
		List<BookingResponse> response = service.getBookedServices(mobileNumber);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null, "Customer does not have any booking",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response, "Booked Service Fetched Sucessfully",
				HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
	}

	
	@GetMapping("/getAllBookedServices")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedService() {
		List<BookingResponse> response = service.getAllBookedServices();
		if (response == null || response.isEmpty() ) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null, "Customer does not have any booking",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response, "Booked Service Fetched Sucessfully",
				HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
	}

	@GetMapping("/getAllBookedServices/{doctorId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByDoctorId(@PathVariable String doctorId) {

		List<BookingResponse> response = service.bookingByDoctorId(doctorId);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null,
					"Docotor Does not involved in any Booking yet ", HttpStatus.OK, HttpStatus.OK.value()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response,
				"Booked Service Fetched Sucessfully on DoctorId" + doctorId, HttpStatus.OK, HttpStatus.OK.value()),
				HttpStatus.OK);

	}
	
	
	@GetMapping("/getAllBookedServicesByBranchId/{branchId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getAllBookedServicesByBranchId(@PathVariable String branchId) {

		List<BookingResponse> response = service.bookingByBranchId(branchId);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null,
					"unable to find any Bookings yet ", HttpStatus.OK, HttpStatus.OK.value()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response,
				"Booked Service Fetched Sucessfully on branchId : " + branchId, HttpStatus.OK, HttpStatus.OK.value()),
				HttpStatus.OK);

	}

	// @GetMapping("/getBookedServicesByServiceId/{serviceId}")
	// public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByServiceId(@PathVariable String serviceId) {

	// 	List<BookingResponse> response = service.bookingByServiceId(serviceId);
	// 	if (response == null || response.isEmpty()) {
	// 		return new ResponseEntity<>(ResponseStructure.buildResponse(null,
	// 				"Service Does not Booked by AnyOne" + serviceId, HttpStatus.OK, HttpStatus.OK.value()),
	// 				HttpStatus.OK);
	// 	}
	// 	return new ResponseEntity<>(ResponseStructure.buildResponse(response,
	// 			"Booking fetched sucessfully on ServiceId" + serviceId, HttpStatus.OK, HttpStatus.OK.value()),
	// 			HttpStatus.OK);

	// }
	
	
	@GetMapping("/getBookedServicesByClinicId/{clinicId}")
	public ResponseEntity<ResponseStructure<List<BookingResponse>>> getBookingByClinicId(@PathVariable String clinicId) {

		List<BookingResponse> response = service.bookingByClinicId(clinicId);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null,
					"Clinic  Does not have any booking yet" + clinicId, HttpStatus.OK, HttpStatus.OK.value()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response,
				"Booking fetched sucessfully on clinicId" + clinicId, HttpStatus.OK, HttpStatus.OK.value()),
				HttpStatus.OK);

	}
	
	
	@GetMapping("/booking/customerId/{customerId}")
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getBookingByCustomerId(@PathVariable String customerId) {

		List<Map<String,Object>> response = service.bookingByCustomerId(customerId);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null,
					"Clinic  Does not have any booking yet" + customerId, HttpStatus.OK, HttpStatus.OK.value()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response,
				"Booking fetched sucessfully on clinicId" + customerId, HttpStatus.OK, HttpStatus.OK.value()),
				HttpStatus.OK);

	}
	
	
	@GetMapping("/booking/completed/customerId/{customerId}")
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getCompletedBookingByCustomerId(@PathVariable String customerId) {

		List<Map<String,Object>> response = service.CompletedbookingByCustomerId(customerId);
		if (response == null || response.isEmpty()) {
			return new ResponseEntity<>(ResponseStructure.buildResponse(null,
					"No completed bookings found on customerId" + customerId, HttpStatus.OK, HttpStatus.OK.value()),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(ResponseStructure.buildResponse(response,
				"Booking fetched sucessfully on clinicId" + customerId, HttpStatus.OK, HttpStatus.OK.value()),
				HttpStatus.OK);

	}

	
	@GetMapping("/appointments/patientId/{patientId}")	
	public ResponseEntity<?> getBookingByPatientId(@PathVariable String patientId) {

		return service.bookingByPatientId(patientId);}
	
	@GetMapping("/getBookedServicesByClinicIdWithBranchId/{clinicId}/{branchId}")
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getBookedServicesByClinicIdWithBranchId(
	        @PathVariable String clinicId,
	        @PathVariable String branchId) {

		 List<Map<String,Object>> response = service.getBookedServicesByClinicIdWithBranchId(clinicId, branchId);
	    if (response == null || response.isEmpty()) {
	        return new ResponseEntity<>(ResponseStructure.buildResponse(null,
	                "No bookings found for clinicId: " + clinicId + " and branchId: " + branchId,
	                HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
	    }

	    return new ResponseEntity<>(ResponseStructure.buildResponse(response,
	            "Bookings fetched successfully for clinicId: " + clinicId + " and branchId: " + branchId,
	            HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
	}

//	@PutMapping("/updateAppointment")
//	public ResponseEntity<?> updateAppointment(@RequestBody BookingResponse bookingResponse ){
//		return service.updateAppointment(bookingResponse);
//	
//	}
	
	
	@GetMapping("/getAppointmentByPatientId/{patientId}")
	public ResponseEntity<?> getAppointmentByPatientId(@PathVariable String patientId){
		return service.getAppointsByPatientId(patientId);
	
	}
	
	@GetMapping("/in-progress/appointments/{patientId}/{bookingId}")
	public ResponseEntity<?> getInProgressAppointmentByPatientIdAndBookingId(@PathVariable String patientId,@PathVariable String bookingId){
		  List<BookingResponse> response = service.bookingByPatientIdAndBookingId(patientId,bookingId);
		    if (response == null || response.isEmpty()) {
		        return new ResponseEntity<>(ResponseStructure.buildResponse(null,
		                "No bookings found",
		                HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
		    }

		    return new ResponseEntity<>(ResponseStructure.buildResponse(response,
		            "Bookings fetched successfully",
		            HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);
	}
	
	
	@GetMapping("/getAppointsByInput/{input}")
	public ResponseEntity<?> getAppointsByInput(@PathVariable String input){
		return service.getAppointsByInput(input);
	
	}
	
	
	@GetMapping("/getTodayDoctorAppointmentsByDoctorId/{clinicId}/{doctorId}")
	public ResponseEntity<?> getTodayDoctorAppointmentsByDoctorId(@PathVariable String clinicId,@PathVariable String doctorId){
		return service.getTodayDoctorAppointmentsByDoctorId(clinicId, doctorId);
	
	}
	
	@GetMapping("/filterDoctorAppointmentsByDoctorId/{clinicId}/{doctorId}/{number}")
	public ResponseEntity<?> filterDoctorAppointmentsByDoctorId(@PathVariable String clinicId,@PathVariable String doctorId,@PathVariable String number){
		return service.filterDoctorAppointmentsByDoctorId(clinicId, doctorId, number);
	
	}
	
	@GetMapping("/getCompletedApntsByDoctorId/{clinicId}/{doctorId}")
	public ResponseEntity<?> filterDoctorAppointmentsByDoctorId(@PathVariable String clinicId,@PathVariable String doctorId){
		return service.getCompletedApntsByDoctorId(clinicId, doctorId);
	
	}
	
	@GetMapping("/getSizeOfConsultationTypesByDoctorId/{clinicId}/{doctorId}")
	public ResponseEntity<?> getSizeOfConsultationTypesByDoctorId(@PathVariable String clinicId,@PathVariable String doctorId){
		return service.getSizeOfConsultationTypesByDoctorId(clinicId, doctorId);
	
	}
	
	//---------------------------to get patientdetails by bookingId,pateintId,mobileNumber---------------------------
		@GetMapping("getPatientDetailsForConsetForm/{bookingId}/{patientId}/{mobileNumber}")
		public ResponseEntity<Response>getPatientDetailsForConsentFor(@PathVariable String bookingId,@PathVariable String patientId,@PathVariable String mobileNumber){
			Response response =service.getPatientDetailsForConsetForm(bookingId, patientId, mobileNumber);
			return  ResponseEntity.status(response.getStatus()).body(response);
	}
		
		@GetMapping("/getInProgressAppointments/{mobilenumber}")
		public ResponseEntity<?> inProgressAppointments(@PathVariable String mobilenumber)
		{
			return service.getInProgressAppointments(mobilenumber);
		}
		
		
		@GetMapping("/getDoctorFutureAppointments/{doctorId}")
		public ResponseEntity<?> getDoctorFutureAppointments(@PathVariable String doctorId)
		{
			return service.getDoctorFutureAppointments(doctorId);
		}	
		
		
		@GetMapping("/appointments/byIds/{clinicId}/{branchId}")
		public ResponseEntity<?> retrieveOneWeekAppointments(@PathVariable String clinicId,@PathVariable String branchId)
		{
			return service.retrieveOneWeekAppointments(clinicId, branchId);
		}
		
		@GetMapping("/appointments/Inprogress/{customerId}")
		public ResponseEntity<?> getInprogressAppointmentsByCustomerId(@PathVariable String customerId)
		{
			return service.getInProgressAppointmentsByCustomerId(customerId);
		}
		
		@GetMapping("/appointments/Inprogress/patientId/{patientId}/{clinicId}")
		public ResponseEntity<?> getInprogressAppointmentsByPatientId(@PathVariable String patientId,@PathVariable String clinicId )
		{
			return service.getInProgressAppointmentsByPatientId(patientId,clinicId);
		}
		
		@GetMapping("/appointments/byIdsAndDate/{clinicId}/{branchId}/{date}")
		public ResponseEntity<?> retrieveAppointnmentsByServiceDate(@PathVariable String clinicId,@PathVariable String branchId,@PathVariable String date)
		{
			return service.retrieveAppointments(clinicId, branchId, date);
		}
		
		
		@PutMapping("/update/bookingId")
		public ResponseEntity<ResponseStructure<BookingResponse>> updateAppointmentBasedOnBookingId(@RequestBody BookingResponse bookingResponse ){
			return service.updateAppointmentBasedOnBookingId(bookingResponse);
		}
		
		@GetMapping("/appointments/FilterbyRelation/{customerId}")
		public ResponseEntity<?> retrieveAppointnmentsByRelation(@PathVariable String customerId)
		{
			return service.getRelationsByCustomerId(customerId);
		}

		
		@PostMapping("/appointments/serviceDate/serviceTime/DoctorId")
		public BookingResponse blockingSlot(@RequestBody TempBlockingSlot temp)
		{
			return service.checkBookingByDateAndTime(temp.getServiceDate(), temp.getServicetime(), temp.getDoctorId());
		}
		
		@GetMapping("/report/{clinicId}/{branchId}/{number}/{startDate}/{endDate}")
		public ResponseEntity<Response> getReport(
				@PathVariable String clinicId,
				@PathVariable String branchId,
				@PathVariable Integer number,
				@PathVariable String startDate,
				@PathVariable String endDate) {

		    return service.getPatientAndPriceInfo(clinicId, branchId, number, startDate, endDate);
		}
		
		
		@PostMapping("/bookPhysioAppointment")
		public  ResponseEntity<?> bookPhysioAppointment(@RequestBody BookingRequset req) {
			return service.physioAppointment(req);}
		
		
		  // ✅ API 1
	    @GetMapping("/customDate/{clinicId}/{branchId}/{date}")
	    public ResponseEntity<Response> getCustomDateBookings(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
                @PathVariable String date) {

            return service.getCustomDateBookings(clinicId, branchId,date);
	    }

	    // ✅ API 1
	    @GetMapping("/filter/status/{clinicId}/{branchId}")
	    public ResponseEntity<Response> getFilteredBookingsByStatus(
	            @PathVariable String clinicId,
	            @PathVariable String branchId) {

	        return service.getFilteredBookingsByStatus(clinicId, branchId);
	    }

	    // ✅ API 2
	    @GetMapping("/upcoming/{clinicId}/{branchId}/{option}")
	    public ResponseEntity<Response> getUpcomingBookings(
	    		  @PathVariable String clinicId,
	    		  @PathVariable String branchId,
	    		  @PathVariable int option) {

	        return service.getUpcomingBookings(clinicId, branchId, option);
	    }
	    
//	    @GetMapping("/basedOnDate/{clinicId}/{branchId}/{date}")
//	    public ResponseEntity<Response> getPhysioBookingBasedOnDate(
//	            @PathVariable String clinicId,
//	            @PathVariable String branchId,
//	            @PathVariable String date) {
//
//	        return service.getBookingByDate(clinicId, branchId, date);
//	    }
	    
	    @GetMapping("/customeRange/{clinicId}/{branchId}/{start}/{end}")
	    public ResponseEntity<Response> getPhysioBookingsByCustomeRange(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
	            @PathVariable String start,
	            @PathVariable String end) {

	        return service.getBookingByCustomRange(clinicId, branchId,start, end);
	    }
	    
	    @GetMapping("/getBookingById/{bookingId}")
	    public ResponseEntity<Response> getBookingById(@PathVariable String bookingId) {
	        return service.getBookingById(bookingId);
	    }
	    
	    @GetMapping("/deleteReport/{bookingId}/{index}")
	    public void deleteReport(@PathVariable String bookingId,@PathVariable String index) {
	        service.deleteBookedServiceReports(bookingId,index);
	    } 
	    
	    @GetMapping("/reports/patientId/{patientId}")
	    public ResponseEntity<Response> getReportsByPatientId(@PathVariable String patientId) {

	        List<ReportsDTO> reports = service.getReportsByPatientId(patientId);

	        if (reports == null || reports.isEmpty()) {
	            Response response = Response.builder()
	                    .success(false)
	                    .message("No reports found for given patientId")
	                    .status(HttpStatus.OK.value())
	                    .build();

	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        }

	        Response response = Response.builder()
	                .success(true)
	                .data(reports)
	                .message("Reports fetched successfully")
	                .status(HttpStatus.OK.value())
	                .build();

	        return ResponseEntity.ok(response);
	    }
			
	    @GetMapping("/getDoctorAppointmentsonStatus/{clinicId}/{branchId}/{doctorId}/{status}")
		public ResponseEntity<?> getDoctorAppointmentsonStatus(@PathVariable String clinicId,@PathVariable String branchId,
			@PathVariable String doctorId,@PathVariable String status)
		{
			return service.getBookedServicesByClinicIdWithBranchIdAnddoctorIdAndStatus(clinicId, branchId, doctorId, status);
		}	
	    
	    @GetMapping("/searchBookings/{clinicId}/{input}")
	    public ResponseEntity<?> searchBookings(
	            @PathVariable String clinicId,
	            @PathVariable String input) {

	        try {

	            List<Map<String, Object>> data =
	            		service.searchBookings(clinicId, input);

	            return ResponseEntity.ok(
	                    ResponseStructure.buildResponse(
	                            data,
	                            "Bookings fetched successfully",
	                            HttpStatus.OK,
	                            200));

	        } catch (IllegalArgumentException e) {

                return ResponseEntity.badRequest().body(
                        ResponseStructure.buildResponse(
                                new ArrayList<>(), // Empty array instead of null
                                e.getMessage(),
                                HttpStatus.BAD_REQUEST,
                                400));
            }}
	    
	    @PostMapping("/physio-appointments/import-excel")
	    public ResponseEntity<Response> importPhysioAppointmentsFromExcel(
	            @RequestParam("file") MultipartFile file) {

	        Response response =
	                service.importPhysioAppointmentsFromExcel(file);

	        return ResponseEntity
	                .status(response.getStatus())
	                .body(response);
	    }
  }