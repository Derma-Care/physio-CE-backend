package com.clinicadmin.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Document(collection = "expenses")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Expense {

    @Id
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
