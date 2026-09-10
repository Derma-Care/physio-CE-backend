package com.clinicadmin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapistRecordDTO;
import com.clinicadmin.dto.TherapistRecordRequest;
import com.clinicadmin.service.TherapistRecordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class TherapistRecordController {

	@Autowired
	private TherapistRecordService service;

	// POST API
	@PostMapping("/saveRecord")
	public ResponseEntity<ResponseStructure<TherapistRecordDTO>> saveRecord(@RequestBody TherapistRecordDTO dto) {

		ResponseStructure<TherapistRecordDTO> response = service.saveRecord(dto);

		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	// GET API
	@GetMapping("/getRecordByClinicIdBranchIdtherapistRecordIdAndSessionId/{clinicId}/{branchId}/{therapistRecordId}/{sessionId}")
	public ResponseEntity<ResponseStructure<TherapistRecordDTO>> getRecordByClinicIdBranchIdtherapistRecordIdAndSessionId(
			@PathVariable String clinicId, @PathVariable String branchId, @PathVariable String therapistRecordId,
			@PathVariable String sessionId) {

		ResponseStructure<TherapistRecordDTO> response = service.getByIds(clinicId, branchId, therapistRecordId,
				sessionId);

		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@GetMapping("/getByPatientIdAndBookingId/{patientId}/{bookingId}")
	public ResponseEntity<ResponseStructure<List<TherapistRecordDTO>>> getByPatientIdAndBookingId(
			@PathVariable String patientId, @PathVariable String bookingId) {

		return ResponseEntity.ok(service.getByPatientIdAndBookingId(patientId, bookingId));
	}

	@GetMapping("/getRecordBySession/{clinicId}/{branchId}/{bookingId}/{patientId}/{sessionId}")
	public ResponseEntity<ResponseStructure<TherapistRecordDTO>> getRecordBySession(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String bookingId, @PathVariable String patientId,
			@PathVariable String sessionId) {

		return ResponseEntity.ok(service.getBySession(clinicId, branchId, bookingId, patientId, sessionId));
	}

	@GetMapping("/getCompletedTherapyRecord/{clinicId}/{branchId}/{therapistRecordId}/{sessionId}")
	public ResponseEntity<ResponseStructure<TherapistRecordDTO>> getCompletedTherapyRecord(
			@PathVariable String clinicId, @PathVariable String branchId, @PathVariable String therapistRecordId,
			@PathVariable String sessionId) {

		return ResponseEntity.ok(service.getCompletedTherapyRecord(clinicId, branchId, therapistRecordId, sessionId));
	}

	@PostMapping("/therapist-session-details")
	public ResponseEntity<Response> getTherapistSessionDetails(@RequestBody TherapistRecordRequest request) {

		Response response = service.getTherapistSessionDetails(request);

		return ResponseEntity.status(response.getStatus()).body(response);
	}

}