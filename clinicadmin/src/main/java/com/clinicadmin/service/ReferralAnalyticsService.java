package com.clinicadmin.service;

import com.clinicadmin.dto.Response;

public interface ReferralAnalyticsService {

	/** Default overall view — no option given, same as option=5 */
	Response getReferralAnalytics(String clinicId, String branchId);

	/** option: 1=day, 2=week, 3=month, 4=year, 5=overall */
	Response getReferralAnalytics(String clinicId, String branchId, int option);

	Response getReferralAnalyticsByCustomRange(String clinicId, String branchId, String startDate, String endDate);

	/** Summary (no nested patients) — for dashboard tables. Default overall. */
	Response getReferralAnalyticsSummary(String clinicId, String branchId);

	Response getReferralAnalyticsSummary(String clinicId, String branchId, int option);

	Response getReferralAnalyticsSummaryByCustomRange(String clinicId, String branchId, String startDate,
			String endDate);

	/**
	 * doctorRefCode can be an actual code, or "self" for self-generated patients
	 */
	Response getPatientsByReferringDoctor(String clinicId, String branchId, String doctorRefCode);

	/**
	 * doctorRefCode can be an actual code, or "self". option: 1=day, 2=week,
	 * 3=month, 4=year, 5=overall
	 */
	Response getPatientsByReferringDoctor(String clinicId, String branchId, String doctorRefCode, int option);

	Response getPatientsByReferringDoctorCustomRange(String clinicId, String branchId, String doctorRefCode,
			String startDate, String endDate);

}