package com.clinicadmin.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapyExercisesDTO;
import com.clinicadmin.service.TherapyExercisesService;

@RestController
@RequestMapping("/clinic-admin")
public class TherapyExercisesController {

    @Autowired
    private TherapyExercisesService service;

    // ================= CREATE =================
    @PostMapping("/createTherapyExercises")
    public ResponseEntity<ResponseStructure<TherapyExercisesDTO>> createTherapyExercises(
            @RequestBody TherapyExercisesDTO dto) {

        return ResponseEntity
                .status(201)
                .body(service.createTherapyExercises(dto));
    }

    // ================= UPDATE =================
    @PutMapping("/updateTherapyExercisesById/{therapyExercisesId}")
    public ResponseEntity<ResponseStructure<TherapyExercisesDTO>> updateTherapyExercises(
            @PathVariable String therapyExercisesId,
            @RequestBody TherapyExercisesDTO dto) {

        return ResponseEntity.ok(
                service.updateTherapyExercisesById(therapyExercisesId, dto));
    }

    // ================= GET BY ID =================
    @GetMapping("/getBytherapyExercisesId/{therapyExercisesId}")
    public ResponseEntity<ResponseStructure<TherapyExercisesDTO>> getById(
            @PathVariable String therapyExercisesId) {

        return ResponseEntity.ok(
                service.getTherapyExercisesById(therapyExercisesId));
    }

    // ================= GET BY clinicId + branchId =================
    @GetMapping("/getBytherapyExercisesClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<ResponseStructure<List<TherapyExercisesDTO>>> getByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                service.getByClinicIdAndBranchId(clinicId, branchId));
    }
    ////Get By clinic Id /////////
    @GetMapping("/getBytherapyExercisesClinicId/{clinicId}")
    public ResponseEntity<ResponseStructure<List<TherapyExercisesDTO>>> getByClinicId(
            @PathVariable String clinicId) {

        return ResponseEntity.ok(service.getByClinicId(clinicId));
    }

    // ================= GET BY clinicId + branchId + therapyExercisesId =================
    @GetMapping("/getBytherapyExercisesClinicIdAndBranchIdAndtherapyExercisesId/{clinicId}/{branchId}/{therapyExercisesId}")
    public ResponseEntity<ResponseStructure<TherapyExercisesDTO>> getByAll(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String therapyExercisesId) {

        return ResponseEntity.ok(
                service.getByClinicIdBranchIdAndTherapyId(
                        clinicId, branchId, therapyExercisesId));
    }

    // ================= DELETE =================
    @DeleteMapping("/deleteTherapyExercisesById/{therapyExercisesId}")
    public ResponseEntity<ResponseStructure<String>> deleteTherapyExercises(
            @PathVariable String therapyExercisesId) {

        return ResponseEntity.ok(
                service.deleteTherapyExercisesById(therapyExercisesId));
    }
}