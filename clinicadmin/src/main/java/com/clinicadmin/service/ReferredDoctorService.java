package com.clinicadmin.service;

import com.clinicadmin.dto.ReferredDoctorDTO;
import com.clinicadmin.dto.Response;

public interface ReferredDoctorService {

    Response addReferralDoctor(ReferredDoctorDTO dto);

    Response getDoctorByReferralId(String referralId);

    Response getAllReferralDoctor();

	Response deleteReferralDoctoById(String id);

	Response updateReferralDoctorById(String id, ReferredDoctorDTO dto);

	Response getReferralDoctorrById(String id);

	Response getReferralDoctorsByClinicId(String clinicId);
}