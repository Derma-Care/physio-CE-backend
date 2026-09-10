package com.AdminService.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorsDTO {
    private String id;
    private String doctorId;
    
    private String deviceId;
    
    @NotBlank(message = "DoctorEmail is required")
    private String doctorEmail;

    @NotBlank(message = "Clinic id is required")
    private String hospitalId;

    @NotBlank(message = "Branch ID is required")  // <-- Added branchId
    private String branchId;

//    @Size(max = 255, message = "Doctor picture URL should not exceed 255 characters")
    private String doctorPicture;

    @NotBlank(message = "Doctor licence is required")
    @Size(max = 100, message = "Doctor licence number should not exceed 100 characters")
    private String doctorLicence;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String doctorMobileNumber;

    @NotBlank(message = "Doctor name is required")
    @Size(min = 3, max = 50, message = "Doctor name must be between 3 and 50 characters")
    private String doctorName;

//    @Valid
//    @NotNull(message = "Category list cannot be null")
//    @Size(min = 1, message = "At least one Category is required")
//    private List<@Valid DoctorCategoryDTO> category;
//
//    @Valid
//    @NotNull(message = "Services list cannot be null")
//    @Size(min = 1, message = "At least one service is required")
//    private List<@Valid DoctorServicesDTO> service;
//
//    @Valid
//    @NotNull(message = "Sub-services list cannot be null")
//    @Size(min = 1, message = "At least one sub-service is required")
//    private List<@Valid DoctorSubServiceDTO> subServices;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female, or Other")
    private String gender;

    @NotBlank(message = "Experience is required")
    @Pattern(regexp = "^\\d{1,2}(\\+)?$", message = "Experience should be a number like '5' or '5+'")
    private String experience;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotBlank(message = "Available days are required")
    private String availableDays;

    @NotBlank(message = "Available times are required")
    private String availableTimes;

    @Size(max = 1000, message = "Profile description should not exceed 1000 characters")
    private String profileDescription;

    @Size(max = 10, message = "Maximum 10 focus areas allowed")
    private List<@NotBlank(message = "Focus area cannot be blank") String> focusAreas;

    @Size(max = 5, message = "Maximum 5 languages allowed")
    private List<@NotBlank(message = "Language cannot be blank") String> languages;

    @Size(max = 10, message = "Maximum 10 career path items allowed")
    private List<@NotBlank(message = "Career path item cannot be blank") String> careerPath;

    @Size(max = 10, message = "Maximum 10 highlights allowed")
    private List<@NotBlank(message = "Highlight cannot be blank") String> highlights;

    private boolean doctorAvailabilityStatus;
    private boolean recommendation;
    private double doctorAverageRating;

    public void trimAllDoctorFields() {
        id = trim(id);
        doctorId = trim(doctorId);
        hospitalId = trim(hospitalId);
        branchId = trim(branchId); // <-- Trim branchId
        doctorPicture = trim(doctorPicture);
        doctorLicence = trim(doctorLicence);
        doctorMobileNumber = trim(doctorMobileNumber);
        doctorName = trim(doctorName);
        specialization = trim(specialization);
        gender = trim(gender);
        experience = trim(experience);
        qualification = trim(qualification);
        profileDescription = trim(profileDescription);
        availableDays = trim(availableDays);
        availableTimes = trim(availableTimes);

        focusAreas = trimList(focusAreas);
        languages = trimList(languages);
        careerPath = trimList(careerPath);
        highlights = trimList(highlights);
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }

    private List<String> trimList(List<String> list) {
        return (list != null)
            ? list.stream().map(this::trim).collect(Collectors.toList())
            : null;
    }
}
