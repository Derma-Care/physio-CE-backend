package com.dermacare.bookingService.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Appointments")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    @Id
    private String bookingId;
    private String importId;
    private String bookingFor;
    private String relation;
    private String patientMobileNumber;
    private String dob;
    private String patientAddress;

    private String patientId;
    private String sitting;
    private Integer freeFollowUpsLeft;
    private Integer freeFollowUps;
    private String followupDate;
    private String visitType;
    private String name;
    private String age;
    private String gender;

    private String mobileNumber;

    private String customerId;

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
    private List<ConsultationFees> listOfConsultationFee;
    private Double consultationFee;
    private String reasonForCancel;
    private List<Status> currentStatus;

    private String status;

    private List<ReportsList> reports;
    private String bookedAt;
    private Integer visitCount;
    private List<String> attachments;
    private String consentFormPdf;
    private List<String> prescriptionPdf;
    private double totalFee;
    private String paymentType;
    private String doctorRefCode;
    private String consultationExpiration;
    private String followupStatus;
    private String foc;
    private String focReason;
    private String email;
    private Map<String, List<TheraphyAnswersEntity>> theraphyAnswers;
    private List<String> parts;
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
    private List<FollowupBooking> follwupBookings;
    private String transactionId;
    private String referredDoctorId;
    private Boolean nextVisit;
    
    private String pacakgeId;
	private String packageName;
	private String packageType;
	private String noOfSittings;
    private String scheduleId;
	private String sittingsId;
	private Integer sittingNumber;

    public void setIsFollowupStatus(boolean followupStatus) {
        isFollowupStatus = followupStatus;
    }

    public boolean getIsFollowupStatus() {
        return isFollowupStatus;
    }

    /**
     * Deep copy constructor to safely duplicate Booking objects.
     */
    public Booking(Booking booking) {
        this.bookingId = booking.getBookingId();
        this.bookingFor = booking.getBookingFor();
        this.relation = booking.getRelation();
        this.patientMobileNumber = booking.getPatientMobileNumber();
        this.dob = booking.getDob();
        this.patientAddress = booking.getPatientAddress();
        this.patientId = booking.getPatientId();
        this.freeFollowUpsLeft = booking.getFreeFollowUpsLeft();
        this.freeFollowUps = booking.getFreeFollowUps();
        this.followupDate = booking.getFollowupDate();
        this.visitType = booking.getVisitType();
        this.name = booking.getName();
        this.age = booking.getAge();
        this.gender = booking.getGender();
        this.mobileNumber = booking.getMobileNumber();
        this.customerId = booking.getCustomerId();
       // this.customerDeviceId = booking.getCustomerDeviceId();
        this.problem = booking.getProblem();
        this.symptomsDuration = booking.getSymptomsDuration();
        this.clinicId = booking.getClinicId();
        this.clinicName = booking.getClinicName();
        this.branchId = booking.getBranchId();
        this.branchname = booking.getBranchname();
       // this.clinicDeviceId = booking.getClinicDeviceId();
        this.doctorId = booking.getDoctorId();
        this.doctorName = booking.getDoctorName();
       // this.doctorDeviceId = booking.getDoctorDeviceId();
       // this.doctorWebDeviceId = booking.getDoctorWebDeviceId();
        this.serviceDate = booking.getServiceDate();
        this.servicetime = booking.getServicetime();
        this.consultationType = booking.getConsultationType();
        this.listOfConsultationFee = booking.getListOfConsultationFee();
        this.consultationFee = booking.getConsultationFee();
        this.reasonForCancel = booking.getReasonForCancel();
        this.currentStatus = booking.getCurrentStatus();
        this.status = booking.getStatus();
        this.reports = booking.getReports();
        this.bookedAt = booking.getBookedAt();
        this.visitCount = booking.getVisitCount();
        this.attachments = booking.getAttachments();
        this.consentFormPdf = booking.getConsentFormPdf();
        this.prescriptionPdf = booking.getPrescriptionPdf();
        this.totalFee = booking.getTotalFee();
        this.paymentType = booking.getPaymentType();
        this.doctorRefCode = booking.getDoctorRefCode();
        this.consultationExpiration = booking.getConsultationExpiration();
        this.followupStatus = booking.getFollowupStatus();
        this.foc = booking.getFoc();
        this.focReason = booking.getFocReason();
        this.theraphyAnswers = booking.getTheraphyAnswers();
        this.parts = booking.getParts();
        this.dueAmount = booking.getDueAmount();
        this.referredByType = booking.getReferredByType();
        this.referredByName = booking.getReferredByName();
        this.paymentStatus = booking.getPaymentStatus();
        this.previousInjuries = booking.getPreviousInjuries();
        this.currentMedications = booking.getCurrentMedications();
        this.allergies = booking.getAllergies();
        this.occupation = booking.getOccupation();
        this.insuranceProvider = booking.getInsuranceProvider();
        this.policyNumber = booking.getPolicyNumber();
        this.activityLevels = booking.getActivityLevels();
        this.reasonforVisit = booking.getReasonforVisit();
        this.isFollowupStatus = booking.getIsFollowupStatus();
        this.follwupBookings = booking.getFollwupBookings();
        this.transactionId = booking.getTransactionId();
        this.referredDoctorId = booking.getReferredDoctorId();
    }
}
