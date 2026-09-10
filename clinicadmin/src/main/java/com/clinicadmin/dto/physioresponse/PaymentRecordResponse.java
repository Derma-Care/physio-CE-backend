package com.clinicadmin.dto.physioresponse;

import java.util.List;

import lombok.Data;

@Data
public class PaymentRecordResponse {

	private String id;
	private boolean payAfterService;
	private String clinicId;
	private String branchId;
	private String bookingId;
	private String patientId;

	private String doctorId;
	private String doctorName;

	private String therapistId;
	private String therapistName;
	private String therapistRecordId;
	private String treatmentName;
	private String serviceType;
	private String overallStatus;

	private double totalAmount;
	private double discountAmount;
	private double finalAmount;

	private double totalPaid;
	private double balanceAmount;
	private String paymentStatus;

	private String sessionStartDate;
	private String sessionEndDate;
	private int totalSessionCount;
	private int noOfSessionCompletedCount;
	private boolean noOfSessionCompletedStatus;
	private boolean sessionTableCreatedStatus;
	private String overallReceiptNumber;
	private List<PaymentHistory> paymentHistory;

	// ✅ This holds different structure based on serviceType
	private Object therapyWithSessions;
}