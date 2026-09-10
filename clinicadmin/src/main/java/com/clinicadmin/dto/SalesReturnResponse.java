//package com.clinicadmin.dto;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import com.clinicadmin.enums.RefundMode;
//import com.clinicadmin.enums.ReturnStatus;
//import com.clinicadmin.enums.ReturnType;
//import com.fasterxml.jackson.annotation.JsonInclude;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class SalesReturnResponse {
//
//    private String returnNo;
//    private String originalBillNo;
//    private PatientDetailsDto patientDetails;
//    private ReturnDetailsDto returnDetails;
//    private List<ReturnItemDto> items;
//    private SummaryDto summary;
//    private ReturnStatus status;
//    private String clinicId;
//    private String branchId;
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class PatientDetailsDto {
//        private String fileNo;
//        private String patientName;
//        private String mobileNo;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class ReturnDetailsDto {
//        private ReturnType returnType;
//        private RefundMode refundMode;
//        private LocalDateTime returnDate;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class ReturnItemDto {
//        private String medicineId;
//        private String medicineName;
//        private String batchNo;
//        private Integer soldQty;
//        private Integer returnQty;
//        private BigDecimal rate;
//        private BigDecimal discountPercent;
//        private BigDecimal gstPercent;
//        private BigDecimal netAmount;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class SummaryDto {
//        private BigDecimal totalReturnAmount;
//        private BigDecimal totalDiscount;
//        private BigDecimal totalGST;
//        private BigDecimal netRefundAmount;
//    }
//}
