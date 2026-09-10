package com.clinicadmin.dto;

import lombok.Data;

@Data
public class VendorTestDTO {

	private String testName;
	private String testNameId;

	private long totalAssigned;
	private long tested;
	private long pending;
	private long notTested;

}