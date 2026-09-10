package com.clinicadmin.dto;


import lombok.Data;

@Data

public class SessionData {

    private String sessionId;
    private String activity;
    private String duration;
    private String location;
    private String description;
}
