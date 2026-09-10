package com.clinicadmin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineInventoryDto {
	
	    private String id;
	    private String barcode;
	    private String productName;
	    private String brandName;
	    private String category;
	    private String composition;
	    private String manufacturer;
	    private String packSize;
	    private String hsnCode;
	    private double gstPercent;
	    private double mrp;
	    private double minStock;
	    private String status;
	    private String clinicId;
	    private String branchId;
	    private LocalDateTime createdAt;	    
		private String pack;		
		private String batchNo;		
		private String expiryDate;

}
