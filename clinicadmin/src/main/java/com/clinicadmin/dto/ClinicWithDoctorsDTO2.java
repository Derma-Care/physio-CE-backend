package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicWithDoctorsDTO2 {

	private String hospitalId;

	private String name;
	private String address;

	private String city;

	private double hospitalOverallRating;

	private String contactNumber;

	private String openingTime;

	private String closingTime;

	private String hospitalLogo;

	private String emailAddress;

	private String website;

	private String licenseNumber;

	private String issuingAuthority;

	private String contractorDocuments;

	private String hospitalDocuments;

	private boolean recommended;

	// Registration Certificates
	private String clinicalEstablishmentCertificate;
	private String businessRegistrationCertificate;

	// Clinic Type Info
	private String clinicType; // Proprietorship, Partnership, LLP, Pvt Ltd

	// Medicines Handling
	private String medicinesSoldOnSite;
	private String drugLicenseCertificate; // Base64
	private String drugLicenseFormType; // Form 20 or 21

	// Pharmacist Info
	private String hasPharmacist; // Yes/No/NA
	private String pharmacistCertificate; // Base64

	// Other Licenses
	private String biomedicalWasteManagementAuth; // SPCB
	private String tradeLicense; // Municipality
	private String fireSafetyCertificate; // Fire Dept
	private String professionalIndemnityInsurance; // Insurance
	private String gstRegistrationCertificate;
	private String consultationExpiration;

	private String subscription;

	// Allow multiple documents for 'others'

	private List<String> others;

	private int freeFollowUps = 0;
	private double latitude;
	private double longitude;
	private int nabhScore;
	private String branch;

	private String walkthrough;
	private List<Branch> branches;

	// ClinicCredentials.java
	private String role;
	private Map<String, List<String>> permissions;;
	private String instagramHandle;
	private String twitterHandle;
	private String facebookHandle;
	private DoctorsDTO doctors;

}
