package com.clinicadmin.entity;

import java.util.List;

import lombok.Data;

@Data
public class Availability {
    private List<String> days;
    private String startTime;
    private String endTime;
}