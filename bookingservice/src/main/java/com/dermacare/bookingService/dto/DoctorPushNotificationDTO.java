package com.dermacare.bookingService.dto;

import lombok.Data;

@Data
public class DoctorPushNotificationDTO {
    private String doctorId;

    private String bookingId;

    private String patientName;

    private String appointmentDate;

    private String appointmentTime;

    private String appointmentType;
}
