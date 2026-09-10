package com.clinicadmin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Customers_Data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerOnbording {
	@Id
	private String id;
	private String countryCode;
	private String mobileNumber;
	private String email;
	private String fullName;
	private String gender;
	private String dateOfBirth;
	private String age;
	private String address;
	private String hospitalId;
	private String hospitalName;
	private String branchId;
	private String customerId;
	private String patientId;
	private String deviceId;
	private String referralCode;
	private String referredBy;
    private String createdBy;
    
    private String createdAt;
    
    private String updatedDate;
    private Double availableRoyaltyPoints = 0.0;
}
