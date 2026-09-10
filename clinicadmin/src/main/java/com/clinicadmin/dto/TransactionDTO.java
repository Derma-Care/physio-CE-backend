package com.clinicadmin.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private String receiptNo;

    private LocalDate paymentDate;

    private String paymentMode;

    private String transactionId;

    private Double amount;

    private String remarks;
}