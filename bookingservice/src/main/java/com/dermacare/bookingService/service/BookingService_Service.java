package com.dermacare.bookingService.service;

import java.util.List;
import java.util.Map;

import com.dermacare.bookingService.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.dermacare.bookingService.util.Response;
import com.dermacare.bookingService.util.ResponseStructure;

public interface BookingService_Service {

	public ResponseEntity<?> addService(BookingResponse req);
	public BookingResponse deleteService(String id);
	public  Map<String, Object>  getBookedService(String bookingId) ;
	public List<BookingResponse> getBookedServices(String mobileNumber);
	public List<BookingResponse> getAllBookedServices();
	public List<BookingResponse> bookingByDoctorId(String doctorId);
	////public List<BookingResponse> bookingByServiceId(String serviceId);
	public List<BookingResponse> bookingByClinicId(String clinicId);
	//public ResponseEntity<?> updateAppointment(BookingResponse bookingResponse);
	public List<BookingResponse> bookingByBranchId(String branchId);
	public ResponseEntity<?> getAppointsByPatientId(String patientId);
	public ResponseEntity<?> getAppointsByInput(String input);
	public ResponseEntity<?> getTodayDoctorAppointmentsByDoctorId(String hospitalId,String doctorId);
	public ResponseEntity<?> filterDoctorAppointmentsByDoctorId(String hospitalId,String doctorId,String number);
	public ResponseEntity<?> getCompletedApntsByDoctorId(String hospitalId,String doctorId);
	public ResponseEntity<?> getSizeOfConsultationTypesByDoctorId(String hospitalId,String doctorId);
	public Response getPatientDetailsForConsetForm(String bookingId, String patientId, String mobileNumber);
	public ResponseEntity<?> getInProgressAppointments(String number);
	public ResponseEntity<?> retrieveOneWeekAppointments(String cinicId,String branchId);								
	public ResponseEntity<?> getDoctorFutureAppointments(String doctorId);
	public List<Map<String,Object>> getBookedServicesByClinicIdWithBranchId(String clinicId, String branchId);
	public ResponseEntity<?> retrieveAppointments(String cinicId,String branchId,String date);
	public ResponseEntity<ResponseStructure<BookingResponse>> updateAppointmentBasedOnBookingId(BookingResponse dto);
	public ResponseEntity<?> getRelationsByCustomerId(String customerId);
	public List<Map<String,Object>> bookingByCustomerId(String customerId);
	public ResponseEntity<?> bookingByPatientId(String patientId);
	//public BookingInfoByInput bookingByInput(String input,String clinicId);
	public ResponseEntity<?> getInProgressAppointmentsByCustomerId(String customerId);
	public ResponseEntity<?> getInProgressAppointmentsByPatientId(String patientId,String clinicId);
	public BookingResponse checkBookingByDateAndTime(String date,String time,String doctorId);
	public ResponseEntity<Response> getPatientAndPriceInfo(
	        String clinicId,
	        String branchId,
	        Integer number,
	        String startDate,
	        String endDate);

public List<Map<String,Object>> getTodayBookings(String cId,String bId);
public ResponseEntity<?> physioAppointment(BookingRequset request);
public ResponseEntity<Response> getCustomDateBookings(String clinicId, String branchId, String date);

    public ResponseEntity<Response> getUpcomingBookings(String clinicId,
        String branchId,
        int option);

//public ResponseEntity<Response> getBookingByDate(String clinicId, String branchId,String date);
public ResponseEntity<Response> getBookingByCustomRange(String clinicId, String branchId,String start,String end);
public ResponseEntity<Response> getBookingById(String bookingId);
public List<BookingResponse> bookingByPatientIdAndBookingId(String patientId,String bookingId);
public List<ReportsDTO> getReportsByPatientId(String patientId);
public void deleteBookedServiceReports(String bookingId,String index);
public ResponseEntity<?> getBookedServicesByClinicIdWithBranchIdAnddoctorIdAndStatus(
        String clinicId,
        String branchId,
        String doctorId,
        String status);

public List<Map<String, Object>> CompletedbookingByCustomerId(String customerId);
List<Map<String, Object>> searchBookings(String clinicId, String input);
public ResponseEntity<Response> getFilteredBookingsByStatus(
        String clinicId,
        String branchId);
Response importPhysioAppointmentsFromExcel(MultipartFile file);


}
