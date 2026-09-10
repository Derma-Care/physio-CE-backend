package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import com.clinicadmin.validations.FormatChecks;
import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NurseDTO {

	private String id;

	private String nurseId;

	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String branchName;

	private String role;

	@NotBlank(message = "Full name is required", groups = RequiredChecks.class)
	@Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters", groups = FormatChecks.class)
	private String fullName;

	@NotBlank(message = "Date of Birth is required", groups = RequiredChecks.class)
	private String dateOfBirth;

	@NotBlank(message = "Contact number is required", groups = RequiredChecks.class)
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Contact number must be a valid 10-digit Indian number", groups = FormatChecks.class)
	private String nurseContactNumber;

	@NotBlank(message = "Government ID is required", groups = RequiredChecks.class)
	private String governmentId;

	@NotBlank(message = "Nursing license is required", groups = RequiredChecks.class)
	private String nursingLicense;

	@NotBlank(message = "Nursing degree or diploma certificate is required", groups = RequiredChecks.class)
	private String nursingDegreeOrDiplomaCertificate;

	@NotBlank(message = "Nursing council registration is required", groups = RequiredChecks.class)
	private String nursingCouncilRegistration;

	@NotBlank(message = "Date of joining is required", groups = RequiredChecks.class)
	private String dateOfJoining;

	@NotBlank(message = "Department is required", groups = RequiredChecks.class)
	private String department;

	@Valid
	@NotNull(message = "Bank account details are required", groups = RequiredChecks.class)
	private BankAccountDetails bankAccountDetails;

	private String medicalFitnessCertificate;
	@NotBlank(message = " Email Id required", groups = RequiredChecks.class)
	private String emailId;

	private String previousEmploymentHistory;

//	private String experienceCertificates;

	private String vaccinationStatus;
	private String profilePicture;

//	private InsuranceOrESIDetails insuranceOrESIdetails;

	private String gender;
	private String qualifications;
	private String shiftTimingOrAvailability;
	private Address address;
	private String yearsOfExperience;
	private String emergencyContactNumber;
	private Map<String, List<String>> permissions;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

	private String userName;
	private String password;

	public void trimNurseFields() {
		if (nurseId != null)
			nurseId = nurseId.trim();
		if (role != null)
			role = role.trim();
		if (fullName != null)
			fullName = fullName.trim();
		if (dateOfBirth != null)
			dateOfBirth = dateOfBirth.trim();
		if (nurseContactNumber != null)
			nurseContactNumber = nurseContactNumber.trim();
		if (governmentId != null)
			governmentId = governmentId.trim();
		if (nursingLicense != null)
			nursingLicense = nursingLicense.trim();
		if (nursingDegreeOrDiplomaCertificate != null)
			nursingDegreeOrDiplomaCertificate = nursingDegreeOrDiplomaCertificate.trim();
		if (nursingCouncilRegistration != null)
			nursingCouncilRegistration = nursingCouncilRegistration.trim();
		if (dateOfJoining != null)
			dateOfJoining = dateOfJoining.trim();
		if (department != null)
			department = department.trim();
		if (medicalFitnessCertificate != null)
			medicalFitnessCertificate = medicalFitnessCertificate.trim();
		if (emailId != null)
			emailId = emailId.trim();
		if (previousEmploymentHistory != null)
			previousEmploymentHistory = previousEmploymentHistory.trim();
//		if (experienceCertificates != null)
//			experienceCertificates = experienceCertificates.trim();
	}
}
