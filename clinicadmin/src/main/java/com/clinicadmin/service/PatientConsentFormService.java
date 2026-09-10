package com.clinicadmin.service;

import com.clinicadmin.dto.PatientConsentFormDTO;
import com.clinicadmin.dto.Response;

public interface PatientConsentFormService {

	public Response getPatientDetailsForFormUsingBooking(String bookingId, String patientId, String mobileNumber);
	public Response updatePatientConsentForm(String id, PatientConsentFormDTO dto);

}
