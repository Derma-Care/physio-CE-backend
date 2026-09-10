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
public class ReceptionistRequestDTO {

	private String id;

	@NotBlank(message = "Clinic ID is mandatory")
	private String clinicId;

	private String hospitalName;
	private String branchId;
	private String branchName;

	@NotBlank(message = "Full name is required")
	private String fullName;

	private String dateOfBirth;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
	private String contactNumber;
	
	private String qualification;

	
	private String governmentId;

	
	private String dateOfJoining;

	
	private String department;

	private Address address;


	private String emergencyContact;
	private String profilePicture;

	private String userName; // auto = contactNumber
	private String password; // auto-generate

	private String role;

	private Map<String, List<String>> permissions;

	
	private BankAccountDetails bankAccountDetails;

	// ---------- Optional ----------

	private String gender;
	private String yearOfExperience;
	private String vaccinationStatus;
    private String shiftTimingsOrAvailability;


	// ---------- Optional ----------
	@Email(message = "Invalid email format")
	private String emailId;

	private String graduationCertificate;
	private String computerSkillsProof;
	private String previousEmploymentHistory;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

}