package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full payload for the Revenue Analytics screen: summary cards, the patient
 * table, and the data needed to draw the trend / paid-vs-due charts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponseDTO {

	private String filterApplied; // human-readable label of the resolved filter, e.g. "This Month"
	private String rangeStart; // resolved range start (yyyy-MM-dd)
	private String rangeEnd; // resolved range end (yyyy-MM-dd, inclusive)

	private RevenueSummaryDTO summary;

	private Double todayPaidAmount; // always "today", regardless of the selected filter

	private List<PatientRevenueDTO> patientRevenueList;

	private List<RevenueTrendPointDTO> revenueTrend; // for the "revenue over time" chart

	// Convenience fields for a simple 2-bar "Paid vs Due" chart - mirrors summary,
	// kept separate so the frontend chart component can bind directly without
	// reaching into "summary".
	private Double paidVsDuePaid;
	private Double paidVsDueDue;
}