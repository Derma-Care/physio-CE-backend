package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSlotRequestDTO {
	 private String doctorId;
	    private String date;
	    private String oldSlot;
	    private String newSlot;
	    private String branchId;
		
		}

