package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PharmacistDTO {
	private String id;
	
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String role ;
	private String pharmacistId;
	private String fullName;
	private String gender;
	private String qualification;
	private String dateOfBirth;
	private String contactNumber;
	private String governmentId;
	private String pharmacyLicense;
    private String yearsOfExperience;
	private String dateOfJoining;
	private String department;
	@Valid
	@NotNull(message = "Bank account details are required", groups = RequiredChecks.class)
	private BankAccountDetails bankAccountDetails;
	private Address address;
	// Optional Fields
	private String emailID;
	private String previousEmploymentHistory;
	private String emergencyContactNumber;
    private String profilePicture;
    private String statePharmacyCouncilRegistration;
    private String dpharmaOrBPharmaCertificate;
    private String experienceCertificates;
    private Map<String, List<String>> permissions;
    private String shiftTimingsOrAvailability;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
	
	private String userName;
	private String password;
}
