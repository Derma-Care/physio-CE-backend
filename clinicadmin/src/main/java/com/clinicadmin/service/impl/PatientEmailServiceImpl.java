package com.clinicadmin.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PatientEmailDTO;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.service.EmailService;
import com.clinicadmin.service.PatientEmailService;
import com.clinicadmin.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientEmailServiceImpl implements PatientEmailService {

    private static final Logger log = LoggerFactory.getLogger(PatientEmailServiceImpl.class);

    private final EmailService emailService;
    private final S3Service s3Service;
    private final CustomerOnboardingRepository customerOnbordingRepository; // ✅ new dependency

    @Override
    public void sendPatientEmail(PatientEmailDTO dto) {
        try {
            if (dto == null) {
                log.warn("Patient email request is empty");
                return;
            }
            if (dto.getPatientMail() == null || dto.getPatientMail().isBlank()) {
                log.warn("Patient email is required");
                return;
            }
            if (dto.getPatientName() == null || dto.getPatientName().isBlank()) {
                log.warn("Patient name is required");
                return;
            }
            if (dto.getPdfFile() == null || dto.getPdfFile().isBlank()) {
                log.warn("PDF file is required");
                return;
            }

            String title = (dto.getTitle() == null || dto.getTitle().isBlank())
                    ? "Patient Document"
                    : dto.getTitle();

            String body = """
                    Dear %s,
                    Please find the attached PDF document.
                    Thank you.
                    Regards,
                    Clinic Team
                    """.formatted(dto.getPatientName());

            String signedUrl = s3Service.generateSignedUrl(dto.getPdfFile());

            // ✅ resolve dynamic clinic name — placed right before it's needed
            String clinicName = resolveClinicName(dto.getHospitalId());

            emailService.sendPatientPdfEmail(
                    dto.getPatientMail(),
                    dto.getPatientName(),
                    title,
                    body,
                    signedUrl,
                    clinicName);   // ✅ pass resolved name instead of null

            log.info("Patient PDF email sent successfully to {}", dto.getPatientMail());
        } catch (Exception e) {
            log.error("Error while sending patient PDF email : {}", e.getMessage(), e);
        }
    }

    // helper method — add this below sendPatientEmail, still inside the class
    private String resolveClinicName(String hospitalId) {
        if (hospitalId == null || hospitalId.isBlank()) {
            log.warn("hospitalId not provided; email will use default brand name");
            return null;
        }

        List<CustomerOnbording> customers = customerOnbordingRepository.findByHospitalId(hospitalId);

        if (customers == null || customers.isEmpty()) {
            log.warn("No records found for hospitalId {}; using default brand name", hospitalId);
            return null;
        }

        String hospitalName = customers.get(0).getHospitalName();

        if (hospitalName == null || hospitalName.isBlank()) {
            log.warn("hospitalName is blank for hospitalId {}; using default brand name", hospitalId);
            return null;
        }

        return hospitalName;
    }
}