package com.clinicadmin.controller;

import com.clinicadmin.dto.PrivacyPolicyDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PrivacyPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PrivacyPolicyController {

    @Autowired
    private PrivacyPolicyService service;


    @PostMapping("/createPolicy")
    public ResponseEntity<Response> createPolicy(@RequestBody PrivacyPolicyDTO dto) {
        return ResponseEntity.ok(service.createPolicy(dto));
    }


    @GetMapping("/getAllPolicies")
    public ResponseEntity<Response> getAllPolicies() {
        return ResponseEntity.ok(service.getAllPolicies());
    }
    
    @GetMapping("/getPoliciesByClinicId/{clinicId}")
    public ResponseEntity<Response> getPoliciesByClinicId(@PathVariable String clinicId) {
        return ResponseEntity.ok(service.getPoliciesByClinicId(clinicId));
    }



    @GetMapping("/getPolicyById/{id}")
    public ResponseEntity<Response> getPolicyById(@PathVariable String id) {
        return ResponseEntity.ok(service.getPolicyById(id));
    }

 
    @PutMapping("/updatePolicy/{id}")
    public ResponseEntity<Response> updatePolicy(
            @PathVariable String id,
            @RequestBody PrivacyPolicyDTO dto) {

      
        dto.setId(id);

        return ResponseEntity.ok(service.updatePolicy(dto));
    }


    @DeleteMapping("/deletePolicyById/{id}")
    public ResponseEntity<Response> deletePolicy(@PathVariable String id) {
        return ResponseEntity.ok(service.deletePolicy(id));
    }
}