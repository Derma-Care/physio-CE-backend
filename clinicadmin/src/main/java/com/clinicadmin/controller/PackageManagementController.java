package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.PackageManagementDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PackageManagementService;

@RestController
@RequestMapping("/clinic-admin")
public class PackageManagementController {
    @Autowired
    private PackageManagementService service;

    // ✅ CREATE
    @PostMapping("/createPackageManagemet")
    public ResponseEntity<Response> createPackage(
          
            @RequestBody PackageManagementDTO dto) {

      

        Response response = service.createPackage(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    // ✅ GET by clinicId + branchId
    @GetMapping("/getPackageByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getPackageByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = service.getByClinicAndBranch(clinicId, branchId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    // ✅ GET by clinicId + branchId + packageId
    @GetMapping("/getByPackageByClinicIdBranchIdAndPackageId/{clinicId}/{branchId}/{packageId}")
    public ResponseEntity<Response> getByPackageByClinicIdBranchIdAndPackageId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String packageId) {

        Response response = service.getByClinicBranchAndPackageId(clinicId, branchId, packageId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
 // ✅ GET by clinicId
    @GetMapping("/getByPackageByClinicId/{clinicId}")
    public ResponseEntity<Response> getByClinicId(
            @PathVariable String clinicId) {

        Response response = service.getByClinicId(clinicId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    // ✅ UPDATE
    @PutMapping("/updatePackageByPackageId/{packageId}")
    public ResponseEntity<Response> updatePackageByPackageId(
            @PathVariable String packageId,
            @RequestBody PackageManagementDTO dto) {

        Response response = service.updatePackage(packageId, dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    // ✅ DELETE
    @DeleteMapping("/deletePackageByPackageId/{packageId}")
    public ResponseEntity<Response> deletePackageByPackageId(
           
            @PathVariable String packageId) {

        Response response = service.deletePackage(packageId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @GetMapping("/getPackageWithProgramsByUsingClinicIdBranchIdAndPackageId/{clinicId}/{branchId}/{packageId}")
    public ResponseEntity<Response> getPackageWithPrograms(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String packageId) {

       
        Response response = service.getPackageWithPrograms(clinicId, branchId, packageId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}