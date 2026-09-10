package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdministratorDTO {

    @Id
    private String id;

    private String adminId;
    
    @NotBlank(message = "Clinic ID is required")
    private String clinicId;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Contact number must be a valid 10-digit Indian mobile number")
    private String contactNumber;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String emailId;
    
    @NotBlank(message = "Government ID is required")
    private String governmentId;

    private String qualificationOrCertifications;

    @NotBlank(message = "Date of joining is required")
    private String dateOfJoining;

    @NotBlank(message = "Department is required")
    private String department;

  
    private String yearOfExperience;

    @NotBlank(message = "Role is required")
    private String role;

 
    @NotNull(message = "Address is required")
    private Address address;

    @NotBlank(message = "Emergency contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Emergency contact must be a valid 10-digit Indian number")
    private String emergencyContact;

    private String profilePicture;

    private BankAccountDetails bankAccountDetails;

    private Map<String, List<String>> permissions;
    
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;

    private String userName;
    private String password;
}
