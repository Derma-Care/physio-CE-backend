package com.clinicadmin.dto;

import lombok.Data;

@Data
public class TherapyExercisesDTO {

    private String clinicId;
    private String branchId;
    private String therapyExercisesId;

    private String name;
    private String video;
    private String image;

    private String session;
    private String duration;
    private String frequency;
    private String notes;

    // ✅ Price Fields
    private double pricePerSession;
    private double discountPercentage;
    private double discountAmount;
    private double gst;
    private double otherTax;
    private int totalPrice;

    // ✅ Exercise Fields
    private int sets;
    private int repetitions;

    // ✅ New Fields
    private String technique;
    private String machine;
    private String intensity;
    private String assistanceLevel;
    private String type;
    private String area;
    private String metric;
    private String value;
    private String unit;
    private String bodyPart;

    // ✅ Activity Fields
    private String activityType;
    private String activityDuration;
}