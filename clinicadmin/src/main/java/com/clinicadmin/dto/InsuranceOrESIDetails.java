package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceOrESIDetails {
	private String policyOrEsiNumber;
	private String providerName; // LIC, StarHealth, ESI Corporation etc.
	private String type; // "Insurance" or "ESI"
	private String status; // Active, Expired, Cancelled
}
