package com.clinicadmin.dto;

import lombok.Data;

import java.util.List;

@Data
public class DailyAttendanceResponseDTO {

    private String date;

    // =====================================================
    // LOGIN / LOGOUT
    // =====================================================

    private TimeLocationDTO login;

    private TimeLocationDTO logout;

    // =====================================================
    // ATTENDANCE TIME DETAILS
    // =====================================================

    private String logTime;

    private String workingHours;

    private String idleTime;

    private String lateTime;

    private String overtime;

    // =====================================================
    // STATUS
    // =====================================================

    private String status;

    // =====================================================
    // ACTIVITIES
    // =====================================================

    private List<ActivityDTO> activities;

    // =====================================================
    // ABSENT REASON
    //
    // Used only when status = ABSENT
    // =====================================================

    private String reason;

   private String shift;
		
	
}