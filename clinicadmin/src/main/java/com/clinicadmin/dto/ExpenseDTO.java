package com.clinicadmin.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseDTO {

    private String id;

    private String title;

    private String category;

    private LocalDate date;

    private Double amount;

    private String mode;
    
    private String notes;
    private String transactionId;

    private String clinicId;

    private String branchId;
}
