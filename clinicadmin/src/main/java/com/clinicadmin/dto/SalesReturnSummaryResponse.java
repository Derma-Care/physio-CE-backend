package com.clinicadmin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.clinicadmin.enums.RefundMode;
import com.clinicadmin.enums.ReturnStatus;
import com.clinicadmin.enums.ReturnType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesReturnSummaryResponse {

    private String returnNo;
    private String fileNo;
    private String patientName;
    private String mobileNo;
    private String originalBillNo;
    private ReturnType returnType;
    private RefundMode refundMode;
    private BigDecimal netRefundAmount;
    private LocalDate returnDate;
    private ReturnStatus status;
}
