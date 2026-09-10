package com.clinicadmin.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.clinicadmin.entity.Sitting;
import com.clinicadmin.entity.TransactionHistory;

import lombok.Data;

@Data
public class PaymentDTO {

	private String billingId;
	// Patient Details
	private String patientId;
	private String patientName;
	private String mobileNumber;

	// Clinic Details
	private String clinicId;
	private String branchId;

	// Booking Details
	private String bookingId;

	// Current Sitting
	private String sittingNo;

	// Package Details
	private Double packageAmount;

	// Discount Details
	private Double discountPercentage;
	private Double discountAmount;

	// Royalty Details
	private Double royaltyPointsUsed;
	private Double royaltyDeduction;

	// Amount Details
	private Double finalAmount;
	private Double amountPaying;
	private Double remainingDue;
	private Double availableRoyaltyPoints;
	private Double remainingRoyaltyPoints;
	// Payment Details
	private String paymentMode;
	private String transactionId;
	private LocalDateTime paidAt;

	// History
	private List<Sitting> sittings;
	private List<TransactionHistory> transactionHistory;
	private Double earnedRoyaltyPoints;
	private boolean editingDisabled ;
}