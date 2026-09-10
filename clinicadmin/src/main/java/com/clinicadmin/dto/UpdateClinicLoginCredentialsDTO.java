package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClinicLoginCredentialsDTO {
	private String password;
	private String newPassword;
	private String confirmPassword;
}
