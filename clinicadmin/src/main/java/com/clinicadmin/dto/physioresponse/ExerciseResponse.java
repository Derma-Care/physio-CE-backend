package com.clinicadmin.dto.physioresponse;

import java.util.List;

import com.clinicadmin.dto.Session;

import lombok.Data;

@Data
public class ExerciseResponse {
	private String exerciseId; //
	private String exerciseName; //

	private Double pricePerSession; //
	private Integer noOfSessions;
	private double discountPercentage;
	private double discountAmount;
	private double gst;
	private double otherTax;
	private Double totalExercisePrice;
	private double totalPrice;
	private String paymentStatus;
	private Integer repetitions;
	private String frequency;
	private Integer sets;
	private String youtubeUrl;
	private String notes;
// ........New fields......
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
	private List<Session> sessions;
}