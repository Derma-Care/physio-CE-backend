package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpMedicineDTO {
	    private String medicineId;
	    private String medicineName;
	    private String batchNo;
	    private Double qty;
	    private Double rate;
	    private Double totalA;         // qty * rate
	    private Double discPercent;
	    private Double discAmtB;       // discount amount (negative means deduction)
	    private Double netAmtAB;       // totalA + discAmtB
	    private Double gstPercent;
	    private Double gstAmtC;        // GST amount
	    private Double finalAmountABC; // netAmtAB + gstAmtC

}
