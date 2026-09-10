package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TherapistRecordDTO {

	private String id;
    private String therapistRecordId;

    private String clinicId;
    private String branchId;
    private String patientId;
    private String bookingId;
    private String therapistId;
    private String therapistName;
    private String patientName;
//    private String therapy;

//    private String date;
    private String completedDate;
    private String completedTime;

    private String duration;
//    private String exercises;

    private String painBefore;
    private String painAfter;

    private String therapistNotes;
    private String patientResponse;
    private String sessionId;
    private String result;
    private String status;
    private String mode;
    private String nextPlan;

    private String beforeImage;
    private String afterImage;
    private String beforeVideo;
    private String afterVideo;
    private String voiceRecord;
    private String setsDone;
    private String repetationDone;
    private String serviceType;
	private String latitude;
	private String longitude;
	private String location;
	private String consentPdfUrl;
		
	
		
	
    
}