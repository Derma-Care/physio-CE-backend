package com.clinicadmin.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.IncidentDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Incident;
import com.clinicadmin.enumclasses.IncidentStatus;
import com.clinicadmin.repository.IncidentRepository;
import com.clinicadmin.service.IncidentService;

@Service
public class IncidentServiceImpl implements IncidentService {
	@Autowired
	private IncidentRepository incidentRepository;

	@Override
	public Response createIncident(IncidentDTO dto) {
		Response response = new Response();
		if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
			response.setSuccess(false);
			response.setMessage("Title is required");
			response.setStatus(400);
			return response;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mma");

		String now = LocalDateTime.now().format(formatter);
		Incident incident = Incident.builder().title(dto.getTitle()).description(dto.getDescription())
				.status(IncidentStatus.NEW).raisedBy(dto.getRaisedBy()).assignedTo(dto.getAssignedTo())
				.priority(dto.getPriority()).createdAt(now).updatedAt(now).build();
		incident = incidentRepository.save(incident);
		IncidentDTO covetedDTO = convertToDTO(incident);

		response.setSuccess(true);
		response.setData(covetedDTO);
		response.setMessage("Incident created successfully");
		response.setStatus(200);
		return response;
	}
	@Override
	public Response getAllIncidents() {
	    Response response = new Response();
	    try {
	        List<Incident> incidents = incidentRepository.findAll();
	        if (incidents.isEmpty()) {
	            response.setSuccess(true);
	            response.setData(Collections.emptyList());
	            response.setMessage("No incidents found");
	            response.setStatus(200);
	            return response;
	        }

	        List<IncidentDTO> dtos = incidents.stream()
	                .map(this::convertToDTO)
	                .collect(Collectors.toList());

	        response.setSuccess(true);
	        response.setData(dtos);
	        response.setMessage("Incidents fetched successfully"); // ✅ fixed
	        response.setStatus(200);
	        return response;

	    } catch (Exception e) {
	        response.setSuccess(false);
	        response.setMessage("Error occurred while getting list of incidents: " + e.getMessage());
	        response.setStatus(500);
	        return response;
	    }
	}


	@Override
	public Response UpdateIncidentStatu(String id, String status) {
		Response response = new Response();
		try {
			Optional<Incident> optiinalIncident = incidentRepository.findById(id);
			if (optiinalIncident.isEmpty()) {
				response.setSuccess(true);
				response.setMessage("Incident is not found with this id :" + id);
				response.setStatus(200);
				return response;
			}
			Incident incident = optiinalIncident.get();
			try {
				incident.setStatus(IncidentStatus.valueOf(status.toUpperCase()));
			} catch (Exception e) {
				response.setSuccess(true);
				response.setMessage("Invalid status value: " + status);
				response.setStatus(200);
				return response;
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mma");

			String now = LocalDateTime.now().format(formatter);
			incident.setUpdatedAt(now);
			Incident updatedIncident = incidentRepository.save(incident);
			IncidentDTO updatedIncidentDTO = convertToDTO(updatedIncident);
			response.setSuccess(true);
			response.setData(updatedIncidentDTO);
			response.setMessage("Status updated successfully");
			response.setStatus(200);
			return response;
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("error occured while updating incident status :" + e.getMessage());
			response.setStatus(500);
			return response;
		}

	}

	@Override
	public Response deleteIncident(String id) {
		Response response = new Response();
		try {
			Optional<Incident> incident = incidentRepository.findById(id);
			if (incident.isEmpty()) {
				response.setSuccess(true);
				response.setMessage("Incident is not found with this id :" + id);
				response.setStatus(200);
				return response;
			}
			incidentRepository.deleteById(id);
			response.setSuccess(true);
			response.setMessage("Incident deleted successfully");
			response.setStatus(200);
			return response;
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("error occured while deleting incident using incidentId :" + e.getMessage());
			response.setStatus(500);
			return response;

		}

	}

//-------------------Mapper for incident-----------------------------
	private IncidentDTO convertToDTO(Incident incident) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mma");

		return IncidentDTO.builder().id(incident.getId()).title(incident.getTitle())
				.description(incident.getDescription()).status(incident.getStatus().toString())
				.raisedBy(incident.getRaisedBy()).assignedTo(incident.getAssignedTo()).priority(incident.getPriority())
				.createdAt(incident.getCreatedAt() != null ? incident.getCreatedAt().formatted(formatter) : null)
				.updatedAt(incident.getUpdatedAt() != null ? incident.getUpdatedAt().formatted(formatter) : null)
				.build();
	}

}
