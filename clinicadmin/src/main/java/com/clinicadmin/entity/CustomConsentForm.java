package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.QuestionHeading;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "custom_consent_form")
public class CustomConsentForm {
	@Id
	private String id;
	private String hospitalId;
	private String subServiceid;
	private String subServiceName;
	private String consentFormType; // Generic or procedureConsent (1,2)
	private List<QuestionHeading> consentFormQuestions;

}
