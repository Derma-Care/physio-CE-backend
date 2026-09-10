package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class TherapistFeedback {

    private String targetId;

    private String feedbackText;

    private String rating;
}
