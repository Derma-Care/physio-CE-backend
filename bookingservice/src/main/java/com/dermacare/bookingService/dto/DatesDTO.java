package com.dermacare.bookingService.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatesDTO {
	 private String date;
	    private Integer sitting;
	    private String status;
	    private String followupStatus;
	   
}
