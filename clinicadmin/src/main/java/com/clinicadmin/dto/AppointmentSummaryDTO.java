package com.clinicadmin.dto;

import lombok.Data;

@Data
public class AppointmentSummaryDTO {

    private long totalAppointments;
    private long completed;
    private long cancelled;
    private long missed;
}
