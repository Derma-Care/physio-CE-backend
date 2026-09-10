package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistCertificateDTO;
import com.clinicadmin.service.TherapistCertificateService;

@RestController
@RequestMapping("/clinic-admin")
public class TherapistCertificateController {

    @Autowired
    private TherapistCertificateService service;

    // CREATE
    @PostMapping("/createTherapistCertificate")
    public ResponseEntity<Response> createTherapistCertificate(
            @RequestBody TherapistCertificateDTO dto) {

        return ResponseEntity.ok(
                service.createCertificate(dto));
    }

    // GET ALL
    @GetMapping("/getAllTherapistCertificates")
    public ResponseEntity<Response> getAllTherapistCertificates() {

        return ResponseEntity.ok(
                service.getAllCertificates());
    }

    // GET BY ID
    @GetMapping("/getTherapistCertificateById/{id}")
    public ResponseEntity<Response> getTherapistCertificateById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                service.getCertificateById(id));
    }

    // GET BY CLINIC & BRANCH
    @GetMapping("/getTherapistCertificatesByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response>
    getTherapistCertificatesByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                service.getCertificatesByClinicAndBranch(
                        clinicId,
                        branchId));
    }
    
    @GetMapping("/getTherapistCertificatesByClinicIdBranchIdAndTherapistId/{clinicId}/{branchId}/{therapistId}")
    public ResponseEntity<Response> getCertificatesByClinicBranchAndTherapist(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String therapistId) {

        Response response =
                service.getCertificatesByClinicBranchAndTherapist(
                        clinicId,
                        branchId,
                        therapistId);

        return ResponseEntity.status(response.getStatus())
                .body(response);
    }

    // UPDATE
    @PutMapping("/updateTherapistCertificateById/{id}")
    public ResponseEntity<Response> updateTherapistCertificateById(
            @PathVariable String id,
            @RequestBody TherapistCertificateDTO dto) {

        return ResponseEntity.ok(
                service.updateCertificate(id, dto));
    }

    // DELETE
    @DeleteMapping("/deleteTherapistCertificateById/{id}")
    public ResponseEntity<Response> deleteTherapistCertificateById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                service.deleteCertificate(id));
    }
}