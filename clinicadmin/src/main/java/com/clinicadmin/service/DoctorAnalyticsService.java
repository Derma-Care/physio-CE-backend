package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface DoctorAnalyticsService {

    Response getDoctorDashboard(
            String clinicId,
            String branchId,
            Integer type,
            String startDate,
            String endDate);

	Response getDoctorPatients(String clinicId, String branchId, String doctorId, Integer type, String startDate,
			String endDate);
}