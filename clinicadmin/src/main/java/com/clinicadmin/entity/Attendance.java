package com.clinicadmin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String userId;
    private String role;
    private String clinicId;
    private String branchId;
    private String date;

    // 🔥 Clean structure
    private TimeLocation login;
    private TimeLocation logout;

    // 🔹 Calculated
    private String logTime;
    private String workingHours;
    private String idleTime;
    private String lateTime;
    private String reason;
    private String overtime;
    
    private String status;
}