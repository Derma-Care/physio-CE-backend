package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempBlockingSlot {
	
	private String doctorId;
	private String branchId; 
	private String serviceDate;
	private String servicetime;
	private boolean status;
	private long timeInMillis;
	

}
