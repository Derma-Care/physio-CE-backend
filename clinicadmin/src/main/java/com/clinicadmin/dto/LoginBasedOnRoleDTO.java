package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginBasedOnRoleDTO {
	private String username;
	private String password;
	private String role; // Doctor, PHARMACIST, NURSE, DOCTOR

}
