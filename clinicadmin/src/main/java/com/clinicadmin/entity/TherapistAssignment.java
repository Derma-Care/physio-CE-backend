package com.clinicadmin.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "therapist_assignments")
@Data
public class TherapistAssignment {

    @Id
    private String id;

    private String clinicId;
    private String branchId;

    private String therapistRecordId;
   

    private String assignTherapistId;
    private String assignTherapistName;

    private String assignedTherapistId;
    private String assignedTherapistName;

    private String assignedStatus;
    private List<String> services;

    
    private Boolean assignedTo = true;
    
}
