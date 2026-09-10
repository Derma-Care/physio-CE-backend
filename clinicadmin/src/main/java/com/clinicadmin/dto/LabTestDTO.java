package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTestDTO {
    private String id;
    private String testName;
    private String hospitalId;
    private String description;
    private String purpose;
}
