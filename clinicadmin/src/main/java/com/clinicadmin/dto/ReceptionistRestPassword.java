package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceptionistRestPassword {
	private String contactNumber;   
	private String currentpassword;
	private String newPassword;
	private String conformPassword;
	


}