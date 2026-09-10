package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRatingNotificationDTO {

    private String doctorId;
    private String patientName;
    private String rating;
    private String feedback;
}