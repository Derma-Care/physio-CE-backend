package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class TherapistServiceResponseDTO {

    private String therapistId;
    private String therapistName;
    private List<String> services;
    private Boolean isPresent;
}