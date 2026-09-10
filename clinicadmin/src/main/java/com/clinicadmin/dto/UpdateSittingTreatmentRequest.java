package com.clinicadmin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateSittingTreatmentRequest {

    private String condition;

    private String treatmentPlan;

    private List<PhysioMaxTreatedDoctor> treatedBy;
}