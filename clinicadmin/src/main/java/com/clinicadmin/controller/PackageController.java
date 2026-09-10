package com.clinicadmin.controller;

import com.clinicadmin.dto.PackageDTO;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.service.PackageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clinic-admin")
public class PackageController {

    @Autowired
    private PackageService service;

    // ================= CREATE =================
    @PostMapping("/createPackage")
    public ResponseEntity<ResponseStructure<PackageDTO>> createPackage(
            @RequestBody PackageDTO dto) {

        ResponseStructure<PackageDTO> response = service.createPackage(dto);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= GET BY ID =================
    @GetMapping("/getPackageById/{id}")
    public ResponseEntity<ResponseStructure<PackageDTO>> getPackageById(
            @PathVariable String id) {

        ResponseStructure<PackageDTO> response = service.getPackageById(id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= GET ALL =================
    @GetMapping("/getAllPackages")
    public ResponseEntity<ResponseStructure<List<PackageDTO>>> getAllPackages() {

        ResponseStructure<List<PackageDTO>> response = service.getAllPackages();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= GET BY CLINIC & BRANCH =================
    @GetMapping("/getByPackagesClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<PackageDTO>>> getByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        ResponseStructure<List<PackageDTO>> response =
                service.getByClinicAndBranch(clinicId, branchId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= GET BY CLINIC + BRANCH + PACKAGE =================
    @GetMapping("/getByClinicIdBranchIdAndPackageId/{clinicId}/{branchId}/{packageId}")
    public ResponseEntity<ResponseStructure<PackageDTO>> getByClinicBranchAndPackageId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String packageId) {

        ResponseStructure<PackageDTO> response =
                service.getByClinicBranchAndPackageId(clinicId, branchId, packageId);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= UPDATE =================
    @PutMapping("/updatePackageById/{id}")
    public ResponseEntity<ResponseStructure<PackageDTO>> updatePackage(
            @PathVariable String id,
            @RequestBody PackageDTO dto) {

        ResponseStructure<PackageDTO> response =
                service.updatepackagebyid(id, dto);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    // ================= DELETE =================
    @DeleteMapping("/deletePackageById/{id}")
    public ResponseEntity<ResponseStructure<String>> deletePackage(
            @PathVariable String id) {

        ResponseStructure<String> response =
                service.deletepackagebyid(id);

        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}