package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.AddNewTestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.AddNewTestService;


@RestController
@RequestMapping("/clinic-admin")
public class AddNewTestController {

    @Autowired
    private AddNewTestService addNewTestService;

    // ===========================
    // Add Test
    // ===========================
    @PostMapping("/addNewTest")
    public ResponseEntity<Response> addTest(@RequestBody AddNewTestDTO dto) {

        return ResponseEntity.ok(addNewTestService.addTest(dto));
    }

    // ===========================
    // Update Test
    // ===========================
    @PutMapping("/updateNewTest/{id}")
    public ResponseEntity<Response> updateTest(
            @PathVariable String id,
            @RequestBody AddNewTestDTO dto) {

        return ResponseEntity.ok(addNewTestService.updateTest(id, dto));
    }

    // ===========================
    // Delete Test
    // ===========================
    @DeleteMapping("/deleteNewTest/{id}")
    public ResponseEntity<Response> deleteTest(@PathVariable String id) {

        return ResponseEntity.ok(addNewTestService.deleteTest(id));
    }

    // ===========================
    // Get Test By Id
    // ===========================
    @GetMapping("/getByUsingTestId/{id}")
    public ResponseEntity<Response> getById(@PathVariable String id) {

        return ResponseEntity.ok(addNewTestService.getById(id));
    }

    // ===========================
    // Get All Tests
    // ===========================
    @GetMapping("/getAllTest")
    public ResponseEntity<Response> getAll() {

        return ResponseEntity.ok(addNewTestService.getAll());
    }

    // ===========================
    // Get By Clinic Id
    // ===========================
    @GetMapping("/getByTestClinicId/{clinicId}")
    public ResponseEntity<Response> getByClinicId(
            @PathVariable String clinicId) {

        return ResponseEntity.ok(addNewTestService.getByClinicId(clinicId));
    }

    // ===========================
    // Get By Clinic Id & Branch Id
    // ===========================
    @GetMapping("/getByTestClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                addNewTestService.getByClinicIdAndBranchId(clinicId, branchId));
    }

    
 // ===========================
 // Get By Clinic Id, Branch Id & Test Id
 // ===========================
 @GetMapping("/getByClinicIdBranchIdAndTestId/{clinicId}/{branchId}/{testId}")
 public ResponseEntity<Response> getByClinicIdBranchIdAndTestId(
         @PathVariable String clinicId,
         @PathVariable String branchId,
         @PathVariable String testId) {

     return ResponseEntity.ok(
             addNewTestService.getByClinicIdBranchIdAndTestId(clinicId, branchId, testId));
 }
//===========================
}