package com.clinicadmin.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.constraints.NotBlank;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferredDoctorDTO {
	private String id;
    private String clinicId;
	@NotBlank(message = "Full name is required", groups = RequiredChecks.class)
	
private String branchId;

    private String fullName;
	@NotBlank(message = "Gender is required", groups = RequiredChecks.class)
    private String gender;
    
    private Date dateOfBirth;
    private String governmentId;

    private String medicalRegistrationNumber;
    private String specialization;
	@NotBlank(message = "Experience is required", groups = RequiredChecks.class)

    private int yearsOfExperience;
    private String currentHospitalName;
    private String department;
	

    private String mobileNumber;
    private String email;
    private Address address;
    private String referralId; 


//    private Date firstReferralDate;
//    private int totalReferrals;
//    private List<String> referredPatients;
//    private String preferredCommunicationMethod;

    private BankAccountDetails bankAccountNumber;
	@NotBlank(message = "status required", groups = RequiredChecks.class)

    private String status;
}