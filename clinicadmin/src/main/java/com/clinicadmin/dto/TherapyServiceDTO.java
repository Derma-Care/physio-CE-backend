package com.clinicadmin.dto;

import java.util.List;

import com.clinicadmin.entity.TherapyExercises;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TherapyServiceDTO {
    
	private String id;
    private int consentType;
    private List<String> exerciseIds;
    private String therapyName;
    private String clinicId;
    private String branchId;
    private int noExerciseIdCount;
    private List<TherapyExercises> exercises;
}