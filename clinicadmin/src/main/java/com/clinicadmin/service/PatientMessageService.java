package com.clinicadmin.service;

import com.clinicadmin.dto.PatientMessageDTO;
import com.clinicadmin.dto.Response;

public interface PatientMessageService {

    Response savePatientMessage(
            PatientMessageDTO dto
    );
}