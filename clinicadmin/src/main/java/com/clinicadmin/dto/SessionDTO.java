package com.clinicadmin.dto;


import lombok.Data;

@Data
public class SessionDTO {

    private String date;
    private String sessionId;
    private String status;
    private String paymentStatus;
}
