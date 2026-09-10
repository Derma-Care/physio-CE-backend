package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorNotesDTO {
    private String bookingId;
    private String doctorId;
    private String patientPhoneNumber;
    private String notes;
}
