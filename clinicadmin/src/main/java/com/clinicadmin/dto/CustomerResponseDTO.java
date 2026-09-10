package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResponseDTO {
	private String userName;
	private String customerName;
	private String customerId;
	private String patientId;
	private String deviceId;
	private String hospitalName;
	private String hospitalId;
	private String branchId;
}
