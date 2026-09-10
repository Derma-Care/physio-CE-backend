package com.AdminService.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AdminService.dto.DoctorsDTO;
import com.AdminService.service.DoctorService;
import com.AdminService.util.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/addDoctor")
    public ResponseEntity<Response> addDoctor(@RequestBody DoctorsDTO doctorDto) {
        return doctorService.addDoctor(doctorDto);
    }

    @GetMapping("/getAllDoctors")
    public ResponseEntity<Response> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/getDoctorById/{doctorId}")
    public ResponseEntity<Response> getDoctorById(@PathVariable String doctorId) {
        return doctorService.getDoctorById(doctorId);
    }

    @PutMapping("/updateDoctor/{doctorId}")
    public ResponseEntity<Response> updateDoctorById(@PathVariable String doctorId,
                                                     @RequestBody DoctorsDTO doctorDto) {
        return doctorService.updateDoctorById(doctorId, doctorDto);
    }

    @DeleteMapping("/deleteDoctor/{doctorId}")
    public ResponseEntity<Response> deleteDoctorById(@PathVariable String doctorId) {
        return doctorService.deleteDoctorById(doctorId);
    }

    @DeleteMapping("/deleteDoctorsByClinic/{clinicId}")
    public ResponseEntity<Response> deleteDoctorsByClinic(@PathVariable String clinicId) {
        return doctorService.deleteDoctorsByClinic(clinicId);
    }

    @GetMapping("/getDoctorByClinicAndDoctorId/{clinicId}/{doctorId}")
    public ResponseEntity<Response> getDoctorByClinicAndDoctorId(@PathVariable String clinicId,
                                                                 @PathVariable String doctorId) {
        return doctorService.getDoctorByClinicAndDoctorId(clinicId, doctorId);
    }

    @GetMapping("/getDoctorsByHospitalId/{hospitalId}")
    public ResponseEntity<Response> getDoctorsByHospitalId(@PathVariable String hospitalId) {
        return doctorService.getDoctorsByHospitalId(hospitalId);
    }

    @GetMapping("/getDoctorsByHospitalIdAndBranchId/{hospitalId}/{branchId}")
    public ResponseEntity<Response> getDoctorsByHospitalIdAndBranchId(@PathVariable String hospitalId,
                                                                      @PathVariable String branchId) {
        return doctorService.getDoctorsByHospitalIdAndBranchId(hospitalId, branchId);
    }
}
