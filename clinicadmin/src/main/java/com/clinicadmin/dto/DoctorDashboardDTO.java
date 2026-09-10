package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardDTO {

    private long totalDoctors;
    private long totalAppointments;
    private long totalPatientsConsulted;

    private List<DoctorPerformanceDTO> doctorDetails;
}