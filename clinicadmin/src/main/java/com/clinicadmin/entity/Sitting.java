package com.clinicadmin.entity;

import lombok.Data;

@Data
public class Sitting {

    // Sitting / Payment For
    private String sittingNo;

    // Total Due (Before Discount)
    private Double totalDueBeforeDiscount;

    // Discount (%)
    private Double discountPercentage;

    // Discount (₹)
    private Double discountAmount;

    // Current Due (₹)
    private Double currentDue;

    // Use Royalty Points
    private Boolean useRoyaltyPoints;

    // Available Royalty Points
    private Double availableRoyaltyPoints;

    // Amount Paying Now (₹)
    private Double amountPayingNow;

    // Remaining Due (₹)
    private Double remainingDue;

    // Payment Mode
    private String paymentMode;
    
  

}