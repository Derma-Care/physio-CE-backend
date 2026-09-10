package com.clinicadmin.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;
import com.clinicadmin.validations.RequiredChecks;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "referred_doctors")
public class ReferredDoctor {

    @Id
    private String id;
    private String clinicId;
    private String branchId;
    private String fullName;
    private String gender;
    private Date dateOfBirth;
    private String governmentId; 
    private String medicalRegistrationNumber;
    private String specialization;
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

    private String status;

   
}