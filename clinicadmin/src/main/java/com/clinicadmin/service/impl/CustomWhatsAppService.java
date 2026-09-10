package com.clinicadmin.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.clinicadmin.dto.CustomNotificationRequest;
import com.clinicadmin.dto.CustomNotificationRequest.PatientEntry;
import com.clinicadmin.dto.ResponseStructure;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomWhatsAppService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.auth-key}")
    private String authKey;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.single-user-notification-template-id}")
    private String templateId;

    @Value("${whatsapp.base-url}")
    private String baseUrl;

    // =====================================================
    // PUBLIC API
    // =====================================================

    public ResponseStructure<String> sendToAll(CustomNotificationRequest request) {

        try {
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                return ResponseStructure.buildResponse(null,
                        "Title cannot be empty",
                        HttpStatus.BAD_REQUEST, 400);
            }

            if (request.getList() == null || request.getList().isEmpty()) {
                return ResponseStructure.buildResponse(null,
                        "Patient list cannot be empty",
                        HttpStatus.BAD_REQUEST, 400);
            }

            if (request.getBody() == null || request.getBody().isBlank()) {
                return ResponseStructure.buildResponse(null,
                        "Body cannot be empty",
                        HttpStatus.BAD_REQUEST, 400);
            }

            for (PatientEntry patient : request.getList()) {
                try {
                    sendToOne(patient, request);
                } catch (Exception ex) {
                    log.error("CustomWhatsApp failed patientId={} error={}",
                            patient.getPatientId(), ex.getMessage(), ex);
                }
            }

            return ResponseStructure.buildResponse(
                    "Notification dispatch initiated",
                    "WhatsApp notifications sent successfully",
                    HttpStatus.OK, 200);

        } catch (Exception ex) {
            log.error("CustomWhatsApp service error={}", ex.getMessage(), ex);
            return ResponseStructure.buildResponse(null,
                    "Internal server error: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, 500);
        }
    }

    // =====================================================
    // PER-PATIENT SEND
    // =====================================================

    private void sendToOne(PatientEntry patient, CustomNotificationRequest request) {

        String mobile = normalizeMobile(patient.getMobileNumber());

        if (!isValidMobile(mobile)) {
            log.warn("CustomWhatsApp skipped — invalid mobile patientId={}",
                    patient.getPatientId());
            return;
        }

        try {
            String variables = buildVariables(request, patient.getName());

            log.info("CustomWhatsApp variables={}", variables);

            String url = baseUrl + "/dev/whatsapp"
                    + "?authorization=" + authKey
                    + "&message_id=" + templateId
                    + "&phone_number_id=" + phoneNumberId
                    + "&numbers=" + mobile
                    + "&variables_values=" + variables;

            log.info("CustomWhatsApp url={}", url);

            String response = restTemplate.getForObject(url, String.class);

            log.info("CustomWhatsApp sent patientId={} mobile={} response={}",
                    patient.getPatientId(), mobile, response);

        } catch (Exception ex) {
            log.error("CustomWhatsApp send failed patientId={} error={}",
                    patient.getPatientId(), ex.getMessage(), ex);
        }
    }

    // =====================================================
    // VARIABLE BUILDER
    // {{1}} → title (bold)
    // {{2}} → greeting  → Hello, Patient Name
    // {{3}} → body
    // {{4}} → clinicName
    // {{5}} → branchName
    //
    // Template must be:
    // Greetings 👋
    // *{{1}}*
    // {{2}}
    // {{3}}
    // 🏥 Clinic : {{4}}
    // 📍 Branch : {{5}}
    // Thank you for choosing us 🙏
    // =====================================================

    private String buildVariables(CustomNotificationRequest request, String patientName) {

        String greeting = "Hello, " + safe(patientName, "Patient");
        String fullBody = safe(request.getBody(), "");

        return String.join("|",
                safe(request.getTitle(), "Notification"),  // {{1}}
                greeting,                                  // {{2}}
                fullBody,                                  // {{3}}
                safe(request.getClinicName(), "Clinic"),   // {{4}}
                safe(request.getBranchName(), "Branch")    // {{5}}
        );
    }

    // =====================================================
    // MOBILE UTILS
    // =====================================================

    private String normalizeMobile(String mobile) {
        return mobile == null ? null : mobile.replaceAll("\\D", "");
    }

    private boolean isValidMobile(String mobile) {
        return mobile != null && mobile.matches("^[6-9]\\d{9}$");
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}