package com.clinicadmin.service;

import org.springframework.stereotype.Service;

import com.clinicadmin.dto.ReportsDtoList;
import com.clinicadmin.dto.Response;

@Service
public interface ReportsService {

	Response saveReports(ReportsDtoList dto);

	Response getAllReports();

	Response getReportsByBookingId(String bookingId);
	public Response getReportsByCustomerId(String cId);

	Response getReportsByPatientIdAndBookingId(String patientId, String bookingId);

	Response deleteReport(String reportId);

	Response updateReport(String reportId, ReportsDtoList dto);

	Response deleteReportFile(String reportId, String bookingId, int fileIndex);

}
