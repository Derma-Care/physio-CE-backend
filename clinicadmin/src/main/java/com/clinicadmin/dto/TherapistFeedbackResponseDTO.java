package com.clinicadmin.dto;

import lombok.Data;

@Data
public class TherapistFeedbackResponseDTO {

    private String patientName;

    private String appointmentId;

    private String appointmentDate;

    private String serviceName;

    private String submittedDate;

    // Rating from PatientFeedback
    private String sessionRating;

    // Rating from FeedbackDetails
    private String overallRating;

    // FeedbackDetails comments
    private String whatWentWell;

    private String improvements;

    // PatientFeedback comment
    private String patientFeedbackComment;

    // Average of sessionRating and overallRating
    private Double averageRating;
}