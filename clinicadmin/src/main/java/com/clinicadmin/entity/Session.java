package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session {

    private String sessionId;
    private String activity;
    private String duration;
    private String description;
    private String location;

}
