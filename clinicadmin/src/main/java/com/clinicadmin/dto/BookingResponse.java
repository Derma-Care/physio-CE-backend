package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

	private String bookingId;
	private String bookingFor;
	private String name;
	private String dob;
	private String patientMobileNumber;
	private String patientId;
	private String visitType;
	private Integer freeFollowUpsLeft;
	private Integer freeFollowUps;
	private String patientAddress;
	private String age;
	private String gender;
	private String email;
	private String mobileNumber;
	private String customerId;
	private String consultationExpiration;
	private String problem;
	private String symptomsDuration;
	private String clinicId;
	private String clinicName;
	private String branchId;
	private String branchname;
	private String doctorId;
	private String doctorName;
	private String serviceDate;
	private String servicetime;
	private String consultationType;
	private List<ConsultationFeesDTO> listOfConsultationFee;
	private Double consultationFee;
	private Integer visitCount;
	private String reasonForCancel;
	private List<ReportsDtoList> reports;
	private String BookedAt;
	private List<StatusDTO> currentStatus;
	private String status;
	private double totalFee;
	private List<String> attachments;
	private String consentFormPdf;
	private List<String> prescriptionPdf;
	private String doctorRefCode;
	private String paymentType;
	private String followupDate;
	private String foc;
	private String focReason;
	private String followupStatus;
	private String treatmentName;
	// ✅ Add treatments info
	// private TreatmentResponseDTO treatments;
	// ✅ Add this new field
	private Map<String, List<TheraphyAnswersDTO>> theraphyAnswers;
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
	private List<Session> session;
	private String partImageKey;
	private String referredDoctorId;
	private String transactionId;
	private boolean nextVisit;

	private double dueAmount;
	private String finalAmount;
	private String paidAmount;
	private String discount;
	private String actualAccount;
	private String pacakgeId;
	private String packageName;
	private String packageType;
	private String noOfSittings;
	// private List<FollowupBookingDto> follwupBookings;

	private String scheduleId;
	private String sittingsId;
	private Integer sittingNumber;

	public void setIsFollowupStatus(boolean followupStatus) {
		isFollowupStatus = followupStatus;
	}

	public boolean getIsFollowupStatus() {
		return isFollowupStatus;
	}

}
