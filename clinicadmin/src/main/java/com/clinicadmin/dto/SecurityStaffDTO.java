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
public class SecurityStaffDTO {

	private String securityStaffId;

	private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String role;

	@NotBlank(message = "Full Name is mandatory")
	private String fullName;

	@NotBlank(message = "Date of Birth is mandatory")
	private String dateOfBirth;

	@NotBlank(message = "Gender is mandatory")
	private String gender;

	@NotBlank(message = "Contact Number is mandatory")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact Number must be 10 digits")
	private String contactNumber;

	@NotBlank(message = "Government ID is mandatory")
	private String govermentId;

	@NotBlank(message = "Date of Joining is mandatory")
	private String dateOfJoining;

	@NotBlank(message = "Department is mandatory")
	private String department;

	@NotNull(message = "Address is mandatory")
	private Address address;

	@NotNull(message = "Bank Account Details are mandatory")
	private BankAccountDetails bankAccountDetails;

	@NotBlank(message = "Police Verification is mandatory")
	private String policeVerification;
	
	private String policeVerificationCertificate;

	@NotBlank(message = "Medical Fitness Certificate is mandatory")
	private String medicalFitnessCertificate;

	private String profilePicture;

	private String emailId;

	private String traningOrGuardLicense;

	private String previousEmployeeHistory;
	private Map<String, List<String>> permissions;
    private String shiftTimingsOrAvailability;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;


	private String userName;
	private String password;
}