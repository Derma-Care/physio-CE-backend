package com.clinicadmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpSalesRequest {

    @NotBlank(message = "Bill number is required")
    private String billNo;

    @NotBlank(message = "Bill date is required")
    private String billDate;

    @NotBlank(message = "Bill time is required")
    private String billTime;

    private String visitType;

    private String opNo;

    private String payCategory;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    private String mobile;

    private Integer age;

    private String sex;

    private String consultingDoctor;

    private Boolean includeReturns;

    private List<OpMedicineDTO> medicines;

    private Double amountPaid;

    @NotBlank(message = "Clinic ID is required")
    private String clinicId;

    @NotBlank(message = "Branch ID is required")
    private String branchId;
}
