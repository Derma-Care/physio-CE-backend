package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TestOrderDTO;
import com.clinicadmin.service.TestOrderService;

@RestController
@RequestMapping("/clinic-admin")
public class TestOrderController {

    @Autowired
    private TestOrderService service;

    // Create Test Order
    @PostMapping("/CreateTestOrder")
    public ResponseEntity<Response> create(@RequestBody TestOrderDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    // Update Test Order
    @PutMapping("/updateTestOrderById/{id}")
    public ResponseEntity<Response> update(@PathVariable String id,
                                           @RequestBody TestOrderDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // Delete Test Order
    @DeleteMapping("DeleteTestOrderById/{id}")
    public ResponseEntity<Response> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    // Get All Test Orders
    @GetMapping
    public ResponseEntity<Response> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get Test Order By Id
    @GetMapping("/getTestOrderById/{id}")
    public ResponseEntity<Response> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Get By ClinicId & BranchId
    @GetMapping("/getTestOrderByCliniIdandBranchId/{clinicId}/{branchId}")
    public ResponseEntity<Response> getByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                service.getByClinicIdAndBranchId(clinicId, branchId));
    }

    // Get By ClinicId + BranchId + TestOrderId
    @GetMapping("/getTestOrderByClinicIdBranchIdTestOrderId/{clinicId}/{branchId}/{id}")
    public ResponseEntity<Response> getByClinicIdBranchIdAndId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String id) {

        return ResponseEntity.ok(
                service.getByClinicIdBranchIdAndTestOrderId(
                        clinicId,
                        branchId,
                        id));
    }
    @GetMapping("/getByClinicBranchPatientBooking/{clinicId}/{branchId}/{patientId}/{bookingId}")
    public ResponseEntity<Response> getByClinicBranchPatientBooking(
          @PathVariable String clinicId,
          @PathVariable String branchId,
          @PathVariable String patientId,
          @PathVariable String bookingId) {

      return ResponseEntity.ok(
    		  service.getByClinicBranchPatientBooking(
                      clinicId, branchId, patientId, bookingId));
    }

}