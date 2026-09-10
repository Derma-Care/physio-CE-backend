package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.DiagnosisAnalyticsService;

@RestController
@RequestMapping("/clinic-admin")

public class DiagnosisAnalyticsController {

    @Autowired
    private DiagnosisAnalyticsService service;

    // ======================= Dashboard =======================

    // Today / Week / Month / Year
    @GetMapping("/getDiagnosisDashboard/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getDashboard(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                service.getDashboard(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("/getDiagnosisDashboardCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDashboardCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                service.getDashboard(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }

    // ======================= Diagnosis Centers =======================

    // Today / Week / Month / Year
    @GetMapping("/getDiagnosisCenters/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getDiagnosisCenters(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                service.getDiagnosisTests(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("getDiagnosisCentersCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDiagnosisCentersCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                service.getDiagnosisTests(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }

    // ======================= Diagnosis Center Details =======================

    // Today / Week / Month / Year
    @GetMapping("getDiagnosisCenterPatientDetails/{clinicId}/{branchId}/{vendorId}/{type}")
    public ResponseEntity<Response> getDiagnosisCenterDetails(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String vendorId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                service.getDiagnosisDetails(
                        clinicId,
                        branchId,
                        vendorId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("getDiagnosisCenterPatientDetailsCustom/{clinicId}/{branchId}/{vendorId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDiagnosisCenterDetailsCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String vendorId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                service.getDiagnosisDetails(
                        clinicId,
                        branchId,
                        vendorId,
                        5,
                        startDate,
                        endDate));
    }
    
 // ======================= Vendor Test Summary =======================

    @GetMapping("/getVendorTestSummary/{clinicId}/{branchId}/{vendorId}/{testNameId}")
    public ResponseEntity<Response> getVendorTestSummary(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String vendorId,
            @PathVariable String testNameId) {

        return ResponseEntity.ok(
                service.getVendorTestSummary(
                        clinicId,
                        branchId,
                        vendorId,
                        testNameId));
    }
    
    @GetMapping("/getVendorTests/{clinicId}/{branchId}/{vendorId}")
    public ResponseEntity<Response> getVendorTests(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String vendorId) {

        return ResponseEntity.ok(
                service.getVendorTests(
                        clinicId,
                        branchId,
                        vendorId));
    }
}