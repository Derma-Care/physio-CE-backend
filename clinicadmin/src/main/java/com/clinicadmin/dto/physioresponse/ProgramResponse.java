package com.clinicadmin.dto.physioresponse;

import java.util.List;
import lombok.Data;

@Data
public class ProgramResponse {
    private String programId;
    private String programName;
    private Double totalProgramPrice;
    private String paymentStatus;
    private List<TherapyResponse> therapyData;
}