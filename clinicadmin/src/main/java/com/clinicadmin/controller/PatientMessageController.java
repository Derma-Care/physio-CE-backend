package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.PatientMessageDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PatientMessageService;

@RestController
@RequestMapping("/clinic-admin")
public class PatientMessageController {

    @Autowired
    private PatientMessageService patientMessageService;

    @PostMapping("/savePatientMessage")
    public ResponseEntity<Response> savePatientMessage(
            @RequestBody PatientMessageDTO dto) {

        Response response =
                patientMessageService
                        .savePatientMessage(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}