package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhysiLitePackageDTO {
	private String id;
	private String packageId;
	private String packageName;
	private String packageType;
	private double price;
	private int noOfSittings;
}
