package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "PatientConsentForm")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientConsentForm {
	@Id
	private String id;
	
	// Patient Details
	private String fullName;
	private String dateOfBirth;
	private String contactNumber;
	private String address;
	private String medicalRecordNumber;

	// Procedure Info
	private String procedureName;
	private String procedureDate;
	private String physicianName;

	// Consent Checkboxes
	private boolean informedAboutProcedure = true;
	private boolean understandsRisks = true;
	private boolean informedOfAlternatives = true;
	private boolean hadQuestionsAnswered = true;
	private boolean noGuaranteesGiven = true;

	// Anesthesia Consent
	private boolean anesthesiaConsent = true;
	private boolean understandsAnesthesiaRisks = true;

	// Photo/Video Consent
	private boolean consentForRecording = true;
	private boolean noConsentForRecording = true;

	// Consent Declaration
	private boolean understandsWithdrawalRight = true;
	private boolean consentGiven = true;

	// Signatures and Dates
	private String patientSignature;
	private String patientSignedDate;

	private String witnessSignature;
	private String witnessSignedDate;

	private String physicianSignature;
	private String physicianSignedDate;
	
	private String hospitalId;
	private String hospitalName;
	private String hospitalLogo;
	
	

}
