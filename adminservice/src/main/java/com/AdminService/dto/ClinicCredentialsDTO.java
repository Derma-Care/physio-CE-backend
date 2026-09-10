package com.AdminService.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicCredentialsDTO {

	private String hospitalName;
	private String mobilenumber;
	@NotBlank(message = "UserName should not be blank")
	@Size(min = 1, max = 20, message = "UserName must be minimum 4 characters and maximum 20 characters")
	private String userName;
	@NotBlank(message = " Password should not be blank")
	@Size(min = 3, max = 20, message = "Password must be minimum 4 characters and maximum 20 characters")
	private String password;
	private String email;
	private String role;
	private Map<String, Map<String, List<String>>> permissions;
	private String fcmToken;
	private String otp;
	private Long otpExpiryMillis;

	
}
