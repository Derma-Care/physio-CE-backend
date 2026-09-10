package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerOnbordingDTO {

	private String id;
	private String countryCode;
	@NotBlank(message = "Mobile number is required")
//    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be 10 digits and start with 6, 7, 8, or 9")
	private String mobileNumber;
	private String email;
	private String fullName;
	private String age;
	private String gender;
	private String address;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String customerId;
	private String patientId;
	private String referralCode;
	private String referredBy;
	private String userName;
	private String password;
	private String createdBy;
	private String createdAt;
	private String updatedDate;

	private Double availableRoyaltyPoints = 0.0;
}
