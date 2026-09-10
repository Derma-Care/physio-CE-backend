package com.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateClinicCredentialsWithUserNameAndRole {

	private String username;
	private String role;
	private String currentPassword;
	private String newPassword;
	private String confirmPassword;
}
