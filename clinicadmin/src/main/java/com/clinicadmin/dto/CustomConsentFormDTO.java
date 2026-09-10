package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomConsentFormDTO {
	private String id;
	private String hospitalId;
	private String subServiceid;
	private String subServiceName;
	private String consentFormType; // Generic or procedureConsent (1,2)
	private List<QuestionHeading> consentFormQuestions;
}
