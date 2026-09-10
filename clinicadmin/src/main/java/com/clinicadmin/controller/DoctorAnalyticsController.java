package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.DoctorAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class DoctorAnalyticsController {

    private final DoctorAnalyticsService doctorAnalyticsService;

    // Today / Weekly / Monthly / Yearly
    @GetMapping("/getDoctorDashboard/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getDoctorDashboard(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                doctorAnalyticsService.getDoctorDashboard(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("/getDoctorDashboardCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDoctorDashboardCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                doctorAnalyticsService.getDoctorDashboard(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }

    // Today / Weekly / Monthly / Yearly
    @GetMapping("/getDoctorPatients/{clinicId}/{branchId}/{doctorId}/{type}")
    public ResponseEntity<Response> getDoctorPatients(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String doctorId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                doctorAnalyticsService.getDoctorPatients(
                        clinicId,
                        branchId,
                        doctorId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("/getDoctorPatientsCustom/{clinicId}/{branchId}/{doctorId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDoctorPatientsCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String doctorId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                doctorAnalyticsService.getDoctorPatients(
                        clinicId,
                        branchId,
                        doctorId,
                        5,
                        startDate,
                        endDate));
    }
}