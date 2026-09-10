package com.dermacare.bookingService.service.Impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.dermacare.bookingService.dto.BookingRequset;
import com.dermacare.bookingService.dto.BranchDTO;
import com.dermacare.bookingService.feign.AdminServiceClient;
import com.dermacare.bookingService.util.ResponseStructure;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WhatsAppService {

    private final WebClient webClient;
    private final AdminServiceClient adminServiceClient;

    @Value("${whatsapp.auth-key}")
    private String authKey;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.booking-confirmation-template-id}")
    private String bookingConfirmationTemplateId;

    public WhatsAppService(
            WebClient.Builder webClientBuilder,
            AdminServiceClient adminServiceClient,
            @Value("${whatsapp.base-url}") String baseUrl
    ) {
        this.adminServiceClient = adminServiceClient;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    private static final String BOOKING_TEMPLATE = "appointment_confirmation";

    public void sendBookingConfirmation(BookingRequset booking) {

        try {

            // =====================================================
            // MOBILE NUMBER
            // =====================================================

            String mobile = booking.getPatientMobileNumber();

            if (mobile == null || mobile.trim().isEmpty()) {
                mobile = booking.getMobileNumber();
            }

            if (mobile == null || mobile.trim().isEmpty()) {
                log.warn("Mobile number not found for booking {}", booking.getBookingId());
                return;
            }

            String normalizedMobile = mobile.replaceAll("[^0-9]", "");

            if (normalizedMobile.startsWith("91") && normalizedMobile.length() == 12) {
                normalizedMobile = normalizedMobile.substring(2);
            }

            final String cleanMobile = normalizedMobile;

            // =====================================================
            // FETCH BRANCH DETAILS
            // =====================================================

            BranchDTO branch = null;

            try {
                ResponseEntity<ResponseStructure<BranchDTO>> response =
                        adminServiceClient.getBranchById(booking.getBranchId());

                if (response != null
                        && response.getBody() != null
                        && response.getBody().getData() != null) {
                    branch = response.getBody().getData();
                }

            } catch (Exception e) {
                log.error("Branch fetch failed: {}", e.getMessage(), e);
            }

            // =====================================================
            // DEFAULT VALUES
            // =====================================================

            String clinicName = "Clinic";
            String branchName = "";
            String whatsappNumber = "";
            String email = "";
            String locationUrl = "";

            if (branch != null) {

                clinicName = nullSafe(branch.getHospitalName(), "Clinic");
                branchName = nullSafe(branch.getBranchName(), "");
                whatsappNumber = nullSafe(branch.getContactNumber(), "");
                email = nullSafe(branch.getEmail(), "");

                if (branch.getLatitude() != null
                        && !branch.getLatitude().isBlank()
                        && branch.getLongitude() != null
                        && !branch.getLongitude().isBlank()) {

                    locationUrl =
                            "https://www.google.com/maps/search/?api=1&query="
                                    + branch.getLatitude()
                                    + ","
                                    + branch.getLongitude();
                }
            }

            // =====================================================
            // TEMPLATE VARIABLES
            // =====================================================

            final String variables = String.join("|",
                    clinicName,
                    nullSafe(booking.getName(), "Patient"),
                    nullSafe(booking.getDoctorName(), "Doctor"),
                    nullSafe(booking.getServiceDate(), ""),
                    nullSafe(booking.getServicetime(), ""),
                    nullSafe(booking.getBookingId(), ""),
                    branchName,
                    whatsappNumber,
                    email,
                    locationUrl
            );

            // =====================================================
            // LOGGING
            // =====================================================

            log.info("Sending WhatsApp | Template={} | BookingId={} | Mobile={}",
                    BOOKING_TEMPLATE,
                    booking.getBookingId(),
                    cleanMobile);

            log.info("WhatsApp Variables={}", variables);

            // =====================================================
            // API CALL
            // =====================================================

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/dev/whatsapp")
                            .queryParam("authorization", authKey)
                            .queryParam("message_id", bookingConfirmationTemplateId)
                            .queryParam("phone_number_id", phoneNumberId)
                            .queryParam("numbers", cleanMobile)
                            .queryParam("variables_values", variables)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("WhatsApp sent successfully: {}", response);

        } catch (Exception e) {
            log.error("WhatsApp booking failed: {}", e.getMessage(), e);
            throw new RuntimeException("WhatsApp booking failed", e);
        }
    }

    // =====================================================
    // SAFE METHOD
    // =====================================================

    private String nullSafe(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty())
                ? value.trim()
                : defaultValue;
    }
}