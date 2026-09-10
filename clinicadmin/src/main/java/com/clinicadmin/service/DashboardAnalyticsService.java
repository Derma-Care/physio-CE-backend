package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface DashboardAnalyticsService {

    Response getDashboard(
            String clinicId,
            String branchId);

}
