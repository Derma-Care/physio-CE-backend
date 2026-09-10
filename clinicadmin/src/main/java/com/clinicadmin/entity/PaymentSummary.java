package com.clinicadmin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummary {

    private Double subTotal;

    private Double totalDiscount;

    private Double totalTax;

    private Double totalAmount;

    private Double totalPaid;

    private Double dueAmount;
}