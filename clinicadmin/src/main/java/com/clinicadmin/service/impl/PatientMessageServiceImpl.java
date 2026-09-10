package com.clinicadmin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PatientInfoDTO;
import com.clinicadmin.dto.PatientMessageDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.EmailService;
import com.clinicadmin.service.PatientMessageService;

@Service
public class PatientMessageServiceImpl
        implements PatientMessageService {

    @Autowired
    private EmailService emailService;

    @Override
    public Response savePatientMessage(
            PatientMessageDTO dto) {

        Response response =
                new Response();

        try {

            if (dto.getList() != null
                    && !dto.getList().isEmpty()) {

                for (PatientInfoDTO patient
                        : dto.getList()) {

                    if (patient.getPatientEmail() != null
                            && !patient.getPatientEmail()
                                       .isBlank()) {

                        emailService.sendPatientEmail(

                                patient.getPatientEmail(),

                                patient.getPatientName(),

                                dto.getClinicName(),

                                dto.getBranchName(),

                                dto.getTitle(),

                                dto.getBody()
                        );
                    }
                }
            }

            response.setSuccess(
                    true);

            response.setMessage(
                    "Emails sent successfully");

            response.setData(
                    dto);

            response.setStatus(
                    200);

        } catch (Exception e) {

            response.setSuccess(
                    false);

            response.setMessage(
                    e.getMessage());

            response.setStatus(
                    500);
        }

        return response;
    }
}