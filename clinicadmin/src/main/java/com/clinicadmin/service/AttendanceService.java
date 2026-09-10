package com.clinicadmin.service;

import com.clinicadmin.dto.AttendanceDTO;
import com.clinicadmin.dto.Response;

public interface AttendanceService {

    Response save(AttendanceDTO dto);

//    Response update(AttendanceDTO dto);

    Response getDaily(String userId, String date);

//    Response getMonthly(String userId, String startDate, String endDate);



	Response getMonthlyReport(String userId, String month);

	Response updateActivity(AttendanceDTO dto);

//	Response getDailyByClinicAndBranch(String clinicId, String branchId, String date);


	Response getDailyByClinicAndBranch(String clinicId, String branchId, String date);


//	Response getMonthlyByClinicAndBranch(String clinicId, String branchId, String userId, String startDate,
//			String endDate


			Response getMonthlyByClinicAndBranch(String clinicId, String branchId, String userId, String startDate,
					String endDate);

			Response getUserDetailsByMobile(String mobileNumber);


			Response updateStatus(String userId, String clinicId, String branchId, String status, String reason);



//	Response getByClinicBranch(String clinicId, String branchId, String date);
}
