package com.clinicadmin.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientFeedbackDTO {

    private String id;

    private String patientId;
    private String clinicId;
    private String branchId;
    private String patientName;

    private String patientPhone;

    private LocalDateTime date;

    private HospitalFeedbackDTO hospitalFeedback;

    private DoctorFeedbackDTO doctorFeedback;

    private ReceptionistFeedbackDTO receptionistFeedback;

    private TherapistFeedbackDTO therapistFeedback;
    
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}