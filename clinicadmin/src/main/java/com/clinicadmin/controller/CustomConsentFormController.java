package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.CustomConsentFormDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.CustomConsentFormService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CustomConsentFormController {

	@Autowired
	private CustomConsentFormService consentFormService;

	// -------------------------------
	// Add Consent Form
	// -------------------------------
//	@PostMapping("/consent-form/{hospitalId}/{consentFormType}")
//	public ResponseEntity<Response> addConsentForm(@PathVariable String hospitalId, @PathVariable String consentFormType,
//			@RequestBody CustomConsentFormDTO dto) {
//		Response response = consentFormService.addCustomConsentForm(hospitalId, consentFormType, dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}

	// -------------------------------
	// Update Consent Form
	// -------------------------------
//	@PutMapping("/consent-form/{hospitalId}/{consentFormType}")
//	public ResponseEntity<Response> updateConsentForm(@PathVariable String hospitalId, @PathVariable String consentFormType,
//			@RequestBody CustomConsentFormDTO dto) {
//		Response response = consentFormService.updateCustomConsentForm(hospitalId, consentFormType, dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}

	// -------------------------------
	// Get Generic Consent Form
	// -------------------------------
	@GetMapping("/consent-form/{hospitalId}/{consentFormType}")
	public ResponseEntity<Response> getConsentForm(@PathVariable String hospitalId, @PathVariable String consentFormType) {
		Response response = consentFormService.getConsentForm(hospitalId, consentFormType);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// -------------------------------
	// Get Procedure Consent Form
	// -------------------------------
	@GetMapping("/consent-form/{hospitalId}/subservice/{subServiceId}")
	public ResponseEntity<Response> getProcedureConsentForm(@PathVariable String hospitalId, @PathVariable String subServiceId) {
		Response response = consentFormService.getProcedureConsentForm(hospitalId, subServiceId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// -------------------------------
	// Get All Consent Forms by Hospital
	// -------------------------------
	@GetMapping("/consent-form/{hospitalId}")
	public ResponseEntity<Response> getAllConsentFormsByHospital(@PathVariable String hospitalId) {
		Response response = consentFormService.getAllConsentFormsByHospital(hospitalId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	@DeleteMapping("/deleteConsentFormById/{id}")
    public Response deleteConsentFormById(@PathVariable String id) {
        return consentFormService.deleteConsentFormById(id);
    }
}

