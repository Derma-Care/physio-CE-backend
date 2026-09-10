package com.clinicadmin.service;

import com.clinicadmin.dto.PatientAnalyticsRequest;
import com.clinicadmin.dto.Response;

public interface PatientAnalyticsService {

    /**
     * @param filterType 1=Today, 2=Week, 3=Month, 4=Year (from the path)
     * @param request    optional body — only `search` is used for these presets;
     *                   startDate/endDate are ignored here since the range is
     *                   derived from filterType, not from the body
     */
    Response getPatientAnalytics(String clinicId, String branchId, int filterType, PatientAnalyticsRequest request);

    /**
     * Used when request.getFilter() == "custom" — startDate/endDate drive the
     * range instead of a preset filterType.
     */
    Response getCustomPatientAnalytics(String clinicId, String branchId, PatientAnalyticsRequest request);
}
