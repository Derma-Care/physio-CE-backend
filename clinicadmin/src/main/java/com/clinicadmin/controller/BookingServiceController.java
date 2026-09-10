package com.clinicadmin.controller;

import com.clinicadmin.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clinicadmin.service.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BookingServiceController {

	@Autowired
	BookingService bookingService; 
	
	@GetMapping("/getAllbookingsDetailsByBranchId/{branchId}")
	public ResponseEntity<Response> getAllbookingsDetailsByBranchId(@PathVariable String branchId) {
		Response response = bookingService.getAllBookedServicesDetailsByBranchId(branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	@GetMapping("/getAllbookingsDetailsByClinicAndBranchId/{clinicId}/{branchId}")
	public ResponseEntity<?> getAllbookingsDetailsByClinicAndBranchId(@PathVariable String clinicId,@PathVariable String branchId) {
		return bookingService.getBookingsByClinicIdWithBranchId(clinicId,branchId);
		
	}
	
	
	@GetMapping("/appointments/byIds/{clinicId}/{branchId}")
	public ResponseEntity<?> retrieveOneWeekAppointments(@PathVariable String clinicId,@PathVariable String branchId) {
		return bookingService.retrieveOneWeekAppointments(clinicId, branchId);
	
	}
	
	
	@GetMapping("/appointments/byIdsAndDate/{clinicId}/{branchId}/{date}")
	public ResponseEntity<?> retrieveAppointnmentsByServiceDate(@PathVariable String clinicId,@PathVariable String branchId,@PathVariable String date) {
	return bookingService.retrieveAppointnmentsByServiceDate(clinicId, branchId, date);		
	}
	
	
	@PutMapping("/updateAppointmentBasedOnBookingId")
	public ResponseEntity<?> updateAppointmentBasedOnBookingId(@RequestBody BookingResponse bookingResponse) {
		return bookingService.updateAppointmentBasedOnBookingId(bookingResponse);
		
	}

	   @GetMapping("/bookings/byPatientId/{patientId}")
	   public ResponseEntity<?> getInprogressBookingsByPatientId(
				 @PathVariable String patientId){
		   return bookingService.retrieveAppointnmentsByPatientId(patientId);
		   
	 }
	   
	   @GetMapping("/in-progress/PatientId/bookingId/{patientId}/{bookingId}")
	   public ResponseEntity<?> getInprogressBookingsByPatientId(
				 @PathVariable String patientId, @PathVariable String bookingId){
		   return bookingService.getInProgressBookingsByIds(patientId, bookingId);
		   
	 }
	   
	   @GetMapping("/reports/patientId/{patientId}")
	    public ResponseEntity<?> getReportsByPatientId(@PathVariable String patientId){
	    return bookingService.getReportsByPatientId(patientId);}


	   @PostMapping("/bookService")
	   public ResponseEntity<Object> bookService(@RequestBody BookingResponse req)throws JsonProcessingException  {
	   	Response response = bookingService.bookService(req);
	   	if(response != null && response.getData() == null) {
	   		 return ResponseEntity.status(response.getStatus()).body(response);
	   	 }else if(response != null && response.getData() != null) {
	   		 return ResponseEntity.status(response.getStatus()).body(response.getData());}
	   		 else {
	   			 return null;
	   		 }
	   	}
	   
	   @GetMapping("/bookings/Inprogress/patientId/{patientId}")
	   public ResponseEntity<?> getInprogressAppointmentsByPatientId(
				 @PathVariable String patientId){
		   return bookingService.getInprogressBookingsByPatientId(patientId);
	 }
	   @GetMapping("/bookings/Inprogress/patientId/{patientId}/{clinicId}")
	   public ResponseEntity<?> getInprogressAppointmentsByPatientIdAndClinicId(
	           @PathVariable String patientId,
	           @PathVariable String clinicId) {

	       return bookingService.getInprogressBookingsByPatientIdAndClinicId(patientId, clinicId);
	   }
	   
	   @PostMapping("/physioAppointment")
	   public ResponseEntity<?> physioAppointment(
			   @RequestBody BookingRequset req){
		   return bookingService.physioAppointment(req);
	 }
	   
	   @GetMapping("/reprts/{clinicId}/{branchId}/{number}/{startDate}/{endDate}")
	   public ResponseEntity<?> getReprts(@PathVariable String clinicId,
			   @PathVariable String branchId,
			   @PathVariable Integer number,
			   @PathVariable  String startDate,
			   @PathVariable String endDate) {

	       return bookingService.getReprts(clinicId, branchId, number, startDate, endDate);
	   }

    @GetMapping("/customDate/{clinicId}/{branchId}/{date}")
    public ResponseEntity<?> getCustomBookings(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String date ) {

        return bookingService.getCustomBookings(clinicId, branchId,date);
    }

	   
	   @GetMapping("/date/{clinicId}/{branchId}/{date}")
	    public ResponseEntity<?> getBookingsByDate(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,         
	            @PathVariable String date) {

	        return bookingService.getBookingsByDate(clinicId, branchId,date);
	    }
	   
	   @GetMapping("/dateRange/{clinicId}/{branchId}/{start}/{end}")
	    public ResponseEntity<?> getBookingsByDateRange(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
	            @PathVariable String start,
	            @PathVariable String end) {

	        return bookingService.getBookingsByDateRange(clinicId, branchId, start, end);
	    }
	   
	   
	   @GetMapping("/getBookingById/{bookingId}")
	    public ResponseEntity<?> getBookingById(
	            @PathVariable String bookingId) {

	        return bookingService.getBookingById(bookingId);
	    }
	   
	   @GetMapping("/getTodayBookingsByClinicIdAndBranchId/{clinicId}/{branchId}")
	    public ResponseEntity<?> getTodayBookingsByClinicIdAndBranchId(
	            @PathVariable String clinicId, @PathVariable String branchId) {

	        return bookingService.getTodayBookingsByClinicIdAndBranchId(clinicId, branchId);
	    }
	    
	    /**
	     * ✅ API 2: Get Upcoming Bookings
	     * option:
	     * 1 → next 3 days
	     * 2 → next 7 days
	     *
	     * URL Example:
	     * /api/physio/bookings/upcoming/CL001/BR001/1
	     */
	    @GetMapping("/upcoming/{clinicId}/{branchId}/{option}")
	    public ResponseEntity<?> getUpcomingBookings(
	            @PathVariable String clinicId,
	            @PathVariable String branchId,
	            @PathVariable int option) {

	        // ✅ Optional validation (recommended)
	        if (option != 1 && option != 2) {
	            return ResponseEntity.badRequest()
	                    .body("Option must be 1 (3 days) or 2 (7 days)");
	        }

	        return bookingService.getUpcomingBookings(clinicId, branchId, option);
	    }
	    
	    
	    @GetMapping("/getBookedServiceById/{id}")
		public ResponseEntity<?> getBookedService(@PathVariable String id) {
			return bookingService.getBookedServiceById(id);
		
		}
	    
	        @DeleteMapping("/deleteBookedServiceById/{id}")
	 		public ResponseEntity<?> deleteBookedServiceById(@PathVariable String id) {
	 			return bookingService.deleteBookedService(id);
	 		
	 		}
	    
	    @GetMapping("/filter/status/{clinicId}/{branchId}")
	    public ResponseEntity<?> getFilteredBookingsByStatus(
	            @PathVariable String clinicId,
	            @PathVariable String branchId){
	    	return bookingService.getFilteredBookingsByStatus(clinicId,branchId);
	 		
	 		}

}
