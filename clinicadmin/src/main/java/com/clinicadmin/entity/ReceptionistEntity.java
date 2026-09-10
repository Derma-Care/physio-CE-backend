package com.clinicadmin.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "receptionists")
public class ReceptionistEntity {

	@Id
	private String id; // Custom ID like RC_A27873EB

	private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String fullName;
	private String dateOfBirth; // dd-MM-yyyy
	private String contactNumber;

	private String qualification; // ✅ Mandatory (moved here)

	private String governmentId;
	private String dateOfJoining; // dd-MM-yyyy
	private String department;

	private Address address; // Mandatory
	private String emergencyContact;
//    private String userName;   // auto = contactNumber
//    private String password;   // auto-generate

	private String role;

	private Map<String, List<String>> permissions;

	private String profilePicture;

	private BankAccountDetails bankAccountDetails; // ✅ Mandatory

	private String gender;
	private String yearOfExperience;
	private String vaccinationStatus;
    private String shiftTimingsOrAvailability;


	// ---------- Optional Fields ----------
	private String emailId;
	private String graduationCertificate;
	private String computerSkillsProof;
	private String previousEmploymentHistory;
    private String createdBy;
    private Boolean dashboardStatus = false;
    private String createdAt;
    
    private String updatedDate;
}