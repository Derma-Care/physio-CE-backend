package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientSummary {
    private String patientId;
    private String patientName;
    private Integer totalNumberOfSittings;
    ///private List<List<PhysioMaxTreatedDoctor>> treatedBy;
}
