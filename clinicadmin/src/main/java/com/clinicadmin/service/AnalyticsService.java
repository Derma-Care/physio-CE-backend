package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface AnalyticsService {

    Response getDoctorReferralAnalytics(
            String clinicId,
            String branchId,
            Integer type,
            String startDate,
            String endDate);


	Response getDoctorReferralPatientDetails(String clinicId, String branchId, String referralId);


//	Response getReferralChannels(String clinicId, String branchId);


	Response getReferralChannelPatientDetails(String clinicId, String branchId, String channel);


	Response getReferralChannels(String clinicId, String branchId, Integer type, String startDate, String endDate);


	Response getReferralSummary(String clinicId, String branchId, Integer type, String startDate, String endDate);
}