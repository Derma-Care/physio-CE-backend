package com.dermacare.bookingService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TherapistAnalyticsDTO {
    private String doctorId;
    private String doctorName;
    private String dateRange;
    private Integer totalSittings;
    private Integer completedSittings;
    private Integer pendingSittings;
    private Double completionRatio;
}
