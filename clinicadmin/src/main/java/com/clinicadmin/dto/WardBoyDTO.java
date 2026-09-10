package com.clinicadmin.dto;


import java.util.List;
import java.util.Map;

import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WardBoyDTO {
	
    private String wardBoyId;  

    @NotBlank(message = "Full Name is required")
    private String fullName;
    
    @NotBlank(message = "Clinic_ID is required")

    private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;

    @NotBlank(message = "Date of Birth is required")
    @Pattern(
        regexp = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$",
        message = "Date of Birth must be in the format dd-MM-yyyy"
    )
    private String dateOfBirth;


    @NotBlank(message = "Contact Number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    @NotBlank(message = "Government ID is required")
    private String governmentId;

    @NotBlank(message = "Date of Joining is required")
    private String dateOfJoining;

    @NotBlank(message = "Department is required")
    private String department;
    
    private String profilePicture;


    @Valid
	@NotNull(message = "Bank account details are required", groups = RequiredChecks.class)
    private BankAccountDetails bankAccountDetails;

    @NotBlank(message = "Medical Fitness Certificate is required")
    private String medicalFitnessCertificate;

    private String emailId;
    private String basicHealthFirstAidTrainingCertificate;
    private String previousEmploymentHistory;
    private String policeVerification;
    private String policeVerificationCertificate;
    
    private String gender;
    private String workExprience;
    
    private String shiftTimingsOrAvailability;
    private Address address;
    private String emergencyContact;
    
    private String userName;
    private String password;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

    
    private  String role;
	private Map<String, List<String>> permissions;

    

}