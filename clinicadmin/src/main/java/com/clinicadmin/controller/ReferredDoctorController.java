package com.clinicadmin.controller;

import com.clinicadmin.dto.ReferredDoctorDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.ReferredDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReferredDoctorController {

    @Autowired
    private ReferredDoctorService service;

    // ----------------- Add -----------------
    @PostMapping("/addReferralDoctor")
    public ResponseEntity<Response> addReferralDoctor(@RequestBody ReferredDoctorDTO dto) {
        Response response = service.addReferralDoctor(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/getReferralDoctorByReferralId/{referralId}")
    public ResponseEntity<Response> getReferralDoctorByReferralId(@PathVariable String referralId) {
        Response response = service.getDoctorByReferralId(referralId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @PutMapping("/updateReferralDoctorById/{id}")
    public ResponseEntity<Response> updateReferralDoctorById(
            @PathVariable String id,
            @RequestBody ReferredDoctorDTO dto) {
        Response response = service.updateReferralDoctorById(id, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/deleteReferralDoctorById/{id}")
    public ResponseEntity<Response> deleteReferralDoctorById(@PathVariable String id) {
        Response response = service.deleteReferralDoctoById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/getReferralDoctorById/{id}")
    public ResponseEntity<Response> getReferralDoctorById(@PathVariable String id) {
        Response response = service.getReferralDoctorrById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

   
    @GetMapping("/getAllReferralDoctors")
    public ResponseEntity<Response> getAllReferralDoctors() {
        Response response = service.getAllReferralDoctor();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/getReferralDoctorsByClinicId/{clinicId}")
    public ResponseEntity<Response> getReferralDoctorsByClinicId(@PathVariable String clinicId) {
        Response response = service.getReferralDoctorsByClinicId(clinicId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}