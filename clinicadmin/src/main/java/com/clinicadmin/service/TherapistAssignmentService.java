package com.clinicadmin.service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistAssignmentDTO;

public interface TherapistAssignmentService {

    Response assignTherapist(
            TherapistAssignmentDTO dto);

    Response getAssignedTherapistDetails(
            String therapistRecordId);

	Response updateAssignedStatus(String therapistRecordId, TherapistAssignmentDTO dto);



}