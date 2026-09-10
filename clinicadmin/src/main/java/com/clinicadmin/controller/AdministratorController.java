package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.AdministratorDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.AdministratorService;
import com.clinicadmin.validations.RequiredChecks;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdministratorController {

	@Autowired
	private AdministratorService administratorService;

	// ------------------- Add Administrator -------------------
	@PostMapping("/addAdministrator")
	public ResponseEntity<Response> addAdministrator(
			@Validated(RequiredChecks.class) @RequestBody AdministratorDTO dto) {
		Response response = administratorService.administratorOnboarding(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get All Administrators by Clinic -------------------
	@GetMapping("/getAllAdministrators/{clinicId}")
	public ResponseEntity<Response> getAllAdministratorsByClinic(@PathVariable String clinicId) {
		Response response = administratorService.getAllAdministratorsByClinic(clinicId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get All Administrators by Clinic -------------------
	@GetMapping("/getAllAdministrators/{clinicId}/{branchId}")
	public ResponseEntity<Response> getAllAdministratorsByClinicAndBranch(@PathVariable String clinicId,
			@PathVariable String branchId) {

		Response response = administratorService.getAllAdministratorsByClinic(clinicId, branchId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ----------------------- Get Administrator ---------------------------------
	@GetMapping("/getAdministrator/{clinicId}/{adminId}")
	public ResponseEntity<Response> getAdministratorById(@PathVariable String clinicId, @PathVariable String adminId) {
		Response response = administratorService.getAdministratorById(clinicId, adminId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	// ----------------------- Get Administrator clinicId,branchId,adminId ---------------------------------

	@GetMapping("/getAdministrator/{clinicId}/{branchId}/{adminId}")
	public ResponseEntity<Response> getAdministratorByClinicIdAndBranchIdAndAdminId(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String adminId) {
		Response response = administratorService.getAdministratorUsingClinicIdAndBranchIdAndAdminId(clinicId, branchId,
				adminId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ---------------------------  Update Administrator  --------------------------------------
	@PutMapping("/updateAdministrator/{clinicId}/{adminId}")
	public ResponseEntity<Response> updateAdministrator(@PathVariable String clinicId, @PathVariable String adminId,
			@RequestBody AdministratorDTO dto) {
		Response response = administratorService.updateAdministrator(clinicId, adminId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	// ------------------- Update Administrator using clinicId,branchId,adminId -------------------
	@PutMapping("/updateAdministrator/{clinicId}/{branchId}/{adminId}")
	public ResponseEntity<Response> updateAdministratorUsingClinicIdAndBranchIdAndAdminId(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String adminId, @RequestBody AdministratorDTO dto) {
		Response response = administratorService.updateAdministratorByClinicIdAndBranchIdAndAdminId(clinicId, branchId,
				adminId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// -----------------------------  Delete Administrator  --------------------------------------
	@DeleteMapping("/deleteAdministrator/{clinicId}/{adminId}")
	public ResponseEntity<Response> deleteAdministrator(@PathVariable String clinicId, @PathVariable String adminId) {
		Response response = administratorService.deleteAdministrator(clinicId, adminId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	// ------------------- Delete Administrator using clinicId,branchId,adminId-------------------
	@DeleteMapping("/deleteAdministrator/{clinicId}/{branchId}/{adminId}")
	public ResponseEntity<Response> deleteAdministratorUsingClinicIdAndBranchIdAndAdminId(@PathVariable String clinicId,
			@PathVariable String branchId, @PathVariable String adminId) {
		Response response = administratorService.deleteAdministratorByClinicIdAndBranchIdAndAdminId(clinicId, branchId,
				adminId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
}
