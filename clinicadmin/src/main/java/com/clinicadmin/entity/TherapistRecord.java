package com.clinicadmin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "therapist_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)

public class TherapistRecord {

    @Id
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

    private String result;
    private String status;
    private String mode;
    private String nextPlan;
    private String sessionId;
    private String beforeImage;
    private String afterImage;
    private String beforeVideo;
    private String afterVideo;
    private String voiceRecord;
    private String setsDone;
    private String repetationDone;
    private String serviceType;
	private String loginTime;
	private String logoutTime;
	private String latitude;
	private String longitude;
	private String location;
	private String consentPdfUrl;
	public String description;



	

	
	

}