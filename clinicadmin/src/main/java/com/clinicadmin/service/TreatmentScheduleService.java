package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.*;
import org.springframework.http.ResponseEntity;

public interface TreatmentScheduleService {
	Response createFromSoapNote(String clinicId, String branchId, String bookingId, String patientId);
	Response getTreatmentSchedule(String clinicId, String branchId, String bookingId, String patientId);
//	Response bookSitting(String scheduleId, Integer sittingNumber, String doctorId, String patientId,
//			String date, String slot, String bookingId);
//	Response bookSitting(String scheduleId, String sittingsId, Integer sittingNumber, String doctorId,
//            String patientId, String date, String slot, String bookingId,
//            List<PhysioMaxTreatedDoctor> treatedBy);
	Response getTreatmentScheduleUsingClinicIdBranchId(String clinicId, String branchId, String bookingId,
			String patientId);

    public ResponseEntity<Response> getDoctorAnalytics(String clinicId, String branchId, String fromDateStr, String toDateStr);
    public ResponseEntity<Response> getAnalytics(String clinicId, String branchId, int filter) ;
    Response completeSitting(String clinicId, String branchId, String sittingsId, Integer sittingNumber, String status);
    public ResponseEntity<Response> getDoctorSummary(String clinicId, String branchId, String doctorId, int filter) ;
    public ResponseEntity<Response> getPatientSummary(  String clinicId,
                                                        String branchId,
                                                        String doctorId,
                                                        String patientId
                                                      ) ;
	Response addSitting(String clinicId, String branchId, String bookingId, String patientId);
	Response deleteSitting(String scheduleId, String sittingsId);
	public TreatmentScheduleDTO getFilteredSittings(String clinicId, String branchId, String bookingId,
                                                 String startDate, String endDate);

	public List<TreatmentScheduleDTO> getSittingsBasedOnDates(String clinicId, String branchId,
	                                                          String startDate, String endDate,List<String> ids);
	Response bookSitting(String scheduleId, String sittingsId, Integer sittingNumber, String doctorId, String patientId,String condition,
			String date, String slot, String bookingId, List<PhysioMaxTreatedDoctor> treatedBy, String treatmentPlan,
			String visitType);

	public ResponseEntity<Response> getHomeSittings(
			String clinicId,
			String branchId,
			Integer number);

	public ResponseEntity<Response> getHomeSittingsByDoctor(
			String clinicId,
			String branchId,
			String doctorId,
			Integer number);


	public ResponseEntity<Response> updateHomeVisitStatus(String clinicId, String branchId,
	                                                      String sittingsId,
	                                                      HomeVisitTrackingRequest request);

	public ResponseEntity<Response> getHomeVisit(
			String clinicId,
			String branchId,
			String sittingsId,
			Integer number);


	public ResponseEntity<Response> updateSittingTreatment(
			String clinicId,
			String branchId,
			String sittingId,
			UpdateSittingTreatmentRequest request);

    }