package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingRequset {
	
	private String bookingId;
	private String bookingFor;
	private String relation;
	private String patientMobileNumber;
	private String dob;
	private String visitType;
	private Integer freeFollowUps;
	private String patientAddress;
	private String patientId;
	private String name;
	private String email;
	private String age;
	private String gender;
	private String mobileNumber;
	private String consultationExpiration;
	private String customerId;
	private String customerDeviceId;
	private String problem;
	private String symptomsDuration;
	private String clinicId;
	private String clinicName;
	private String branchId;
	private String branchname;
	private String clinicDeviceId;
	private String doctorId;
	private String doctorName;
	private String doctorMobileDeviceId;
	private String doctorWebDeviceId;
	private String subServiceId;
	private String subServiceName;
	private String serviceDate;
	private String servicetime;
	private String followupDate;
	private String consultationType;
	private List<ConsultationFeesDTO> listOfConsultationFee;
	private double consultationFee;	
	private double totalFee;
	private String paymentType;
	private List<String> attachments;
	private String consentFormPdf;
	private String doctorRefCode;
	private String bookedAt;
	private String followupStatus;
	private String foc;
	private String focReason;
	private String bodyPartId;
	private String bodyPartName;
	private String partImage;
	private Map<String,List<TheraphyAnswersDTO>> theraphyAnswers;
	private List<String> parts;
	private double partAmount;
	private double dueAmount;
	private String referredByType;
	private String referredByName;
	private String previousInjuries;
	private String currentMedications;
	private String allergies;
	private String occupation;
	private String insuranceProvider;
	private String policyNumber;
	private List<String> activityLevels;
	private String reasonforVisit;
	private boolean isFollowupStatus;
	private List<ReportsDtoList> reports;
	private String transactionId;
	private String referredDoctorId;

	
	public void setIsFollowupStatus(boolean followupStatus) {
	    isFollowupStatus = followupStatus;
	}
	
	public boolean getIsFollowupStatus() {
	    return isFollowupStatus;
	}
}