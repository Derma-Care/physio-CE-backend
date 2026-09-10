package com.clinicadmin.controller;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.SoapNoteDTO;
import com.clinicadmin.service.SoapNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clinic-admin")
public class SoapNoteController {

	@Autowired
	private SoapNoteService soapNoteService;

	@PostMapping("/soap-notes/create")
	public ResponseEntity<Response> createSoapNote(@RequestBody SoapNoteDTO dto) {
		Response response = soapNoteService.createSoapNote(dto);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@PutMapping("/soap-notes/{id}")
	public ResponseEntity<Response> updateSoapNote(@PathVariable String id, @RequestBody SoapNoteDTO dto) {
		Response response = soapNoteService.updateSoapNote(id, dto);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/{id}")
	public ResponseEntity<Response> getSoapNoteById(@PathVariable String id) {
		Response response = soapNoteService.getSoapNoteById(id);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/getAll")
	public ResponseEntity<Response> getAllSoapNotes() {
		Response response = soapNoteService.getAllSoapNotes();
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/patient/{patientId}")
	public ResponseEntity<Response> getSoapNotesByPatientId(@PathVariable String patientId) {
		Response response = soapNoteService.getSoapNotesByPatientId(patientId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/doctor/{doctorId}")
	public ResponseEntity<Response> getSoapNotesByDoctorId(@PathVariable String doctorId) {
		Response response = soapNoteService.getSoapNotesByDoctorId(doctorId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/booking/{bookingId}")
	public ResponseEntity<Response> getSoapNotesByBooking(@PathVariable String bookingId) {
		Response response = soapNoteService.getSoapNotesByBooking(bookingId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@DeleteMapping("/soap-notes/{id}")
	public ResponseEntity<Response> deleteSoapNoteById(@PathVariable String id) {
		Response response = soapNoteService.deleteSoapNoteById(id);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	// -------------------- New: clinic / branch combined lookups
	// --------------------

	@GetMapping("/soap-notes/clinic/{clinicId}")
	public ResponseEntity<Response> getSoapNotesByClinicId(@PathVariable String clinicId) {
		Response response = soapNoteService.getSoapNotesByClinicId(clinicId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getSoapNotesByClinicIdAndBranchId(@PathVariable String clinicId,
			@PathVariable String branchId) {
		Response response = soapNoteService.getSoapNotesByClinicIdAndBranchId(clinicId, branchId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/clinic/{clinicId}/branch/{branchId}/doctor/{doctorId}")
	public ResponseEntity<Response> getSoapNotesByClinicBranchAndDoctor(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String doctorId) {
		Response response = soapNoteService.getSoapNotesByClinicBranchAndDoctor(clinicId, branchId, doctorId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/clinic/{clinicId}/branch/{branchId}/patient/{patientId}")
	public ResponseEntity<Response> getSoapNotesByClinicBranchAndPatient(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String patientId) {
		Response response = soapNoteService.getSoapNotesByClinicBranchAndPatient(clinicId, branchId, patientId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/clinic/{clinicId}/branch/{branchId}/booking/{bookingId}")
	public ResponseEntity<Response> getSoapNotesByClinicBranchAndBooking(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String bookingId) {
		Response response = soapNoteService.getSoapNotesByClinicBranchAndBooking(clinicId, branchId, bookingId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	@GetMapping("/soap-notes/clinic/{clinicId}/branch/{branchId}/booking/{bookingId}/patient/{patientId}")
	public ResponseEntity<Response> getSoapNotesByClinicBranchAndPatient(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String bookingId, @PathVariable String patientId) {
		Response response = soapNoteService.getSoapNotesByClinicBranchAndPatient(clinicId, branchId, bookingId,
				patientId);
		return ResponseEntity.status(resolveStatus(response)).body(response);
	}

	// -------------------- HELPER: map Response.status (int) to a valid HttpStatus
	// --------------------
	private HttpStatus resolveStatus(Response response) {
		HttpStatus status = HttpStatus.resolve(response.getStatus());
		return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
	}
}