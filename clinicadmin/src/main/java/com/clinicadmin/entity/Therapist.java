package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "therapists")
@Data
public class Therapist {

    @Id
    
    private String Id;
    private String therapistId;

    private String clinicId;
    private String clinicName;
    private String branchId;
    private String branchName;
    private String fullName;
    private String contactNumber;
    private String gender;
    private String dateOfBirth;

    private String qualification;
    private Integer yearsOfExperience;

    private List<String> services;
    private List<String> specializations;
    private List<String> expertiseAreas;
    private List<String> treatmentTypes;

    private Availability availability;
    private String emailId;
    private String bio;

    private Documents documents;

    private List<String> languages;

    private String role;
    private String physioType;
	private String dateofJoining;
	private String emergencyContact;
    private String aadharID;
    
    private String userName;
    private String password;
    
    private Boolean isPresent = false;
}
