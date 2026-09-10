//package com.clinicadmin.dto;
//
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import com.clinicadmin.enums.RefundMode;
//import com.clinicadmin.enums.ReturnType;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.DecimalMax;
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotEmpty;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Pattern;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class SalesReturnRequest {
//
//    @Valid
//    @NotNull(message = "Patient details are required")
//    private PatientDetailsRequest patientDetails;
//
//    @Valid
//    @NotNull(message = "Return details are required")
//    private ReturnDetailsRequest returnDetails;
//
//    @Valid
//    @NotNull(message = "Items are required")
//    @NotEmpty(message = "At least one item is required")
//    private List<ReturnItemRequest> items;
//
//    @Valid
//    @NotNull(message = "Summary is required")
//    private SummaryRequest summary;
//
//    @NotBlank(message = "Clinic ID is required")
//    private String clinicId;
//
//    @NotBlank(message = "Branch ID is required")
//    private String branchId;
//
//    private String originalBillNo;
//
//    // ── Nested DTOs ────────────────────────────────────────────────────────
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class PatientDetailsRequest {
//
//        private String fileNo;
//
//        @NotBlank(message = "Patient name is required")
//        private String patientName;
//
//        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
//        private String mobileNo;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class ReturnDetailsRequest {
//
//        @NotBlank(message = "Original bill number is required")
//        private String originalBillNo;
//
//        @NotNull(message = "Return type is required")
//        private ReturnType returnType;
//
//        @NotNull(message = "Refund mode is required")
//        private RefundMode refundMode;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class ReturnItemRequest {
//
//        @NotBlank(message = "Medicine ID is required")
//        private String medicineId;
//
//        @NotBlank(message = "Medicine name is required")
//        private String medicineName;
//
//        private String batchNo;
//
//        @NotNull @Min(value = 1, message = "Sold quantity must be at least 1")
//        private Integer soldQty;
//
//        @NotNull @Min(value = 1, message = "Return quantity must be at least 1")
//        private Integer returnQty;
//
//        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
//        private BigDecimal rate;
//
//        @DecimalMin("0.0") @DecimalMax("100.0")
//        private BigDecimal discountPercent;
//
//        @DecimalMin("0.0") @DecimalMax("100.0")
//        private BigDecimal gstPercent;
//
//        private BigDecimal netAmount;
//    }
//
//    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//    public static class SummaryRequest {
//
//        @NotNull private BigDecimal total;
//        private BigDecimal discount;
//        private BigDecimal gst;
//
//        @NotNull private BigDecimal netRefund;
//    }
//}
