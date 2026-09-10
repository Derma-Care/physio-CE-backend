package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {

    private String mobileNumber;
    private String role;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}