package com.clinicadmin.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.AnalyticsService;
@RestController
@RequestMapping("/clinic-admin")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;
    @GetMapping("/getDoctorReferralAnalytics/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getDoctorReferralAnalytics(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                analyticsService.getDoctorReferralAnalytics(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }

    @GetMapping("/getDoctorReferralAnalyticsCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getDoctorReferralAnalyticsCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                analyticsService.getDoctorReferralAnalytics(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }
    
    @GetMapping("/getDoctorReferralPatientDetails/{clinicId}/{branchId}/{referralId}")
    public ResponseEntity<Response> getDoctorReferralPatientDetails(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String referralId) {

        Response response =
                analyticsService.getDoctorReferralPatientDetails(
                        clinicId,
                        branchId,
                        referralId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @GetMapping("/getReferralChannels/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getReferralChannels(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                analyticsService.getReferralChannels(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }
    
    @GetMapping("/getReferralChannelsCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getReferralChannelsCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                analyticsService.getReferralChannels(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }
    
    @GetMapping("/getReferralChannelPatientDetails/{clinicId}/{branchId}/{channel}")
    public ResponseEntity<Response>
            getReferralChannelPatientDetails(
                    @PathVariable String clinicId,
                    @PathVariable String branchId,
                    @PathVariable String channel) {

        return ResponseEntity.ok(
                analyticsService
                        .getReferralChannelPatientDetails(
                                clinicId,
                                branchId,
                                channel));
    }
    
    @GetMapping("/getReferralSummary/{clinicId}/{branchId}/{type}")
    public ResponseEntity<Response> getReferralSummary(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable Integer type) {

        return ResponseEntity.ok(
                analyticsService.getReferralSummary(
                        clinicId,
                        branchId,
                        type,
                        null,
                        null));
    }
    @GetMapping("/getReferralSummaryCustom/{clinicId}/{branchId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getReferralSummaryCustom(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(
                analyticsService.getReferralSummary(
                        clinicId,
                        branchId,
                        5,
                        startDate,
                        endDate));
    }
}