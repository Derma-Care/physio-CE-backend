package com.clinicadmin.dto.physioresponse;

import java.util.List;
import lombok.Data;

@Data
public class TherapyResponse {
    private String therapyId;
    private String therapyName;
    private Double totalTherapyPrice;
    private String paymentStatus;
    private List<ExerciseResponse> exercises;
}