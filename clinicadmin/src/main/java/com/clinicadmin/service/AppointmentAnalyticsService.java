package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface AppointmentAnalyticsService {
	
	Response getAppointmentAnalytics(String clinicId, String branchId, Integer type, String startDate, String endDate);

	Response getAppointmentSummary(String clinicId, String branchId, Integer type, String startDate, String endDate);
}
