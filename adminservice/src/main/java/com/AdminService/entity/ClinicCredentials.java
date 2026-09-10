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
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "clinic_credentials")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicCredentials {

    @Id
    private String id;  
    private String mobilenumber;
    private String hospitalName;
    private String userName;
    private String password;
    private String role;
    private String email;
    private Map<String, Map<String, List<String>>> permissions;
    private List<String> fcmToken;
    private String otp;
    private Long otpExpiryMillis;
		// TODO Auto-generated method stub
		
	}
    
 
    
