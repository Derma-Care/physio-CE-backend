package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUpDetailsDTO {
    private int durationValue;
    private String durationUnit;
    private String nextFollowUpDate;
    private String followUpNote;
}

