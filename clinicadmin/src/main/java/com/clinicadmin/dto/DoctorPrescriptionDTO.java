package com.clinicadmin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPrescriptionDTO {

	 private String id; 
	    
	    @NotBlank(message = "Clinic ID is required")

	    private String clinicId;
	    
	    private List<MedicineDTO> medicines;  
}
