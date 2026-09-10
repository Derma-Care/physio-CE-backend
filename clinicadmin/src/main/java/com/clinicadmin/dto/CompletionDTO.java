package com.clinicadmin.dto;


import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionDTO {

    private String status;

    private OffsetDateTime completedAt;

    private String completedBy;

    private Boolean isCompleted;
}