package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequestDto {
	
	private String mobileNumber;

	private String oldPassword;

	private String newPassword;

	private String confirmPassword;

	private String otp;

}
