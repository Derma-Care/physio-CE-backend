package com.clinicadmin.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;
import com.clinicadmin.dto.BankAccountDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "lab_technicians")
public class LabTechnicianEntity {

    @Id
    private String id;   // Custom ID like LT_A27873EB
    
    private String clinicId;
    private String branchId;
    private String hospitalName;
    private String branchName;
    private String fullName;
    private String gender;
    private String dateOfBirth;
   

    @Indexed(unique = true)
    private String contactNumber;   // This will also be username
    
    private String governmentId;
    private String dateOfJoining;
    private String departmentOrAssignedLab;
    private String yearOfExperience;
    private String specialization;
    private String shiftTimingsOrAvailability;
    private Address address;
    private String emergencyContact;

    private BankAccountDetails bankAccountDetails; // 👈 embed bank account
//
//    private String userName;  // auto = contactNumber
//    private String password;  // auto-generated

    private String role;

    // ✅ Permissions as nested object
    
    private Map<String, List<String>> permissions;

    // Optional Fields
    private String emailId;
    private String labLicenseOrRegistration;
    private String vaccinationStatus;
    private String previousEmploymentHistory;
    private String qualificationOrCertifications;
    private String medicalFitnessCertificate;
    private String profilePicture;
    
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
}
