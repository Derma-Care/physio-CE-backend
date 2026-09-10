package com.AdminService.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "branchCredentials")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchCredentials {
    @Id
    private String id;
    private String mobilenumber;
    private String branchId; 
    private String userName;  
    private String password;  
    private String branchName;
    private String role;
	private Map<String, Map<String, List<String>>> permissions;
    private List<String> fcmTokens;
    private String email;
    private String otp;
    private Long otpExpiryMillis;
		//
		
	

}

