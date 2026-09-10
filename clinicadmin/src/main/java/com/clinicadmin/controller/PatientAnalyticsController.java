package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PatientAnalyticsRequest;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PatientAnalyticsService;

/**
 * POST /patient-analytics/{clinicId}/{branchId}/1   -> Today
 * POST /patient-analytics/{clinicId}/{branchId}/2   -> Week
 * POST /patient-analytics/{clinicId}/{branchId}/3   -> Month
 * POST /patient-analytics/{clinicId}/{branchId}/4   -> Year
 * POST /patient-analytics/{clinicId}/{branchId}/5   -> Custom (body must carry
 *                                                       startDate + endDate)
 *
 * Body is optional for 1-4 (only `search` is read from it); required for 5.
 */
@RestController
@RequestMapping("/clinic-admin")
public class PatientAnalyticsController {

    private static final int CUSTOM = 5;

    @Autowired
    private PatientAnalyticsService patientAnalyticsService;

    @PostMapping("/patient-analytics/{clinicId}/{branchId}/{filterType}")
    public ResponseEntity<Response> getPatientAnalytics(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable int filterType,
            @RequestBody(required = false) PatientAnalyticsRequest request) {

        PatientAnalyticsRequest body = request != null ? request : new PatientAnalyticsRequest();

        Response response = (filterType == CUSTOM)
                ? patientAnalyticsService.getCustomPatientAnalytics(clinicId, branchId, body)
                : patientAnalyticsService.getPatientAnalytics(clinicId, branchId, filterType, body);

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}