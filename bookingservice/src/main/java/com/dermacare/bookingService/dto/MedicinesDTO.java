package com.dermacare.bookingService.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicinesDTO {
    private String id;
    private String name;
    private String dose;
    private String duration;
    private String durationUnit;

    private String food;
    private String medicineType;
    private String note;
    private String remindWhen;
    private List<String> times;
    private String others;

}
