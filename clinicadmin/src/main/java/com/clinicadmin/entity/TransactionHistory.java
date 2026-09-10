package com.clinicadmin.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistory {

    private String receiptNumber;

    private String transactionId;

    private LocalDateTime date;

    private String sittingNo;

    private String paymentMode;

    private Double amount;

    private Double discount;

    private Double royaltyDeduction;

    private Double finalPaid;

    private Double earnedRoyaltyPoints;
    private Double remainingRoyaltyPoints;
    private LocalDateTime paidAt;

    
}