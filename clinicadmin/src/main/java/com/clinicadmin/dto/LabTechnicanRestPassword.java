package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LabTechnicanRestPassword {
    private String contactNumber;   // acts as username
	private String currentpassword;
	private String newPassword;
	private String conformPassword;
	

}