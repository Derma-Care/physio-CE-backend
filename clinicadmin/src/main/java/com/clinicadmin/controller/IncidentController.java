package com.clinicadmin.controller;

import java.util.Map;

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

import com.clinicadmin.dto.IncidentDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.IncidentService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class IncidentController {
	@Autowired
	private IncidentService incidentService;

	@PostMapping("/incident/createIncident")
	ResponseEntity<Response> createIncidents(@RequestBody IncidentDTO dto) {
		Response response = incidentService.createIncident(dto);
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}
		return null;
	}

	@GetMapping("/incident/getAllIncidents")
	ResponseEntity<Response> getAllIncidents() {
		Response response = incidentService.getAllIncidents();
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}
		return null;
	}

	@PutMapping("/incident/updateIncidentStatus/{id}")
	ResponseEntity<Response> updateIncidentStatus(@PathVariable String id, @RequestBody Map<String, String> statusMap) {
		String status=statusMap.get("status");
		Response response = incidentService.UpdateIncidentStatu(id, status);
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}

		return null;
	}

	@DeleteMapping("/incident/deleteIncident/{id}")
	ResponseEntity<Response> deleteIncident(@PathVariable String id) {
		Response response = incidentService.deleteIncident(id);
		if (response != null) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}
		return null;
	}

}
