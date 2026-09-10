package com.clinicadmin.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueResponse {

	private boolean success;
	private Object data;
	private Double consultationTotal;
	private Double therapyFeeTotal;
	private Double totalFinalAmount;
	private Double dueAmountTotal;
	private Double grandTotal;
	private String message;
	private int status;
}
