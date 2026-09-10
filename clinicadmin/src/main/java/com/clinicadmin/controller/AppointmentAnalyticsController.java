package com.clinicadmin.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.AppointmentAnalyticsService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class AppointmentAnalyticsController {
    private final AppointmentAnalyticsService appointmentAnalyticsService;
    // Today / Weekly / Monthly / Yearly
    @GetMapping("/getDoctorAnalytics/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getDoctorAnalytics(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                appointmentAnalyticsService.getAppointmentAnalytics(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }

    // Custom Date Range
    @GetMapping("/getDoctorAnalyticsCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDoctorAnalyticsCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                appointmentAnalyticsService.getAppointmentAnalytics(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }
    
    @GetMapping("/getAppointmentSummary/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getAppointmentSummary(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                appointmentAnalyticsService
                        .getAppointmentSummary(
                                clinicId,
                                branchId,
                                type,
                                null,
                                null));
    }

    @GetMapping("/getAppointmentSummaryCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getAppointmentSummaryCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                appointmentAnalyticsService
                        .getAppointmentSummary(
                                clinicId,
                                branchId,
                                5,
                                startDate,
                                endDate));
    }
}