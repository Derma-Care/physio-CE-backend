package com.clinicadmin.entity;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;
import com.clinicadmin.dto.InsuranceOrESIDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Nurse")
public class Nurse {
	private ObjectId id;
	private String nurseId;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String role;
	private String fullName;
	private String gender;
	private String qualifications;
	private String yearsOfExperience;
	private String dateOfBirth;
	private String nurseContactNumber;
	private String governmentId;
	private String nursingLicense;
	private String nursingDegreeOrDiplomaCertificate;
	private String nursingCouncilRegistration;
	private String dateOfJoining;
	private String department;
	private BankAccountDetails bankAccountDetails;
	private String medicalFitnessCertificate;
	private String emailId;
	private String previousEmploymentHistory;
//	private String experienceCertificates;
	private String vaccinationStatus;
//	private InsuranceOrESIDetails insuranceOrESIdetails;
	private String emergencyContactNumber;
	private String shiftTimingOrAvailability;
	private Address address;
    private String profilePicture;
    private Map<String, List<String>> permissions;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

}
