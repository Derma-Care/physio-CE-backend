package com.clinicadmin.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.clinicadmin.dto.BookingRequset;
import com.clinicadmin.dto.BookingResponse;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface BookingService {
	//public Object deleteBookedService(String id);

	Response getAllBookedServicesDetailsByBranchId(String branchId);
	
	public ResponseEntity<ResponseStructure<List<Map<String,Object>>>> getBookingsByClinicIdWithBranchId(String clinicId, String branchId);
	public ResponseEntity<?> retrieveOneWeekAppointments(String clinicId, String branchId);

	public ResponseEntity<?> retrieveAppointnmentsByServiceDate(String clinicId, String branchId,String date);
	
	public ResponseEntity<?> updateAppointmentBasedOnBookingId(BookingResponse response);
 
	//public ResponseEntity<?> retrieveAppointnmentsByInput(String input, String clinicId);

	ResponseEntity<?> retrieveAppointnmentsByPatientId(String patientId);

	Response bookService(BookingResponse req) throws JsonProcessingException;

	ResponseEntity<?> getInprogressBookingsByPatientId(String patientId);
	
	ResponseEntity<?> getInprogressBookingsByPatientIdAndClinicId(String patientId, String clinicId);
		
	public ResponseEntity<?> getReprts(String clinicId,
			String branchId,
			Integer number,
		    String startDate,
			String endDate);

	public ResponseEntity<?> physioAppointment(BookingRequset bookingResponse);

    public ResponseEntity<?> getCustomBookings(String clinicId,
                                               String branchId,String date);
    public ResponseEntity<?> getUpcomingBookings(String clinicId,
			String branchId,int option);
	public ResponseEntity<?> getBookingsByDate(String clinicId,
			String branchId,String date);
	public ResponseEntity<?> getBookingsByDateRange(String clinicId,
			String branchId,String start, String end);
	public ResponseEntity<?> getBookingById(String bookingId);
	public ResponseEntity<?> getTodayBookingsByClinicIdAndBranchId(String clinicId,String branchId);
	public ResponseEntity<?> getInProgressBookingsByIds(String patientId,
			String bookingId);
	public ResponseEntity<?> getReportsByPatientId(String patientId);
	public ResponseEntity<?> getBookedServiceById(String bookingId);
	public ResponseEntity<?> getFilteredBookingsByStatus(String clinicId,String branchId);
	public ResponseEntity<ResponseStructure<BookingResponse>> deleteBookedService(String id);


}
