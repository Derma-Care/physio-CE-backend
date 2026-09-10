package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionsDTO {

    private String clinicId;
    private String branchId;
    private String userId;
    private String role; 
    private Map<String, List<String>> permissions;
}

