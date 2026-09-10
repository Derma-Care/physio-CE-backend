package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class Therapy {
    private String name;
    private String sessions;
    private String sessionDuration;
    private String validity;
}
