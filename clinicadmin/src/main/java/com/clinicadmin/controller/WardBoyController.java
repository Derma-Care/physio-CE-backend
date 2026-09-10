package com.clinicadmin.controller;



import java.util.List;

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
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.WardBoyDTO;
import com.clinicadmin.service.WardBoyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class WardBoyController {

    private final WardBoyService wardBoyService;

    @PostMapping("/addWardBoy")
    public ResponseEntity<ResponseStructure<WardBoyDTO>> adWardBoy(@RequestBody WardBoyDTO dto) {
        ResponseStructure<WardBoyDTO> response = wardBoyService.addWardBoy(dto);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/getWardBoyById/{id}")
    public ResponseEntity<ResponseStructure<WardBoyDTO>> getWardBoyById(@PathVariable String id) {
        ResponseStructure<WardBoyDTO> response = wardBoyService.getWardBoyById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/getAllWardBoys")
    public ResponseEntity<ResponseStructure<List<WardBoyDTO>>> getAllWardBoys() {
        ResponseStructure<List<WardBoyDTO>> response = wardBoyService.getAllWardBoys();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @PutMapping("/updateWardBoyById/{id}")
    public ResponseEntity<ResponseStructure<WardBoyDTO>> updateWardBoy(
            @PathVariable String id,
            @RequestBody WardBoyDTO dto) {
        ResponseStructure<WardBoyDTO> response = wardBoyService.updateWardBoy(id, dto);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    @GetMapping("/getWardBoysByClinicId/{clinicId}")
    public ResponseStructure<List<WardBoyDTO>> getWardBoysByClinic(@PathVariable String clinicId) {
        return wardBoyService.getWardBoysByClinicId(clinicId);
    }


    @GetMapping("/getWardBoyByIdAndClinicId/{wardBoyId}/{clinicId}")
    public ResponseStructure<WardBoyDTO> getWardBoyByIdAndClinic(
            @PathVariable String wardBoyId,
            @PathVariable String clinicId) {
        return wardBoyService.getWardBoyByIdAndClinicId(wardBoyId, clinicId);
    }

    @DeleteMapping("/deleteWardBoyById/{id}")
    public ResponseEntity<ResponseStructure<Void>> deleteWardBoy(@PathVariable String id) {
        ResponseStructure<Void> response = wardBoyService.deleteWardBoy(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    @GetMapping("/getWardBoysByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<WardBoyDTO>>> getWardBoysByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {
        
        ResponseStructure<List<WardBoyDTO>> response = wardBoyService.getWardBoysByClinicIdAndBranchId(clinicId, branchId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

}