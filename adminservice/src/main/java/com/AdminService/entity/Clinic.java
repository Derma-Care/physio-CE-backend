package com.AdminService.entity;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clinics") // MongoDB collection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Clinic {
    @Id
    private String id;
    private String name;
    private String hospitalId;
    private String address;
    private String city;
    private String contactNumber;
    private String openingTime;
    private String closingTime;

    private byte[] hospitalLogo;
    private String emailAddress;
    private String website;
    private String licenseNumber;
    private String issuingAuthority;
 
 
    private byte[] contractorDocuments;

 
    private byte[] hospitalDocuments;

    private boolean recommended;
    private double hospitalOverallRating;

    private byte[] clinicalEstablishmentCertificate;

   
    private byte[] businessRegistrationCertificate;

    // Clinic Type: Proprietorship, Partnership, LLP, Pvt Ltd
    private String clinicType;

    // Medicines Handling
    private String medicinesSoldOnSite;  // Yes / No

  
    private byte[] drugLicenseCertificate; // Only if medicinesSoldOnSite is true

 
    private byte[] drugLicenseFormType; // Form 20 or 21

    // Pharmacist info
    private String hasPharmacist; // Yes / No / NA

  
    private byte[] pharmacistCertificate;

    // Other Licenses
    private byte[] biomedicalWasteManagementAuth; // SPCB

    private byte[] tradeLicense; // Municipality

    
    private byte[] fireSafetyCertificate; // Local Fire Department

  
    private byte[] professionalIndemnityInsurance; // Insurance company

  
    private byte[] gstRegistrationCertificate;

    private String consultationExpiration;
    
    private String subscription;

    private List<byte[]> others;
    
    private int freeFollowUps;
    private double latitude;
    private double longitude;
    private String walkthrough;
    private int nabhScore;
    private String branch;
    
    private List<Branch> branches;
    private String role;    
    private Map<String, List<String>> permissions;

    private String instagramHandle;
    private String twitterHandle;
    private String facebookHandle;
	private String status ;
	private String createdAt;
	private String loyaltyPoints;
    private String location;
    private String subscriptionDates;
    private String subscriptionStartDate;
    private String subscriptionEndDate;
    private String server;

}
