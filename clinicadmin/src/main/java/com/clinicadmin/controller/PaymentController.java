package com.clinicadmin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PaymentDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PaymentService;

@RestController
@RequestMapping("/clinic-admin")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/createPayment")
    public ResponseEntity<Response> createPayment(
            @RequestBody PaymentDTO dto) {

        Response response = paymentService.createPayment(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/getAllPaymentsUsingClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getAllPayments(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        Response response = paymentService.getAllPayments(clinicId, branchId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/getPaymentByUsingClinicIdBranchIdAndBillingId/{clinicId}/{branchId}/{billingId}")
    public ResponseEntity<Response> getPaymentByBillingId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String billingId) {

        Response response = paymentService.getPaymentByBillingId(
                clinicId,
                branchId,
                billingId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @GetMapping("/getAllPayments")
    public ResponseEntity<Response> getAllPayments() {

        Response response = paymentService.getAllPayments();

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PutMapping("/updatePaymentByUsingBillingId/{billingId}")
    public ResponseEntity<Response> updatePayment(
            @PathVariable String billingId,
            @RequestBody PaymentDTO dto) {

        Response response = paymentService.updatePayment(billingId, dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @GetMapping("/getPaymentByClinicIdBranchIdPatientIdAndBookingId/{clinicId}/{branchId}/{patientId}/{bookingId}")
    public ResponseEntity<Response> getPaymentByPatientAndBooking(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String patientId,
            @PathVariable String bookingId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByPatientAndBooking(
                        clinicId,
                        branchId,
                        patientId,
                        bookingId));
    }

    @DeleteMapping("/deletePaymentByUsingBillingId/{billingId}")
    public ResponseEntity<Response> deletePayment(
            @PathVariable String billingId) {

        Response response = paymentService.deletePayment(billingId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    
    @GetMapping("/PaymentInfo/bookingId/{bookingId}")    
    public Map<String,Double> getAllPayments(@PathVariable String bookingId) {
   return paymentService.retriveInfoBasedOnBookingId(bookingId);
    }
    @GetMapping("/getTreatmentScheduleWithPayment/{clinicId}/{branchId}/{bookingId}/{patientId}")
    public ResponseEntity<Response> getTreatmentScheduleWithPayment(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String bookingId,
            @PathVariable String patientId) {

        Response response = paymentService
                .getTreatmentScheduleWithPayment(
                        clinicId,
                        branchId,
                        bookingId,
                        patientId);

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}