package com.clinicadmin.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivacyPolicyDTO {

	private String id;
	private String clinicId;
	private String PrivacyPolicy;
}