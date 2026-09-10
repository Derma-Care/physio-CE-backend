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

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.VitalsDTO;
import com.clinicadmin.service.VitalService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class VitalsController {
	@Autowired
	VitalService vitalService;

	@PostMapping("/addingVitals/{bookingId}")
	public ResponseEntity<Response> addVitals(@PathVariable String bookingId, @RequestBody VitalsDTO dto) {

		Response response = vitalService.postVitals(bookingId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);

	}

	@GetMapping("/getVitals/{bookingId}/{patientId}")
	public ResponseEntity<Response> getVitals(@PathVariable String bookingId, @PathVariable String patientId) {
		Response response = vitalService.getPatientByBookingIdAndPatientId(bookingId, patientId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@DeleteMapping("/deleteVitals/{bookingId}/{patientId}")
	public ResponseEntity<Response> delVitals(@PathVariable String bookingId, @PathVariable String patientId) {
		Response response = vitalService.deleteVitals(bookingId, patientId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PutMapping("/updateVitals/{bookingId}/{patientId}")
	public ResponseEntity<Response> delVitals(@PathVariable String bookingId,@PathVariable String patientId,@RequestBody VitalsDTO dto)
	{
		Response response=vitalService.updateVitals(bookingId, patientId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

}