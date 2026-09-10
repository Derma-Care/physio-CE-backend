package com.clinicadmin.entity;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "Pharmacist")
@AllArgsConstructor
@NoArgsConstructor
public class Pharmacist {
	private ObjectId id;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String role;
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
	private BankAccountDetails bankAccountDetails;
	private Address address;
	// Optional Fields
	private String emailID;
	private String previousEmploymentHistory;
	private String emergencyContactNumber;
    private String shiftTimingsOrAvailability;

	private String profilePicture;
	private String statePharmacyCouncilRegistration;
	private String dpharmaOrBPharmaCertificate;
	private String experienceCertificates;
	private Map<String, List<String>> permissions;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
	


}
