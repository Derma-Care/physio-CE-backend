package com.clinicadmin.dto.physioresponse;

import lombok.Data;

@Data
public class TreatmentRow {
	private String treatmentName;
	private String type;        // Activity / Therapy / Program / Package
	private int patients;
	private int sessions;
	private int completed;
	private double successRate; // %
	private double avgRevenue;
}