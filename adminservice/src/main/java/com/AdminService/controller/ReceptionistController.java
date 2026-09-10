package com.AdminService.controller;

import com.AdminService.dto.ReceptionistRequestDTO;
import com.AdminService.service.ReceptionistService;
import com.AdminService.util.ResponseStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    // ✅ Create Receptionist
    @PostMapping("/createReceptionist")
    public ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> createReceptionist(
            @RequestBody ReceptionistRequestDTO dto) {
        return receptionistService.createReceptionist(dto);
    }

    // ✅ Get Receptionist by ID
    @GetMapping("/getReceptionistById/{id}")
    public ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> getReceptionistById(@PathVariable String id) {
        return receptionistService.getReceptionistById(id);
    }

    // ✅ Get All Receptionists
    @GetMapping("/getAllReceptionists")
    public ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getAllReceptionists() {
        return receptionistService.getAllReceptionists();
    }

    // ✅ Update Receptionist
    @PutMapping("/updateReceptionist/{id}")
    public ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> updateReceptionist(
            @PathVariable String id,
            @RequestBody ReceptionistRequestDTO dto) {
        return receptionistService.updateReceptionist(id, dto);
    }

    // ✅ Delete Receptionist
    @DeleteMapping("/deleteReceptionist/{id}")
    public ResponseEntity<ResponseStructure<String>> deleteReceptionist(@PathVariable String id) {
        return receptionistService.deleteReceptionist(id);
    }

    // ✅ Get Receptionists by Clinic ID
    @GetMapping("/getReceptionistsByClinic/{clinicId}")
    public ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinic(
            @PathVariable String clinicId) {
        return receptionistService.getReceptionistsByClinic(clinicId);
    }

    // ✅ Get Receptionist by Clinic ID and Receptionist ID
    @GetMapping("/getReceptionistByClinicAndId/{clinicId}/{receptionistId}")
    public ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> getReceptionistByClinicAndId(
            @PathVariable String clinicId,
            @PathVariable String receptionistId) {
        return receptionistService.getReceptionistByClinicAndId(clinicId, receptionistId);
    }

    // ✅ Get Receptionists by Clinic ID and Branch ID
    @GetMapping("/getReceptionistsByClinicAndBranch/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId) {
        return receptionistService.getReceptionistsByClinicAndBranch(clinicId, branchId);
    }
}
