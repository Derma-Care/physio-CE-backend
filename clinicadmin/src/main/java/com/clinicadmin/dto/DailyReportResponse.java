package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyReportResponse {

    private String date;

  
    private TimeLocationDTO login;
    private TimeLocationDTO logout;

    private String logTime;
    private String status;

    private List<SessionData> sessions;

}