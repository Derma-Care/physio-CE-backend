package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentResponseDTO {
    private List<String> selectedTestTreatment;
    private Map<String, TreatmentDetailsDTO> generatedData; // dynamic treatment names
    private String followupStatus;
    // ✅ Sitting summary fields (optional)
    private int totalSittings;
    private int pendingSittings;
    private int takenSittings;
    private int currentSitting;
}
