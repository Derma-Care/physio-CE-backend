package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.ReferralAnalyticsService;

@RestController
@RequestMapping("/clinic-admin")
public class ReferralAnalyticsController {

    @Autowired
    private ReferralAnalyticsService referralAnalyticsService;

    /** Default, overall (option=5), full detail with patients */
    @GetMapping("/referralAnalytics/{clinicId}/{branchId}")
    public ResponseEntity<Response> getReferralAnalyticsDefault(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = referralAnalyticsService.getReferralAnalytics(clinicId, branchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Preset period, full detail. option: 1=day, 2=week, 3=month, 4=year, 5=overall */
    @GetMapping("/referralAnalytics/{clinicId}/{branchId}/{option}")
    public ResponseEntity<Response> getReferralAnalytics(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable int option) {

        Response response = referralAnalyticsService.getReferralAnalytics(clinicId, branchId, option);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Custom range, full detail */
    @GetMapping("/referralAnalytics/custom/{clinicId}/{branchId}/{start}/{end}")
    public ResponseEntity<Response> getReferralAnalyticsByCustomRange(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String start,
            @PathVariable String end) {

        Response response = referralAnalyticsService.getReferralAnalyticsByCustomRange(clinicId, branchId, start, end);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Default, overall (option=5), summary (no nested patients) — for the dashboard table */
    @GetMapping("/referralAnalytics/summary/{clinicId}/{branchId}")
    public ResponseEntity<Response> getReferralAnalyticsSummaryDefault(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = referralAnalyticsService.getReferralAnalyticsSummary(clinicId, branchId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Preset period, summary. option: 1=day, 2=week, 3=month, 4=year, 5=overall */
    @GetMapping("/referralAnalytics/summary/{clinicId}/{branchId}/{option}")
    public ResponseEntity<Response> getReferralAnalyticsSummary(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable int option) {

        Response response = referralAnalyticsService.getReferralAnalyticsSummary(clinicId, branchId, option);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Custom range, summary */
    @GetMapping("/referralAnalytics/summary/custom/{clinicId}/{branchId}/{start}/{end}")
    public ResponseEntity<Response> getReferralAnalyticsSummaryByCustomRange(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String start,
            @PathVariable String end) {

        Response response = referralAnalyticsService.getReferralAnalyticsSummaryByCustomRange(clinicId, branchId, start, end);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Drill-down, default overall. Pass a doctorRefCode, or "self". */
    @GetMapping("/referralAnalytics/{clinicId}/{branchId}/doctor/{doctorRefCode}")
    public ResponseEntity<Response> getPatientsByReferringDoctorDefault(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String doctorRefCode) {

        Response response = referralAnalyticsService.getPatientsByReferringDoctor(clinicId, branchId, doctorRefCode);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Drill-down, preset period. option: 1=day, 2=week, 3=month, 4=year, 5=overall */
    @GetMapping("/referralAnalytics/{clinicId}/{branchId}/doctor/{doctorRefCode}/{option}")
    public ResponseEntity<Response> getPatientsByReferringDoctor(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String doctorRefCode,
            @PathVariable int option) {

        Response response = referralAnalyticsService.getPatientsByReferringDoctor(clinicId, branchId, doctorRefCode, option);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /** Drill-down, custom range */
    @GetMapping("/referralAnalytics/custom/{clinicId}/{branchId}/doctor/{doctorRefCode}/{start}/{end}")
    public ResponseEntity<Response> getPatientsByReferringDoctorCustomRange(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String doctorRefCode,
            @PathVariable String start,
            @PathVariable String end) {

        Response response = referralAnalyticsService.getPatientsByReferringDoctorCustomRange(clinicId, branchId, doctorRefCode, start, end);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}