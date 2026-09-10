package com.clinicadmin.dto;

import lombok.Data;

@Data
public class SessionCompletedEventDto {
    private String bookingId;
    private String clinicId;
    private String branchId;
    private String patientName;
    private String mobileNumber;
    private int totalNoOfSessions;
    private int noOfSessionsCompleted;
}