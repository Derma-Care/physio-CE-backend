package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PatientFeedbackDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PatientFeedbackService;

@RestController
@RequestMapping("/clinic-admin")
public class PatientFeedbackController {

    @Autowired
    private PatientFeedbackService service;

    // ================= CREATE =================

    @PostMapping("/createPatientFeedback")
    public Response createFeedback(
            @RequestBody PatientFeedbackDTO dto) {

        return service.createFeedback(dto);
    }

    // ================= GET ALL =================

    @GetMapping("/getAllPatientFeedback")
    public Response getAllPatientFeedbacks() {

        return service.getAllFeedbacks();
    }

    // ================= GET BY ID =================

    @GetMapping("/getPatientFeedbackById/{id}")
    public Response getPatientFeedbackById(
            @PathVariable String id) {

        return service.getFeedbackById(id);
    }

    // ================= UPDATE =================

    @PutMapping("/updatePatientFeedback/{id}")
    public Response updatePatientFeedback(
            @PathVariable String id,
            @RequestBody PatientFeedbackDTO dto) {

        return service.updateFeedback(id, dto);
    }

    // ================= DELETE =================

    @DeleteMapping("/deletePatientFeedback/{id}")
    public Response deletePatientFeedback(
            @PathVariable String id) {

        return service.deleteFeedback(id);
    }

    @GetMapping("/getByPatientFeedbackClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = service
                .getByClinicIdAndBranchId(clinicId, branchId);

        return new ResponseEntity<>(
                response,
                HttpStatus.valueOf(response.getStatus()));
    }
    

    @GetMapping("/getByPatientFeedbackClinicIdAndBranchId/{clinicId}/{branchId}/{patientId}")
    public ResponseEntity<Response> getByClinicIdAndBranchIdAndPatirntId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String patientId ) {

        Response response = service
                .getByClinicIdAndBranchIdAndPatientId(clinicId, branchId,patientId);

        return new ResponseEntity<>(
                response,
                HttpStatus.valueOf(response.getStatus()));
    }
    
    @GetMapping("/getDoctorFeedbackSummaryByCinicIdAndDoctorId/{clinicId}/{doctorId}")
    public ResponseEntity<Response> getDoctorFeedbackSummary(
            @PathVariable String doctorId,
            @PathVariable String clinicId) {

        return ResponseEntity.ok(service.getDoctorFeedbackSummary(doctorId, clinicId));
    }
}