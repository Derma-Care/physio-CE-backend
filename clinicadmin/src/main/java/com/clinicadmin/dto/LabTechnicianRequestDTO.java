// ✅ Request DTO
package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabTechnicianRequestDTO {
	private String id;
	private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of Birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Contact Number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact Number must be 10 digits")
    private String contactNumber;

    @NotBlank(message = "Government ID is required")
    private String governmentId;

    @NotBlank(message = "Qualification or Certification is required")
    private String qualificationOrCertifications;

    @NotBlank(message = "Date of Joining is required")
    private String dateOfJoining;

    @NotBlank(message = "Department or Assigned Lab is required")
    private String departmentOrAssignedLab;

    private String yearOfExperience;
    private String specialization;
    private String shiftTimingsOrAvailability;
    private Address address;
    private String profilePicture;

    private String emergencyContact;

    @NotNull(message = "Bank Account Details are required")
    private BankAccountDetails bankAccountDetails;

    @NotBlank(message = "Medical Fitness Certificate is required")
    private String medicalFitnessCertificate;

    @Email(message = "Email must be valid")
    private String emailId;
    private String userName;
    private String password;
    
    private String role;
    
    private Map<String, List<String>> permissions;

    private String labLicenseOrRegistration;


    @Pattern(regexp = "^(?:Yes|No)$", message = "Vaccination status must be Yes or No")
    private String vaccinationStatus;

    private String previousEmploymentHistory;
    
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
    
   
}