package com.clinicadmin.service;

import java.util.Map;

import com.clinicadmin.dto.Response;

public interface TherapistAttendenceService {

	Response getDailyReport(String therapistId, String date);

    Response updateAttendance(String therapistId, Map<String, String> body);


	Response getMonthlyReport(String therapistId, String month);

	public Response deleteSession(String therapistId, String date, String sessionId);
//	String getCityFromLatLong(String latitude, String longitude);

	Response addManualSession(String therapistId, Map<String, String> body);

	Response getReportByClinicBranch(String clinicId, String branchId, String therapistId, String date);

//    // ✅ DAILY GET
//    public Response  getDailyReport(String therapistId, String date);
//
//  
//    // ✅ MONTHLY GET
//    List<MonthlyTherapistResponse> getMonthlyReport(String therapistId, String month);
//
//	void updateAttendance(String therapistId, String date, String loginTime, String logoutTime, String activity,
//			String duration, String latitude, String longitude);
}