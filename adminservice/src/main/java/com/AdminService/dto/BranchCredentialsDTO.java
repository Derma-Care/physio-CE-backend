package com.AdminService.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchCredentialsDTO {
	
	private String mobilenumber;
    private String branchId;     // e.g., H_1-B_2
    private String userName;     // same as branchId or custom username
    private String password;     // generated password
    private String branchName; 
    private String email;
    private String otp;
    private Long otpExpiryMillis;
}
