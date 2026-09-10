package com.clinicadmin.dto;

import java.util.List;
import lombok.Data;

@Data
public class CustomNotificationRequest {

    private String title;       // Header {{1}}
    private String body;        // Body {{1}}
    private String clinicName;  // Body {{2}}
    private String branchName;  // Body {{3}}
    private List<PatientEntry> list;

    @Data
    public static class PatientEntry {
        private String patientId;
        private String name;
        private String mobileNumber;
    }
}