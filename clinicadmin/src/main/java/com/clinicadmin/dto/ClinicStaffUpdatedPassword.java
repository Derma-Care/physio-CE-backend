package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor
@NoArgsConstructor
public class ClinicStaffUpdatedPassword {
	private String username;
	private String role;
	private String currentPassword;
	private String newPassword;
	private String confirmPassword;
}
