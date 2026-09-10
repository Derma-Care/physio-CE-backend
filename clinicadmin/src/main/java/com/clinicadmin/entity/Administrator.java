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

@Document(collection = "administrators")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Administrator {

    @Id
    private String id;
    private String adminId;
    private String clinicId;
    private String branchId;
    private String branchName;
    private String hospitalName;

    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String contactNumber;
    private String emailId;
    private String governmentId;

    private String qualificationOrCertifications;
    private String dateOfJoining;
    private String department;
    private String yearOfExperience;

    private String role;

    private Address address;
    private String emergencyContact;
    private String profilePicture;

    private BankAccountDetails bankAccountDetails;
	private Map<String, List<String>> permissions;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

  
}
