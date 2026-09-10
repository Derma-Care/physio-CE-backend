package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRatingDomain {
	    
	private double doctorRating;
    private double branchRating;
    private String feedback;
    private String hospitalId;
    private String branchId;
    private String doctorId; 
    private String customerMobileNumber;
    private String patientId;
    private String patientName;
    private String appointmentId;
    private boolean rated;
    private String dateAndTimeAtRating;
	   
	   
}
