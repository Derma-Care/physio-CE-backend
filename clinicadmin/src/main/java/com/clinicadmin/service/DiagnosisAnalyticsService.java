package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface DiagnosisAnalyticsService {

    Response getDashboard(String clinicId, String branchId,
                           Integer type, String startDate, String endDate);

    Response getDiagnosisTests(String clinicId, String branchId,
                                Integer type, String startDate, String endDate);

    Response getDiagnosisDetails(String clinicId, String branchId, String testNameId,
                                  Integer type, String startDate, String endDate);



	Response getVendorTestSummary(String clinicId, String branchId, String vendorId, String testNameId);

	Response getVendorTests(String clinicId, String branchId, String vendorId);
}