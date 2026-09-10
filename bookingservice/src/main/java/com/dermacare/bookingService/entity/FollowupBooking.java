package com.dermacare.bookingService.entity;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowupBooking {
	
	
	    private String visitType;
	    private String doctorId;
	    private String doctorName;
	    private String serviceDate;
	    private String servicetime;
		private String status;
	   		
	   	
}
