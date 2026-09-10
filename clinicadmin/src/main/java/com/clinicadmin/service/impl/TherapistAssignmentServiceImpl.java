package com.clinicadmin.service.impl;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistAssignmentDTO;
import com.clinicadmin.entity.TherapistAssignment;
import com.clinicadmin.feignclient.NotificationFeign;
import com.clinicadmin.repository.TherapistAssignmentRepository;
import com.clinicadmin.service.TherapistAssignmentService;

@Service
public class TherapistAssignmentServiceImpl
        implements TherapistAssignmentService {

    @Autowired
    private TherapistAssignmentRepository repository;
    
    @Autowired
    private NotificationFeign notificationFeign;

    @Override
    public Response assignTherapist(
            TherapistAssignmentDTO dto) {

        Response response = new Response();

        try {

        	Optional<TherapistAssignment> existingAssignment =
        	        repository.findByTherapistRecordId(
        	                dto.getTherapistRecordId());

        	TherapistAssignment assignment =
        	        existingAssignment.orElse(
        	                new TherapistAssignment());
        	if (assignment == null) {
        	    assignment = new TherapistAssignment();
        	}

            assignment.setClinicId(
                    dto.getClinicId());

            assignment.setBranchId(
                    dto.getBranchId());

            assignment.setTherapistRecordId(
                    dto.getTherapistRecordId());

            assignment.setAssignTherapistId(
                    dto.getAssignTherapistId());

            assignment.setAssignTherapistName(
                    dto.getAssignTherapistName());

            assignment.setAssignedTherapistId(
                    dto.getAssignedTherapistId());

            assignment.setAssignedTherapistName(
                    dto.getAssignedTherapistName());

            assignment.setServices(
                    dto.getServices());

            assignment.setAssignedStatus("true");
            assignment.setAssignedTo(false);
            
            

            TherapistAssignment savedAssignment =
                    repository.save(assignment);
            
            try {
            	Map<String,String> map = new LinkedHashMap<>();            
    			map.put("therapistname",dto.getAssignTherapistName());
    			map.put("reassignedTherapistId",dto.getAssignedTherapistId());
    			map.put("reassignedTherapistname",dto.getAssignedTherapistName()); 
    			map.put("therapistRecordId",dto.getTherapistRecordId());
            	notificationFeign.sendSessionReassignNotificationToTherapist(map);
            }catch(Exception e) {}

            TherapistAssignmentDTO responseDto =
                    convertToDto(savedAssignment);

            response.setSuccess(true);
            response.setData(responseDto);
            response.setMessage(
                    "Therapist assigned successfully");
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getAssignedTherapistDetails(
            String therapistRecordId) {

        Response response = new Response();

        try {

            Optional<TherapistAssignment> optional =
                    repository.findByTherapistRecordId(
                            therapistRecordId);

            if (optional.isEmpty()) {

                response.setSuccess(false);
                response.setMessage(
                        "Assignment not found");
                response.setStatus(404);

                return response;
            }

            TherapistAssignmentDTO dto =
                    convertToDto(optional.get());

            response.setSuccess(true);
            response.setData(dto);
            response.setMessage(
                    "Assignment fetched successfully");
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(500);
        }

        return response;
    }
    
    @Override
    public Response updateAssignedStatus(
            String therapistRecordId,
            TherapistAssignmentDTO dto) {

        Response response = new Response();

        try {

            Optional<TherapistAssignment> optional =
                    repository.findByTherapistRecordId(
                            therapistRecordId);

            if (optional.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Assignment not found");
                response.setStatus(404);

                return response;
            }

            TherapistAssignment assignment =
                    optional.get();

            assignment.setAssignedStatus(
                    dto.getAssignedStatus());

            repository.save(assignment);

            response.setSuccess(true);
            response.setData(convertToDto(assignment));
            response.setMessage(
                    "Assigned status updated successfully");
            response.setStatus(200);
            
            try {
            	if(dto.getAssignedStatus().equals("false")) {
            	Map<String,String> map = new LinkedHashMap<>();               			
    			map.put("therapistId",assignment.getAssignTherapistId());
    			map.put("reassignedTherapistname",assignment.getAssignedTherapistName()); 
    			map.put("therapistRecordId",assignment.getTherapistRecordId());
            	notificationFeign.sendSessionWithdrawNotificationToTherapist(map);
            	}}catch(Exception e) {System.out.println(e.getMessage());}

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(500);
        }

        return response;
    }
    private TherapistAssignmentDTO convertToDto(
            TherapistAssignment assignment) {

        TherapistAssignmentDTO dto =
                new TherapistAssignmentDTO();

        dto.setId(assignment.getId());
        dto.setClinicId(assignment.getClinicId());
        dto.setBranchId(assignment.getBranchId());

        dto.setTherapistRecordId(
                assignment.getTherapistRecordId());

        dto.setAssignTherapistId(
                assignment.getAssignTherapistId());

        dto.setAssignTherapistName(
                assignment.getAssignTherapistName());

        dto.setAssignedTherapistId(
                assignment.getAssignedTherapistId());

        dto.setAssignedTherapistName(
                assignment.getAssignedTherapistName());
        dto.setServices(
                assignment.getServices());

        dto.setAssignedStatus(
                assignment.getAssignedStatus());

        return dto;
    }
}
