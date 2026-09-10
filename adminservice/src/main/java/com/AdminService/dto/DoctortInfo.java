package com.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctortInfo {
	
	private String doctorPicture;
	private String doctorName;
	private String specialization;
	private String experience;
	private String profileDescription;
	

}
