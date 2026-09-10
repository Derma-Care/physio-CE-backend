package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheraphyNamesDTO {
	
	private String theraphyId;
	private String theraphyName;
	private int theraphyTotalAmount;

}
