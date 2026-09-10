package com.clinicadmin.controller;

import java.util.List;

import com.clinicadmin.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.service.TreatmentScheduleService;

@RestController
@RequestMapping("/clinic-admin")
public class TreatmentScheduleController {

	@Autowired
	private TreatmentScheduleService treatmentScheduleService;

	// -------------------- CREATE (manual/re-create for an existing SOAP note)
	// --------------------
	@PostMapping("/treatment-schedule/create/{clinicId}/{branchId}/{bookingId}/{patientId}")
	public ResponseEntity<Response> createFromSoapNote(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String bookingId, @PathVariable String patientId) {
		Response response = treatmentScheduleService.createFromSoapNote(clinicId, branchId, bookingId, patientId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	// -------------------- GET (fetch schedule by clinic/branch/booking/patient)
	// --------------------
	@GetMapping("/treatment-schedule/{clinicId}/{branchId}/{bookingId}/{patientId}")
	public ResponseEntity<Response> getTreatmentSchedule(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String bookingId, @PathVariable String patientId) {
		Response response = treatmentScheduleService.getTreatmentSchedule(clinicId, branchId, bookingId, patientId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	// -------------------- BOOK A SPECIFIC SITTING --------------------
//	@PutMapping("/treatment-schedule/{scheduleId}/sittings/{sittingNumber}/book/{doctorId}/{patientId}/{date}/{slot}/{bookingId}")
//	public ResponseEntity<Response> bookSitting(
//			@PathVariable String scheduleId,
//			@PathVariable Integer sittingNumber,
//			@PathVariable String doctorId,
//			@PathVariable String patientId,
//			@PathVariable String date,
//			@PathVariable String slot,
//			@PathVariable String bookingId) {
//		Response response = treatmentScheduleService.bookSitting(
//				scheduleId, sittingNumber, doctorId, patientId, date, slot, bookingId);
//		return ResponseEntity.status(resolveStatus(response)).body(response);
//	}

	// -------------------- HELPER: map Response.status (int) to a valid HttpStatus
	// --------------------
	private HttpStatus resolveStatus(Response response) {
		HttpStatus status = HttpStatus.resolve(response.getStatus());
		return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@PutMapping("/treatment-schedule/book-sitting")
	public ResponseEntity<Response> bookSitting(@RequestBody BookSittingRequest request) {
		Response response = treatmentScheduleService.bookSitting(request.getScheduleId(), request.getSittingsId(),
				request.getSittingNumber(), request.getDoctorId(), request.getPatientId(),request.getCondition(), request.getDate(),
				request.getSlot(), request.getBookingId(), request.getTreatedBy(), request.getTreatmentPlan(),
				request.getVisitType());
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/getTreatmentScheduleUsingClinicIdBranchIdBookingIdAndPatientId/{clinicId}/{branchId}/{bookingId}/{patientId}")
	public ResponseEntity<Response> getTreatmentScheduleUsingClinicAndBranchId(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String bookingId, @PathVariable String patientId) {

		Response response = treatmentScheduleService.getTreatmentSchedule(clinicId, branchId, bookingId, patientId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/treatment-schedule/complete/{clinicId}/{branchId}/{sittingsId}/{sittingNumber}/{status}")
	public ResponseEntity<Response> completeSitting(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String sittingsId, @PathVariable Integer sittingNumber, @PathVariable String status) {

		Response response = treatmentScheduleService.completeSitting(clinicId, branchId, sittingsId, sittingNumber,
				status);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/doctoranalytics/range/{clinicId}/{branchId}/{fromDate}/{toDate}")
	public ResponseEntity<Response> getDoctorAnalytics(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String fromDate, @PathVariable String toDate) {
		return treatmentScheduleService.getDoctorAnalytics(clinicId, branchId, fromDate, toDate);
	}

	@GetMapping("/doctoranalytics/filter/{clinicId}/{branchId}/{filter}")
	public ResponseEntity<Response> getAnalytics(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable int filter) {
		return treatmentScheduleService.getAnalytics(clinicId, branchId, filter);
	}

	@GetMapping("/doctoranalytics/doctorSummary/{clinicId}/{branchId}/{doctorId}/{filter}")
	public ResponseEntity<Response> getDoctorSummary(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String doctorId, @PathVariable int filter) {
		return treatmentScheduleService.getDoctorSummary(clinicId, branchId, doctorId, filter);
	}

	@GetMapping("/doctoranalytics/patientSummary/{clinicId}/{branchId}/{doctorId}/{patientId}")
	public ResponseEntity<Response> getPatientSummary(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String doctorId, @PathVariable String patientId) {
		return treatmentScheduleService.getPatientSummary(clinicId, branchId, doctorId, patientId);
	}

	@PostMapping("/treatment-schedule/add-sitting/{clinicId}/{branchId}/{bookingId}/{patientId}")
	public ResponseEntity<Response> addSitting(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String bookingId, @PathVariable String patientId) {

		Response response = treatmentScheduleService.addSitting(clinicId, branchId, bookingId, patientId);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/treatment-schedule/{scheduleId}/sitting/{sittingsId}")
	public ResponseEntity<Response> deleteSitting(@PathVariable String scheduleId, @PathVariable String sittingsId) {
		Response response = treatmentScheduleService.deleteSitting(scheduleId, sittingsId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@GetMapping("/treatment-schedule/sittings/clinicId/{clinicId}/branchId/{branchId}/bookingId/{bookingId}/dates/{startDate}/{endDate}")
	public TreatmentScheduleDTO getSittings(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String bookingId, @PathVariable String startDate, @PathVariable String endDate) {

		return treatmentScheduleService.getFilteredSittings(clinicId, branchId, bookingId, startDate, endDate);

	}

	@PostMapping("/filtereByDates/{clinicId}/{branchId}/{startDate}/{endDate}")
	public List<TreatmentScheduleDTO> getFilteredSittings(@PathVariable String clinicId, @PathVariable String branchId,
			@PathVariable String startDate, @PathVariable String endDate, @RequestBody List<String> ids) {

		return treatmentScheduleService.getSittingsBasedOnDates(clinicId, branchId, startDate, endDate, ids);

	}


	@GetMapping("/home/clinicId/{clinicId}/branchId/{branchId}/{number}")
	public ResponseEntity<Response> getHomeSittings(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable Integer number) {

		return treatmentScheduleService.getHomeSittings(
				clinicId,
				branchId,
				number
		);
	}

	@GetMapping("/home/clinicId/{clinicId}/branchId/{branchId}/doctorId/{doctorId}/{number}")
	public ResponseEntity<Response> getHomeSittingsByDoctor(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String doctorId,
			@PathVariable Integer number) {

		return treatmentScheduleService.getHomeSittingsByDoctor(
				clinicId,
				branchId,
				doctorId,
				number
		);
	}



	@PutMapping("/updateHomeVisitStatus/{clinicId}/{branchId}/{sittingsId}")
	public ResponseEntity<Response> updateHomeVisitStatus(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String sittingsId,
			@RequestBody HomeVisitTrackingRequest request) {

		return treatmentScheduleService.updateHomeVisitStatus(
				clinicId,
				branchId,
				sittingsId,
				request
		);
	}

	@GetMapping("/fetch/home-visit/{clinicId}/{branchId}/{sittingsId}/{number}")
	public ResponseEntity<Response> getHomeVisit(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String sittingsId,
			@PathVariable Integer number) {

		return treatmentScheduleService.getHomeVisit(
				clinicId,
				branchId,
				sittingsId,
				number
		);
	}


	@PutMapping("/updateSittingTreatment/clinic/{clinicId}/branch/{branchId}/sitting/{sittingId}")
	public ResponseEntity<Response> updateSittingTreatment(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@PathVariable String sittingId,
			@RequestBody UpdateSittingTreatmentRequest request) {

		return treatmentScheduleService.updateSittingTreatment(
				clinicId,
				branchId,
				sittingId,
				request
		);
	}
}
