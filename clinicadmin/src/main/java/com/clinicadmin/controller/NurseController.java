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

import com.clinicadmin.dto.NurseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.NurseService;
import com.clinicadmin.validations.RequiredChecks;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NurseController {

	@Autowired
	private NurseService nurseService;

	// ------------------- Add Nurse -------------------
	@PostMapping("/addNurse")
	public ResponseEntity<Response> nurseOnBoarding(@Validated(RequiredChecks.class) @RequestBody NurseDTO dto) {
		Response response = nurseService.nureseOnboarding(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get All Nurses by Hospital -------------------
	@GetMapping("getAllNurses/{hospitalId}")
	public ResponseEntity<Response> getAllByHospital(@PathVariable String hospitalId) {
		Response response = nurseService.getAllNursesByHospital(hospitalId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get Single Nurse -------------------
	@GetMapping("getNurse/{hospitalId}/{nurseId}")
	public ResponseEntity<Response> getNurse(@PathVariable String hospitalId, @PathVariable String nurseId) {
		Response response = nurseService.getNurseByHospitalAndNurseId(hospitalId, nurseId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Update Nurse -------------------
	@PutMapping("/updateNurse/{nurseId}")
	public ResponseEntity<Response> updateNurse( @PathVariable String nurseId,
			@RequestBody NurseDTO dto) {
		Response response = nurseService.updateNurse(nurseId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Delete Nurse -------------------
	@DeleteMapping("/deleteNurse/{hospitalId}/{nurseId}")
	public ResponseEntity<Response> deleteNurse(@PathVariable String hospitalId, @PathVariable String nurseId) {
		Response response = nurseService.deleteNurse(hospitalId, nurseId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	// ------------------- Get All Nurses by Hospital and Branch -------------------
	@GetMapping("/getAllNursesByBranchIdAndHospiatlId/{hospitalId}/{branchId}")
	public ResponseEntity<Response> getAllNursesByBranch(
	        @PathVariable String hospitalId,
	        @PathVariable String branchId) {
	    Response response = nurseService.getAllNursesByBranchIdAndHospitalId(hospitalId, branchId);
	    return ResponseEntity.status(response.getStatus()).body(response);
	}


	// ---------------------------------------------------------------------------------------------------
	// ---------------------------------
	// NurseLogin-------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------

//	@PostMapping("/nurseLogin")
//	public ResponseEntity<Response> nurseLogin(@RequestBody NurseLoginDTO dto) {
//		Response response = nurseService.nurseLogin(dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
//	@PostMapping("/resetNurseLogin")
//	public ResponseEntity<Response> resetNurseLogin(@RequestBody ResetNurseLoginPasswordDTO dto) {
//		Response response = nurseService.resetLoginPassword(dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
}
