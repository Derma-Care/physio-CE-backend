package com.clinicadmin.dto;



import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
public class FeedbackDetailsDTO {
    private String id;
    private String clinicId;
    private String branchId;
	 // ================= PATIENT =================
    private String patientId;
    private String patientName;
    private String mobileNumber;

    // ================= BOOKING =================
    private String bookingId;

    // ================= DOCTOR =================
    private String doctorId;
    private String doctorName;

    // ================= THERAPIST =================
    private String therapistId;
    private String therapistName;
    private String therapistRecordId;

    // ================= STAFF =================
    private String staffId;
    private String staffName;

    // ================= SERVICE =================
    private String serviceType;
    private List<ServiceInfo> service;

    // ================= SESSION =================
    private int totalNoOfSessions;
    private int noOfSessionsCompleted;

    // ================= STATUS =================
    private boolean isHalfSessionsCompleted;
    private boolean isFullSessionsCompleted;

    // ================= FEEDBACK =================
    private String whatWentWell;
    private String improvements;
    private String rating;
    // ================= AUDIT =================


    private String createdAt;

  
    private String updatedAt;
    
    private boolean halfNotificationSent;
    private boolean fullNotificationSent;
}
