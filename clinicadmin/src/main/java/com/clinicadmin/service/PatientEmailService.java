package com.clinicadmin.service;

import com.clinicadmin.dto.PatientEmailDTO;

public interface PatientEmailService {

    void sendPatientEmail(
            PatientEmailDTO dto);
}