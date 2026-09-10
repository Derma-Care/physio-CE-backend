package com.clinicadmin.dto;

import java.util.List;

import com.clinicadmin.entity.Availability;
import com.clinicadmin.entity.Documents;

import lombok.Data;

@Data
public class TherapistDTO {

    private String therapistId;

    private String clinicId;
    private String branchId;

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
    private int totalSessionCount;
    private String userName;
    private String password;
    private Boolean isPresent;
}
