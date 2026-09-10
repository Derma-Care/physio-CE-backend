package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.service.DashboardAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

	@Autowired
    private  DashboardAnalyticsService analyticsService;

    @GetMapping("/getDashboardAnalytics/{clinicId}/{branchId}")
    public ResponseEntity<Response> getDashboard(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                analyticsService.getDashboard(
                        clinicId,
                        branchId));
    }
}
