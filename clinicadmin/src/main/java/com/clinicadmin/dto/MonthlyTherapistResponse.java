package com.clinicadmin.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTherapistResponse {

    private String date;
    private String inTime;
    private String outTime;
    private String logTime;
    private String workingHours;
    private String idleTime;
		
	
}