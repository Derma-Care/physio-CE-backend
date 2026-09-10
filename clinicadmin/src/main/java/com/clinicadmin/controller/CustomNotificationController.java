package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.CustomNotificationRequest;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.service.impl.CustomWhatsAppService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
@Slf4j
public class CustomNotificationController {

    private final CustomWhatsAppService customWhatsAppService;

    @PostMapping("/notifications/whatsapp/custom")
    public ResponseEntity<ResponseStructure<String>> sendCustomWhatsApp(
            @RequestBody CustomNotificationRequest request) {

        log.info("CustomWhatsApp request received clinicName={} branchName={} patients={}",
                request.getClinicName(),
                request.getBranchName(),
                request.getList() != null ? request.getList().size() : 0);

        return ResponseEntity.ok(customWhatsAppService.sendToAll(request));
    }
}