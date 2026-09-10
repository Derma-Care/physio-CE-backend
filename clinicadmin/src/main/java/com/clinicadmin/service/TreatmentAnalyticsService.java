package com.clinicadmin.service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistAnalyticsDTO;

import java.util.List;

public interface TreatmentAnalyticsService {

	/**
	 * @param type   "All Types" | "Activity" | "Therapy" | "Program" | "Package"
	 * @param period "Today" | "Month" | "Quarter" | "Year"
	 */
	Response getTreatmentAnalytics(String clinicId, String branchId, String type, String period);

	Response getTreatmentAnalyticsByDateRange(String clinicId, String branchId, String type,
			String fromDate, String toDate);

 }
