package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.BillingDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.BillingService;

@RestController
@RequestMapping("/clinic-admin")
public class BillingController {

    @Autowired
    private BillingService billingService;


    // ================= CREATE =================

    @PostMapping("/createBilling")
    public ResponseEntity<Response> createBilling(
            @RequestBody BillingDTO billingDTO) {

        Response response =
                billingService.createBilling(billingDTO);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    // ================= GET BY BILLING ID =================

    @GetMapping("/getBillingById/{billingId}")
    public ResponseEntity<Response> getBillingById(
            @PathVariable String billingId) {

        Response response =
                billingService.getBillingById(billingId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    // ================= GET ALL =================

    @GetMapping("/getAllBillingsByUsingClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getAllBillings(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response =
                billingService.getAllBillings(
                        clinicId,
                        branchId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    // ================= UPDATE =================

    @PutMapping("/updateBillingByUsingBillingId/{billingId}")
    public ResponseEntity<Response> updateBilling(
            @PathVariable String billingId,
            @RequestBody BillingDTO billingDTO) {

        Response response =
                billingService.updateBilling(
                        billingId,
                        billingDTO);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
 // ================= GET ALL BY CLINIC ID =================

    @GetMapping("/getAllBillingsByUsingClinicId/{clinicId}")
    public ResponseEntity<Response> getAllBillingsByClinicId(
            @PathVariable String clinicId) {

        Response response =
                billingService.getAllBillingsByClinicId(
                        clinicId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    // ================= GET ALL BILLINGS =================

    @GetMapping("/getAllBillings")
    public ResponseEntity<Response> getAllBillings() {

        Response response =
                billingService.getAllBillings();

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


    // ================= DELETE =================

    @DeleteMapping("/deleteBillingByUsingBillingId/{billingId}")
    public ResponseEntity<Response> deleteBilling(
            @PathVariable String billingId) {

        Response response =
                billingService.deleteBilling(billingId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}