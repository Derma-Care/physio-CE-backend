package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhysiLiteSittingDTO {

	private String id;
	private String sittingId;
	private String sittingName;
	private String packageType;
	private double price;
	private int noOfSittings;
	private String clinicId;
	private String branchId;
}
