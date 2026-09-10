package com.clinicadmin.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String billingId;
    private String patientId;
    private String patientName;
    private String mobileNumber;

    private String clinicId;
    private String branchId;

    private String bookingId;
 

    private String sittingNo;

    private Double packageAmount;

    private Double discountPercentage;
    private Double discountAmount;

    private Double royaltyPointsUsed;
    private Double royaltyDeduction;
    private Double availableRoyaltyPoints;
    private Double remainingRoyaltyPoints;
    
    private Double finalAmount;

    private Double amountPaying;

    private Double remainingDue;

    private String paymentMode;

    private String transactionId;

    private LocalDateTime paidAt;

    private List<Sitting> sittings;

    private List<TransactionHistory> transactionHistory;

	private double earnedRoyaltyPoints;
	// NEW: @Builder.Default is required — without it, Lombok's builder()
	// silently ignores the "= false" initializer and leaves this null.

	private boolean editingDisabled ;
	

}