package com.clinicadmin.dto.physioresponse;


import java.util.List;

import lombok.Data;

@Data
public class TreatmentAnalyticsResponse {
	// summary cards
	private int totalSessions;
	private int totalPatients;
	private double avgSuccessRate;
	private int totalTreatmentTypes;      // distinct treatment rows (Image 1 "Treatment Types")
	private int totalTreatments;          // same value, kept for Image 2/3 "Total Treatments" card
	private int highlyRatedCount;         // rows with successRate >= 90 (Image 2/3 "Highly Rated")
	private double avgRevenuePerTreatment;

	// table
	private List<TreatmentRow> treatments;
}
