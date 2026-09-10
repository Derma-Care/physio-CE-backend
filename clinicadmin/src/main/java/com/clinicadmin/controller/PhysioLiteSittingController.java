package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PhysiLiteSittingDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PhysioLiteSittingService;

@RestController
@RequestMapping("/clinic-admin")
public class PhysioLiteSittingController {

	@Autowired
	private PhysioLiteSittingService physioLiteSittingService;

	// CREATE (clinicId & branchId are carried inside the DTO body itself)
	@PostMapping("/physiolite-sitting/create")
	public ResponseEntity<Response> createSitting(@RequestBody PhysiLiteSittingDTO dto) {

		Response response = physioLiteSittingService.createSitting(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// UPDATE (scoped by clinicId & branchId to avoid cross-clinic updates)
	@PutMapping("/physiolite-sitting/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> updateSitting(@PathVariable String id, @PathVariable String clinicId,
			@PathVariable String branchId, @RequestBody PhysiLiteSittingDTO dto) {

		Response response = physioLiteSittingService.updateSitting(id, clinicId, branchId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// DELETE (scoped by clinicId & branchId)
	@DeleteMapping("/physiolite-sitting/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> deleteSitting(@PathVariable String id, @PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physioLiteSittingService.deleteSitting(id, clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET single record by id (scoped by clinicId & branchId)
	@GetMapping("/physiolite-sitting/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getSittingById(@PathVariable String id, @PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physioLiteSittingService.getSittingById(id, clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET all records for a clinicId + branchId
	@GetMapping("/physiolite-sitting/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getAllSittingsByClinicIdAndBranchId(@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physioLiteSittingService.getAllSittingsByClinicIdAndBranchId(clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET all records for a clinicId only (across all branches)
	@GetMapping("/physiolite-sitting/clinic/{clinicId}")
	public ResponseEntity<Response> getAllSittingsByClinicId(@PathVariable String clinicId) {

		Response response = physioLiteSittingService.getAllSittingsByClinicId(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	@GetMapping("/physiolite-sitting/sitting/{sittingId}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getSittingBySittingId(
	        @PathVariable String sittingId,
	        @PathVariable String clinicId,
	        @PathVariable String branchId) {

	    Response response = physioLiteSittingService
	            .getSittingBySittingId(sittingId, clinicId, branchId);

	    return ResponseEntity.status(response.getStatus()).body(response);
	}
	// DELETE by sittingId (scoped by clinicId & branchId)
		@DeleteMapping("/physiolite-sitting/sitting/{sittingId}/clinic/{clinicId}/branch/{branchId}")
		public ResponseEntity<Response> deleteSittingBySittingId(
				@PathVariable String sittingId,
				@PathVariable String clinicId,
				@PathVariable String branchId) {
			Response response = physioLiteSittingService.deleteSittingBySittingId(sittingId, clinicId, branchId);
			return ResponseEntity.status(response.getStatus()).body(response);
		}
}