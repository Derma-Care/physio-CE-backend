package com.dermacare.bookingService.dto;

import java.util.List;
import java.util.Map;
import com.dermacare.bookingService.entity.ConsultationFees;
import com.dermacare.bookingService.entity.ReportsList;
import com.dermacare.bookingService.entity.Status;
import com.dermacare.bookingService.entity.TheraphyAnswersEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowupBookingDto {
	
	 private String bookingId;
	    private String bookingFor;
	    private String relation;
	    private String patientMobileNumber;
	    private String patientAddress;
	    private String patientId;
	    private Integer freeFollowUpsLeft;
	    private Integer freeFollowUps;
	    private String followupDate;
	    private String visitType;
	    private String name;
	    private String age;
	    private String gender;
	    private String mobileNumber;
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
	    private String doctorDeviceId;
	    private String doctorWebDeviceId;
	    private String subServiceId;
	    private String subServiceName;
	    private String serviceDate;
	    private String servicetime;
	    private String consultationType;
	    private List<ConsultationFees> listOfConsultationFee;
	    private double consultationFee;
	    private String reasonForCancel;
	    private String notes;
	    private List<Status> currentStatus;
		private String status;
	    private List<ReportsList> reports;
	    private String channelId;
	    private String bookedAt;
	    private Integer visitCount;
	    private String attachments;
	    private String consentFormPdf;
	    private List<String> prescriptionPdf;
	    private double totalFee;
		private String paymentType;
	    private String doctorRefCode;
	    private String consultationExpiration;
	    private String followupStatus;
	    private Integer sittings;
	    private Integer totalSittings;
	    private Integer pendingSittings;
	    private Integer takenSittings;
	    private Integer currentSitting;
	    private String foc;
	    // ------------------- NEW: Treatments and dates -------------------
	    ///private TreatmentResponseDTO treatments; // treatmentName -> treatment details
	    private String bodyPartId;
	   	private String bodyPartName;
	   	private String partImage;
	   	private Map<String,List<TheraphyAnswersEntity>> theraphyAnswers;
	   	private List<String> parts;
	   	private double partAmount;
	   	private double dueAmount;
	   	private String referredByType;
		private String referredByName;
		private String paymentStatus;
		private String previousInjuries;
		private String currentMedications;
		private String allergies;
		private String occupation;
		private String insuranceProvider;
		private String policyNumber;
		private List<String> activityLevels;
		private String reasonforVisit;
		private boolean isFollowupStatus;
		
		public void setIsFollowupStatus(boolean followupStatus) {
		    isFollowupStatus = followupStatus;
		}
		
		public boolean getIsFollowupStatus() {
		    return isFollowupStatus;
		}

}