package com.clinicadmin.dto;

import java.math.BigDecimal;
import java.util.List;

import com.clinicadmin.enums.RefundMode;
import com.clinicadmin.enums.ReturnType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturnUpdateRequest {

    @NotBlank(message = "Original bill number is required")
    private String originalBillNo;

    @Valid
    @NotNull(message = "Patient details are required")
    private PatientDetailsDto patientDetails;

    @Valid
    @NotNull(message = "Return details are required")
    private ReturnDetailsDto returnDetails;

    @Valid
    @NotNull @NotEmpty(message = "At least one item is required")
    private List<ReturnItemDto> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PatientDetailsDto {
        private String fileNo;
        @NotBlank private String patientName;
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        private String mobileNo;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReturnDetailsDto {
        @NotNull private ReturnType returnType;
        @NotNull private RefundMode refundMode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReturnItemDto {
        @NotBlank private String medicineId;
        @NotBlank private String medicineName;
        private String batchNo;
        @NotNull @Min(1) private Integer soldQty;
        @NotNull @Min(1) private Integer returnQty;
        @NotNull @DecimalMin("0.01") private BigDecimal rate;
        @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal discountPercent;
        @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal gstPercent;
    }
}
