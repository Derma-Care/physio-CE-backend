package com.clinicadmin.dto;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingDTO {

    private String billingId;

    private String clinicId;
    private String branchId;

    private PatientDTO patient;

    private String doctorId;
    private String visitType;

    private LocalDate billDate;
    private LocalDate invoiceDate;

    private List<ServiceItemDTO> services;

    // Latest/current transaction
    private TransactionDTO newTransaction;

    // Complete payment transaction history
    private List<TransactionDTO> transactions;

    // Billing/payment totals
    private PaymentSummaryDTO paymentSummary;

    private AdditionalDetailsDTO additionalDetails;

    private String invoiceStatus;
}