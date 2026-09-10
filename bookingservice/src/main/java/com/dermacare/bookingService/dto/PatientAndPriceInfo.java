package com.dermacare.bookingService.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAndPriceInfo {
	
	private List<PatientInfo> list;
	private String totalConsultationFee;
	private String totalTheraphyFee;
	//private String totalFinalAmount;
	private String totalDueAmount;
	private String grandTotalAmount;
	private double priceAfterExpenses;

}
