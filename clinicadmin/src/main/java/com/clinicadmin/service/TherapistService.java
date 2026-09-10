package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.ChangeDoctorPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapistDTO;
import com.clinicadmin.dto.TherapistPresenceRequest;

public interface TherapistService {

//    ResponseStructure<TherapistLoginResponseDTO> login(TherapistLoginDTO dto);

	ResponseStructure<TherapistDTO> getBytherapistId(String therapistId);

	ResponseStructure<List<TherapistDTO>> getByClinicIdBranchIdAndTherapistId(String clinicId, String branchId,
			String therapistId);

	ResponseStructure<List<TherapistDTO>> getByClinicIdAndBranchId(String clinicId, String branchId);

	ResponseStructure<TherapistDTO> updateBytherapistId(String therapistId, TherapistDTO dto);

	ResponseStructure<String> deleteBytherapistId(String therapistId);

	Response therapistOnboarding(TherapistDTO dto);

	Response getPaidSessions(String clinicId, String branchId, String bookingId, String therapistRecordId);

//	Response getTherapistPerformanceSummary(String clinicId, String branchId, String therapistId);

	Response getTherapistPerformanceSummary(String clinicId, String branchId, String therapistId, int year);

	Response getTherapistData(String clinicId, String branchId);

	Response updateTherapistPresence(String therapistId, TherapistPresenceRequest request);

	Response getTherapistFeedback(String clinicId, String branchId, String therapistId);

	Response getTherapistsWithServices(String clinicId, String branchId);

	Response changePassword(ChangeDoctorPasswordDTO updateDTO);

}