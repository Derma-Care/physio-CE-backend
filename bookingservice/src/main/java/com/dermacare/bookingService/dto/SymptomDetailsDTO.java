package com.dermacare.bookingService.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomDetailsDTO {
	 private String symptomDetails;
	    private String doctorObs;
	    private String diagnosis;
	    private String duration;
	    private List<String> attachments; 
}
