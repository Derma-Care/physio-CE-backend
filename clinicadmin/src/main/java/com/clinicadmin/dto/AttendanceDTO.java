package com.clinicadmin.dto;

import lombok.Data;

@Data
public class AttendanceDTO {

    private String userId;
    private String role;
    private String clinicId;
    private String branchId;
    private String date;

    // 🔥 Now using object
    private TimeLocationDTO login;
    private TimeLocationDTO logout;
    private String lateTime;
    private String reason;
    private String overtime;
    private String loginTime;
    private String logoutTime;

}