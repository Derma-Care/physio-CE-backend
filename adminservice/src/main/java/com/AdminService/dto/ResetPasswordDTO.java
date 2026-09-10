package com.AdminService.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
	private String mobileNumber;
	private String role;
	private String otp;
	private String newPassword;
	private String confirmPassword;
}
