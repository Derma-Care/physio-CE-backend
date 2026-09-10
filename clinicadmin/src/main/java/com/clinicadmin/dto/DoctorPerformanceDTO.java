package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPerformanceDTO {

    private String doctorId;
    private String doctorName;
    private String speciality;

    private int totalScheduled;
    private int pending;
    private int booked;
    private int completed;
    private int cancelled;

    private double completionRate;
}