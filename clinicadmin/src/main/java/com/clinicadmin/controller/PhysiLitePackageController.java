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

import com.clinicadmin.dto.PhysiLitePackageDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PhysiLitePackageService;

@RestController
@RequestMapping("/clinic-admin")
public class PhysiLitePackageController {

	@Autowired
	private PhysiLitePackageService physiLitePackageService;

	// CREATE (clinicId & branchId supplied via path since DTO does not carry them)
	@PostMapping("/physilite-package/create/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> createPackage(
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@RequestBody PhysiLitePackageDTO dto) {

		Response response = physiLitePackageService.createPackage(clinicId, branchId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// UPDATE (scoped by clinicId & branchId to avoid cross-clinic updates)
	@PutMapping("/physilite-package/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> updatePackage(
			@PathVariable String id,
			@PathVariable String clinicId,
			@PathVariable String branchId,
			@RequestBody PhysiLitePackageDTO dto) {

		Response response = physiLitePackageService.updatePackage(id, clinicId, branchId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// DELETE (scoped by clinicId & branchId)
	@DeleteMapping("/physilite-package/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> deletePackage(
			@PathVariable String id,
			@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physiLitePackageService.deletePackage(id, clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET single record by id (scoped by clinicId & branchId)
	@GetMapping("/physilite-package/{id}/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getPackageById(
			@PathVariable String id,
			@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physiLitePackageService.getPackageById(id, clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET all records for a clinicId + branchId
	@GetMapping("/physilite-package/clinic/{clinicId}/branch/{branchId}")
	public ResponseEntity<Response> getAllPackagesByClinicIdAndBranchId(
			@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = physiLitePackageService.getAllPackagesByClinicIdAndBranchId(clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// GET all records for a clinicId only (across all branches)
	@GetMapping("/physilite-package/clinic/{clinicId}")
	public ResponseEntity<Response> getAllPackagesByClinicId(@PathVariable String clinicId) {

		Response response = physiLitePackageService.getAllPackagesByClinicId(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	// DELETE by packageId (scoped by clinicId & branchId)
		@DeleteMapping("/physilite-package/package/{packageId}/clinic/{clinicId}/branch/{branchId}")
		public ResponseEntity<Response> deletePackageByPackageId(
				@PathVariable String packageId,
				@PathVariable String clinicId,
				@PathVariable String branchId) {
			Response response = physiLitePackageService.deletePackageByPackageId(packageId, clinicId, branchId);
			return ResponseEntity.status(response.getStatus()).body(response);
		}
}