package com.clinicadmin.controller;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapyServiceDTO;
import com.clinicadmin.service.TherapyServiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class TherapyServiceController {
	
    @Autowired
    private  TherapyServiceService service;

    // ✅ CREATE
    @PostMapping("/createTherapyService")
    public ResponseEntity<Response> createTherapyService(@RequestBody TherapyServiceDTO dto) {
        Response res = service.createTherapy(dto);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    // ✅ GET by clinicId + branchId
    @GetMapping("/getByTherapyServiceClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getByTherapyServiceClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response res = service.getByClinicAndBranch(clinicId, branchId);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    // ✅ GET by id + clinicId + branchId
    @GetMapping("/getByTherapyServiceIdClinicIdAndBranchId/{id}/{clinicId}/{branchId}")
    public ResponseEntity<Response> getByTherapyServiceIdClinicIdAndBranchId(
            @PathVariable String id,
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response res = service.getByIdClinicBranch(id, clinicId, branchId);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    //get clinic id //
    @GetMapping("/getByTherapyServiceClinicId/{clinicId}")
    public ResponseEntity<Response> getByClinicId(@PathVariable String clinicId) {
        return ResponseEntity.ok(service.getByClinicId(clinicId));
    }

    // ✅ UPDATE 
    @PutMapping("/updateByTherapyServieId/{id}")
    public ResponseEntity<Response> updateByTherapyServieId(
            @PathVariable String id,
            @RequestBody TherapyServiceDTO dto) {

        Response res = service.updateTherapyById(id, dto);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    // ✅ DELETE 
    @DeleteMapping("/deleteByTherapyServiceId/{id}")
    public ResponseEntity<Response> deleteByTherapyServiceId(
            @PathVariable String id) {

        Response res = service.deleteTherapyById(id);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
 //  NEW API → GET Therapy + Exercises 🔥
    @GetMapping("/getTherapyServiceWithExercises/{id}/{clinicId}/{branchId}")
    public ResponseEntity<Response> getTherapyServiceWithExercises(
            @PathVariable String id,
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response res = service.getTherapyWithExercises(id, clinicId, branchId);
        return ResponseEntity.status(res.getStatus()).body(res);
}
}