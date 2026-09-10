package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistAssignmentDTO;
import com.clinicadmin.service.TherapistAssignmentService;

@RestController
@RequestMapping("/clinic-admin")
public class TherapistAssignmentController {

    @Autowired
    private TherapistAssignmentService service;

    @PostMapping("/assignTherapist")
    public ResponseEntity<Response> assignTherapist(
            @RequestBody TherapistAssignmentDTO dto) {

        Response response =
                service.assignTherapist(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/getAssignedTherapistDetails/{therapistRecordId}")
    public ResponseEntity<Response> getAssignedTherapistDetails(
            @PathVariable String therapistRecordId) {

        Response response =
                service.getAssignedTherapistDetails(
                        therapistRecordId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @PutMapping("/updateAssignedStatus/{therapistRecordId}")
    public ResponseEntity<Response> updateAssignedStatus(
            @PathVariable String therapistRecordId,
            @RequestBody TherapistAssignmentDTO dto) {

        Response response =
                service.updateAssignedStatus(
                        therapistRecordId,
                        dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}