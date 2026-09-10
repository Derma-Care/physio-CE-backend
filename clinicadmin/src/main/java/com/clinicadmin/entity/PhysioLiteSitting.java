package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "PhysioLiteSitting")
public class PhysioLiteSitting{

	 @Id
	    private String id;

	    private String sittingId;
	    private String sittingName;
	    private String packageType;
	    private double price;
	    private int noOfSittings;
	    private String clinicId;
	    private String branchId;
}
