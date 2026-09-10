package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapistRecordDTO;
import com.clinicadmin.dto.TherapistRecordRequest;

public interface TherapistRecordService {

    ResponseStructure<TherapistRecordDTO> saveRecord(TherapistRecordDTO dto);

    ResponseStructure<TherapistRecordDTO> getByIds(
            String clinicId, String branchId, String therapistRecordId,String sessionId);



	ResponseStructure<List<TherapistRecordDTO>> getByPatientIdAndBookingId(String patientId, String bookingId);

	ResponseStructure<TherapistRecordDTO> getBySession(String clinicId, String branchId, String bookingId,
			String patientId, String sessionId);
	 
	public Response getTherapistSessionDetails(
	            TherapistRecordRequest request);

	ResponseStructure<TherapistRecordDTO> getCompletedTherapyRecord(String clinicId, String branchId,
			String therapistRecordId, String sessionId);


}