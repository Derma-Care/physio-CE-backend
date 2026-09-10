package com.clinicadmin.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Wardboy")
public class WardBoy {

	@Id
	private String wardBoyId;
	private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String fullName;
	private String dateOfBirth;
	@Indexed(unique = true)
	private String contactNumber;
	private String gender;
	private String workExprience;
	private String shiftTimingOrAvailability;
	private Address address;
	private String emergencyContact;
	private String profilePicture;

	private String governmentId;
	private String dateOfJoining;
	private String department;
	private BankAccountDetails bankAccountDetails;
	private String medicalFitnessCertificate;

	private String emailId;
	private String basicHealthFirstAidTrainingCertificate;
	private String previousEmploymentHistory;
	private String policeVerification;
	private String policeVerificationCertificate;
	private String role;
	
    private String shiftTimingsOrAvailability;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

	private Map<String, List<String>> permissions;

}