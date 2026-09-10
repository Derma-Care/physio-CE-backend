package com.clinicadmin.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryDTO {

    private Double subTotal;

    private Double totalDiscount;

    private Double totalTax;

    private Double totalAmount;

    private Double totalPaid;

    private Double dueAmount;
}