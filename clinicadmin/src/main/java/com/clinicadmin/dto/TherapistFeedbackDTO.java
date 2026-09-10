package com.clinicadmin.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapistFeedbackDTO {

    private String targetId;

    private String feedbackText;

    private String rating;
}