package com.AdminService.dto;
import java.util.List;
import java.util.Map;

import com.AdminService.entity.Branch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicDTO {

	private String hospitalId;

	@NotBlank(message = "Hospital name is required")
	private String name;

	@NotBlank(message = "Address is required")
	private String address;

	@NotBlank(message = "City is required")
	private String city;

	private double hospitalOverallRating;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
	private String contactNumber;

	@NotBlank(message = "Opening time is required")
	private String openingTime;

	@NotBlank(message = "Closing time is required")
	private String closingTime;

	@NotBlank(message = "Hospital logo (Base64) is required")
	private String hospitalLogo;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
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
	@NotBlank(message = "Consultation expiration is required")
	private String consultationExpiration;

	@NotBlank(message = "subscription is required")
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
    private Map<String, List<String>> permissions;
	private String instagramHandle;
	private String twitterHandle;
	private String facebookHandle;
	private String status;
	private String fcmToken;
	private String loyaltyPoints;
    private String location;
	private String subscriptionDates;
	private String subscriptionStartDate;
	private String subscriptionEndDate;
	private String server;
}