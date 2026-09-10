
package com.dermacare.bookingService.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetailsDTO {
//    private String id;
    private List<MedicinesDTO> medicines;
}
