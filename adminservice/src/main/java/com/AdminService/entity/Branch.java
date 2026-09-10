package com.AdminService.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection="Branches")
public class Branch {

	@Id	
	private String id;
	private String clinicId;
	private String hospitalName;
	private String branchId;
	private String branchName;
	private String address;
	private String city;
	private String contactNumber;
	private String email;
	private String latitude;
	private String longitude;
	private String virtualClinicTour;
	private double branchOverallRating;
	private String role;            
	private Map<String, List<String>> permissions;
	private String Status;
	private String loyaltyPoints;
    private String location;

	private String subscriptionDates;
	private String subscriptionStartDate;
	private String subscriptionEndDate;



}




