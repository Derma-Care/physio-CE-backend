package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.PhysiotherapyRecordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.GenerateTableService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class GenerateTableController {

    private final GenerateTableService service;

    @PostMapping("/generate-table")
    public ResponseEntity<Response> generateTable(
            @RequestBody PhysiotherapyRecordDTO request) {

        Response response = service.generateTable(request);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}