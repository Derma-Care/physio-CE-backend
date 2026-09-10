package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisTestDTO {

    private String testNameId;

    private String testName;

    private Long patientCount;

    private Long testOrderCount;

	

}