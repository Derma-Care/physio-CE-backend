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
@Document(collection = "SecurityStaff")
public class SecurityStaff {
	
	@Id
	private String securityStaffId;
    private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String role;
	private String fullName;
	private String dateOfBirth;
	private String gender;
	 @Indexed(unique = true)
	private String contactNumber;
	private String govermentId;
	private String dateOfJoining;
	private String department;
	private Address address;
	private BankAccountDetails bankAccountDetails;
	private String policeVerification;
	private String policeVerificationCertificate;
	private String medicalFitnessCertificate;
	private String emailId;
	private String traningOrGuardLicense;
	private String previousEmployeeHistory;
    private String profilePicture;
    private Map<String, List<String>> permissions;
    private String shiftTimingsOrAvailability;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
	

}