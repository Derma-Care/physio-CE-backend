package com.clinicadmin.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "therapist_certificate")
public class TherapistCertificate {

    @Id
    private String id;
    
    private String therapistId;
    
    private String clinicId; 
    
    private String branchId;
    
    private String certificateName;

    private String issueAuthority;

    private String upload;
    
    private LocalDateTime uploadDateTime;

}