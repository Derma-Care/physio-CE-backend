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

import com.clinicadmin.dto.DashboardRequest;
import com.clinicadmin.dto.ReceptionistRequestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.service.ReceptionistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clinic-admin") // Base URL matches the class/entity name
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReceptionistController {

    @Autowired
    private ReceptionistService service;

    // Create a new Receptionist
    @PostMapping("/createReceptionist")
    public ResponseStructure<ReceptionistRequestDTO> createReceptionist(
            @Valid @RequestBody ReceptionistRequestDTO dto) {
        return service.createReceptionist(dto);
    }

    // Get Receptionist by ID
    @GetMapping("/getReceptionistById/{id}")
    public ResponseStructure<ReceptionistRequestDTO> getReceptionistById(@PathVariable String id) {
        return service.getReceptionistById(id);
    }

    // Get all Receptionists
    @GetMapping("/getAllReceptionists")
    public ResponseStructure<List<ReceptionistRequestDTO>> getAllReceptionists() {
        return service.getAllReceptionists();
    }

    // Update Receptionist by ID
    @PutMapping("/updateReceptionist/{id}")
    public ResponseStructure<ReceptionistRequestDTO> updateReceptionist(
            @PathVariable String id,
            @RequestBody ReceptionistRequestDTO dto) {
        return service.updateReceptionist(id, dto);
    }

    // Delete Receptionist by ID
    @DeleteMapping("/deleteReceptionist/{id}")
    public ResponseStructure<String> deleteReceptionist(@PathVariable String id) {
        return service.deleteReceptionist(id);
    }
//    @PostMapping("/receptionistLogin")
//    public ResponseEntity<OnBoardResponse> login(@RequestBody ReceptionistRequestDTO request) {
//        OnBoardResponse response = service.login(request.getUserName(), request.getPassword());
//        return ResponseEntity.status(response.getHttpStatus()).body(response);
//    }


//    @PutMapping("/receptionistReset-password/{contactNumber}")
//    public ResponseEntity<ResponseStructure<String>> resetPassword(
//            @PathVariable String contactNumber,
//            @RequestBody ReceptionistRestPassword request) {
//
//        ResponseStructure<String> response = service.resetPassword(contactNumber, request);
//        return ResponseEntity.status(response.getHttpStatus()).body(response);
//    }
    
 // ✅ Get all receptionists by clinicId
    
    @GetMapping("/receptionists/{clinicId}")
    public ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinic(
            @PathVariable String clinicId) {
        ResponseStructure<List<ReceptionistRequestDTO>> response = service.getReceptionistsByClinic(clinicId);  // use 'service'
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
    

    // ✅ GET Receptionist by ClinicId and ReceptionistId
    
    @GetMapping("/{clinicId}/{receptionistId}")
    public ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> getReceptionistByClinicAndId(
            @PathVariable String clinicId,
            @PathVariable String receptionistId) {

        ResponseStructure<ReceptionistRequestDTO> response = service.getReceptionistByClinicAndId(clinicId, receptionistId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    
 // Get receptionists by branchId and clinicId
    @GetMapping("/getReceptionistsByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {
        
        ResponseStructure<List<ReceptionistRequestDTO>> response = 
                service.getReceptionistsByClinicAndBranch(clinicId, branchId);
        
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
    
    @PostMapping("/getReceptionistDashboard/{clinicId}/{branchId}")
    public ResponseEntity<Response> getReceptionistDashboard(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return service.getReceptionistDashboard(clinicId, branchId);
    }
    
    @PutMapping("/updateReceptionistDashboard/{clinicId}/{branchId}/{role}")
    public ResponseEntity<?> updateReceptionistDashboard(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String role,
            @RequestBody DashboardRequest request) {

        return ResponseEntity.ok(
                service.updateReceptionistDashboard(
                        clinicId,
                        branchId,
                        role,
                        request));
    }

}