package com.clinicadmin.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TherapistCertificateDTO {

    private String id;
    
    private String therapistId;
    
    private String clinicId; 
    
    private String branchId;
    

    private String certificateName;

    private String issueAuthority;

    private String upload;
    
    private LocalDateTime uploadDateTime;
}