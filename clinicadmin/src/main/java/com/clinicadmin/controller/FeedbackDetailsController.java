package com.clinicadmin.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.FeedbackDetailsDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.FeedbackDetailsServcie;

@RestController
@RequestMapping("/clinic-admin")

public class FeedbackDetailsController {

    @Autowired
    private FeedbackDetailsServcie service;
    
    
    @PostMapping("/createFeedback")
    public ResponseEntity<Response> createFeedback(
            @RequestBody FeedbackDetailsDTO feedbackDetailsDTO) {

        Response response = new Response();

        try {

            response =
                    service.createFeedback(
                            feedbackDetailsDTO);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @GetMapping("/getAllFeedback")
    public ResponseEntity<Response> getAllFeedbacks() {

        Response response = service.getAllFeedbacks();

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @GetMapping("/getFeedbackById/{id}")
    public ResponseEntity<Response> getFeedbackById(
            @PathVariable String id) {

        Response response =
                service.getFeedbackById(id);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @PutMapping("/updateFeedback/{id}")
    public ResponseEntity<Response> updateFeedback(
            @PathVariable String id,
            @RequestBody FeedbackDetailsDTO feedbackDetailsDTO) {

        Response response =
                service.updateFeedback(
                        id,
                        feedbackDetailsDTO);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @DeleteMapping("/deleteFeedback/{id}")
    public ResponseEntity<Response> deleteFeedback(
            @PathVariable String id) {

        Response response =
                service.deleteFeedback(id);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @GetMapping("/getAllByUsingClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getAllFeedbacksByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = service
                .getAllFeedbacksByClinicIdAndBranchId(
                        clinicId,
                        branchId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @GetMapping("/getFeedbackDetails/{clinicId}/{branchId}")
    public ResponseEntity<Response> getFeedbackDetails(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response =
                service.getFeedbackDetails(clinicId, branchId);

        // ✅ success case
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        // ❌ real system error
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
    
//    @GetMapping("/getDoctorFeedbackSummaryByCinicIdAndDoctorId/{clinicId}/{doctorId}")
//    public ResponseEntity<Response> getDoctorFeedbackSummary(
//            @PathVariable String clinicId,
//            @PathVariable String doctorId) {
//
//        return ResponseEntity.ok(
//        		service.getDoctorFeedbackSummary(
//                        clinicId,
//                        doctorId));
//    }
}
