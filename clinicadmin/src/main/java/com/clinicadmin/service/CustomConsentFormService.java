package com.clinicadmin.service;

import com.clinicadmin.dto.CustomConsentFormDTO;
import com.clinicadmin.dto.Response;

public interface CustomConsentFormService {
//	public Response addCustomConsentForm(String hospitalId, String consentFormType, CustomConsentFormDTO dto);

//	Response updateCustomConsentForm(String hospitalId, String consentFormType, CustomConsentFormDTO dto);

	Response getProcedureConsentForm(String hospitalId, String subServiceId);

	Response getConsentForm(String hospitalId, String consentFormType);

	Response getAllConsentFormsByHospital(String hospitalId);

	Response deleteConsentFormById(String formId);
}