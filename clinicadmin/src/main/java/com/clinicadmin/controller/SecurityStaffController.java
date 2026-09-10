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
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.SecurityStaffDTO;
import com.clinicadmin.entity.SecurityStaff;
import com.clinicadmin.service.SecurityStaffService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class SecurityStaffController {

	@Autowired
    private  SecurityStaffService service;
	
    @PostMapping("/addSecurityStaff")
    public ResponseEntity<ResponseStructure<SecurityStaffDTO>> addSecurityStaff(@RequestBody SecurityStaffDTO dto) {
        ResponseStructure<SecurityStaffDTO> response = service.addSecurityStaff(dto);
        return new ResponseEntity<>(response, response.getHttpStatus());
 }

    @PutMapping("/updateSecurityStaffById/{staffId}")
    public ResponseEntity<ResponseStructure<SecurityStaff>> updateSecurityStaff(
            @PathVariable String staffId,
            @RequestBody SecurityStaff staffRequest) {

      
        staffRequest.setSecurityStaffId(staffId);

        ResponseStructure<SecurityStaff> response = service.updateSecurityStaff(staffRequest);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    @GetMapping("/getSecurityStaffById/{staffId}")
    public ResponseEntity<ResponseStructure<SecurityStaffDTO>> getSecurityStaffById(@PathVariable String staffId) {
        ResponseStructure<SecurityStaffDTO> response = service.getSecurityStaffById(staffId);
        return new ResponseEntity<>(response, response.getHttpStatus());
}

      @GetMapping("/getAllSecurityStaffByClinicId/{clinicId}")
    public ResponseEntity<ResponseStructure<List<SecurityStaffDTO>>> getAllByClinicId(@PathVariable String clinicId) {
        ResponseStructure<List<SecurityStaffDTO>> response = service.getAllByClinicId(clinicId);
        return new ResponseEntity<>(response, response.getHttpStatus());}

    @DeleteMapping("/deleteSecurityStaffById/{staffId}")
    public ResponseEntity<ResponseStructure<String>> deleteSecurityStaff(@PathVariable String staffId) {
        ResponseStructure<String> response = service.deleteSecurityStaff(staffId);
        return new ResponseEntity<>(response, response.getHttpStatus());
}
    
 // ------------------- Get All Security Staff by ClinicId and BranchId -------------------
    @GetMapping("/getSecurityStaffByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<SecurityStaffDTO>>> getSecurityStaffByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        ResponseStructure<List<SecurityStaffDTO>> response = service.getSecurityStaffByClinicIdAndBranchId(clinicId, branchId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

}