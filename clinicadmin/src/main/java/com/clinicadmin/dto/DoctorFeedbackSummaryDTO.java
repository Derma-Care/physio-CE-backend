package com.clinicadmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class DoctorFeedbackSummaryDTO {
    private String doctorId;
    private String clinicId;
    private int totalPatientsRated;
    private double averageRating;
    private List<DoctorPatientFeedbackDTO> patients;
}