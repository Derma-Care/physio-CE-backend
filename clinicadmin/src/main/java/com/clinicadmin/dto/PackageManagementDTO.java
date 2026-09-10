package com.clinicadmin.dto;

import java.util.List;

import com.clinicadmin.entity.TherophyProgramEntity;

import lombok.Data;

@Data
public class PackageManagementDTO {
	 
	    
	private String packageId;
	
    private String clinicId;
    
    private String branchId;

    private String packageName;
    private String discountAmount;
    private String packageAmount;
    private String finalAmount;
    private List<String> programIds;

    private int discountPercentage;

    private String startOfferDate;

    private String endOfferDate;
    
    private List<TherophyProgramEntity>programs;

    private String offerType;

    private int noOfPrograms;		
    
	}
