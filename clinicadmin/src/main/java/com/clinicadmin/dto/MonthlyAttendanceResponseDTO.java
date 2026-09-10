package com.clinicadmin.dto;

import lombok.Data;

@Data
public class MonthlyAttendanceResponseDTO {

    private String date;

    private String shift;

    private String inTime;
    private String outTime;

    private String logTime;
    private String workingHours;
    private String idleTime;
}