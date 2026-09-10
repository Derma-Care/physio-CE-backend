package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentDetailsDTO {
    private List<DatesDTO> dates;
    private String reason;
    private String frequency;
    private Integer sittings;        // remaining sittings
    private String startDate;
    private Integer totalSittings;   // total planned sittings

    // ✅ New sitting summary-fields
    private Integer pendingSittings; // totalSittings - completed
    private Integer takenSittings;   // completed sittings
    private Integer currentSitting;  // last completed sitting number
}
