package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class TherapistFeedbackSummaryDTO {

    private Integer totalPatients;

    private Double averageSessionRating;

    private Double averageOverallRating;

    private Double overallAverageRating;

    private List<TherapistFeedbackResponseDTO> feedbacks;
}