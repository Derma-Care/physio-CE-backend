package com.clinicadmin.dto;

import lombok.Data;

@Data
public class UserLoginDetailsDTO {

    private String clinicId;
    private String clinicName;

    private String branchId;
    private String branchLocation;

    private String userId;
    private String username;
    private String role;
    private String mobileNumber;
}