package com.clinicadmin.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import com.clinicadmin.dto.LabTechnicanRestPassword;
import com.clinicadmin.dto.LabTechnicianLogin;
import com.clinicadmin.dto.LabTechnicianRequestDTO;
import com.clinicadmin.dto.OnBoardResponse;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.service.LabTechnicianService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class LabTechnicianController {

    @Autowired
    private LabTechnicianService service;

    // -------------------------
    // CREATE
    // -------------------------
    @PostMapping("/addLabTechnician")
    public ResponseEntity<ResponseStructure<LabTechnicianRequestDTO>> createLabTechnician(
            @RequestBody LabTechnicianRequestDTO dto) {
        ResponseStructure<LabTechnicianRequestDTO> response = service.createLabTechnician(dto);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // -------------------------
    // LOGIN
    // -------------------------
//    @PostMapping("/login")
//    public ResponseEntity<OnBoardResponse> login(@RequestBody LabTechnicianLogin loginRequest) {
//        OnBoardResponse response = service.login(loginRequest);
//        return ResponseEntity.status(response.getHttpStatus()).body(response);
//    }
//
//    @PutMapping("/reset-password/{contactNumber}")
//    public ResponseEntity<ResponseStructure<String>> resetPassword(
//            @PathVariable String contactNumber,
//            @RequestBody LabTechnicanRestPassword request) {
//        
//        ResponseStructure<String> response = service.resetPassword(contactNumber, request);
//        return ResponseEntity.status(response.getHttpStatus()).body(response);
//    }




    // -------------------------
    // GET BY ID
    // -------------------------
    @GetMapping("/getById/{id}")
    public ResponseEntity<ResponseStructure<LabTechnicianRequestDTO>> getLabTechnicianById(
            @PathVariable String id) {
        ResponseStructure<LabTechnicianRequestDTO> response = service.getLabTechnicianById(id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // -------------------------
    // GET ALL
    // -------------------------
    @GetMapping("/getAllLabTechnicians")
    public ResponseEntity<ResponseStructure<List<LabTechnicianRequestDTO>>> getAllLabTechnicians() {
        ResponseStructure<List<LabTechnicianRequestDTO>> response = service.getAllLabTechnicians();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // -------------------------
    // UPDATE
    // -------------------------
    @PutMapping("/updateById/{id}")
    public ResponseEntity<ResponseStructure<LabTechnicianRequestDTO>> updateLabTechnician(
            @PathVariable String id,
            @RequestBody LabTechnicianRequestDTO dto) {
        ResponseStructure<LabTechnicianRequestDTO> response = service.updateLabTechnician(id, dto);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // -------------------------
    // DELETE
    // -------------------------
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ResponseStructure<String>> deleteLabTechnician(@PathVariable String id) {
        ResponseStructure<String> response = service.deleteLabTechnician(id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
 // ✅ Get all Lab Technicians by Clinic Id
    @GetMapping("/getLabTechniciansByClinicById/{clinicId}")
    public ResponseEntity<ResponseStructure<List<LabTechnicianRequestDTO>>> getLabTechniciansByClinic(
            @PathVariable String clinicId) {
        ResponseStructure<List<LabTechnicianRequestDTO>> response = service.getLabTechniciansByClinic(clinicId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    // ✅ Get a single Lab Technician by Clinic Id and Technician Id
    @GetMapping("/getLabTechnicianByIdAndClinicId/{clinicId}/{technicianId}")
    public ResponseEntity<ResponseStructure<LabTechnicianRequestDTO>> getLabTechnicianByClinicAndId(
            @PathVariable String clinicId,
            @PathVariable String technicianId) {
        ResponseStructure<LabTechnicianRequestDTO> response = service.getLabTechnicianByClinicAndId(clinicId, technicianId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
//  GET BY CLINIC ID + BRANCH ID
@GetMapping("/getLabTechniciansByClinicIdAndBranchId/{clinicId}/{branchId}")
public ResponseEntity<ResponseStructure<List<LabTechnicianRequestDTO>>> getLabTechniciansByClinicAndBranch(
        @PathVariable String clinicId,
        @PathVariable String branchId) {
    ResponseStructure<List<LabTechnicianRequestDTO>> response =
            service.getLabTechniciansByHospitalIdAndBranchId(clinicId, branchId);
    return ResponseEntity.status(response.getHttpStatus()).body(response);
}

    
    
}