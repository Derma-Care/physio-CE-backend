package com.clinicadmin.service;

import java.util.List;
import com.clinicadmin.dto.PatientFeedbackDTO;
import com.clinicadmin.dto.Response;

public interface PatientFeedbackService {

    Response createFeedback(PatientFeedbackDTO dto);

    Response getAllFeedbacks();

    Response getFeedbackById(String id);

    Response updateFeedback(String id,
                            PatientFeedbackDTO dto);

    Response deleteFeedback(String id);

	Response getByClinicIdAndBranchId(String clinicId, String branchId);

	Response getDoctorFeedbackSummary(String doctorId, String clinicId);
	
	 public Response getByClinicIdAndBranchIdAndPatientId(String clinicId,
             String branchId,String patientId);
  
 
}