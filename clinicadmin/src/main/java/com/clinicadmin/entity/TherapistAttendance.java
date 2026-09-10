package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "therapist_attendance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TherapistAttendance {

    @Id
    private String id;

    private String therapistId;  
    private String clinicId;
    private String branchId;

    private String date;        

    private TimeLocation login;
    private TimeLocation logout;
    
    private String logTime;     
    private String workingHours;
    private String idleTime;
    private String overtime;

    // ✅ ADD THIS
    private List<Session> sessions;

private String status;
private String loginLocation;
private  String logoutLocation;
private String  lateTime;
private String reason;


	


	
}
