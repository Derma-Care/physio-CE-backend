package com.clinicadmin.dto;

import java.util.List;



import lombok.Data;

@Data
public class PatientMessageDTO {

    private String clinicId;
    
    private String clinicName;
    
    private String branchId;
 
    private String branchName;
    
    private String title;

    private String body;

    private List<PatientInfoDTO> list;
}