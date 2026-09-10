package com.clinicadmin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sourceType",
    "programId",
    "programName",
    "packageId",
    "packageName",
    "therapyData"
})
public class ProgramResponseDTO {

    private String sourceType;

    private String programId;
    private String programName;

    private String packageId;
    private String packageName;

    private List<TherapyResponseDTO> therapyData;
}