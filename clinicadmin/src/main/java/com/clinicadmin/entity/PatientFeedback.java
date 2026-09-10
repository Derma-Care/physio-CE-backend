package com.clinicadmin.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "patient_feedback")
public class PatientFeedback {

    @Id
    private String id;
    private String clinicId;
    private String branchId;
    private String patientId;

    private String patientName;

    private String patientPhone;

    private LocalDateTime date;

    private HospitalFeedback hospitalFeedback;

    private DoctorFeedback doctorFeedback;

    private ReceptionistFeedback receptionistFeedback;

    private TherapistFeedback therapistFeedback;
    
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}