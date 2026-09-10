package com.clinicadmin.service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapyServiceDTO;

public interface TherapyServiceService {

    Response createTherapy(TherapyServiceDTO dto);

    Response getByClinicAndBranch(String clinicId, String branchId);

    Response getByIdClinicBranch(String id, String clinicId, String branchId);

    Response updateTherapyById(String id, TherapyServiceDTO dto);

    Response deleteTherapyById(String id);

	Response getTherapyWithExercises(String id, String clinicId, String branchId);
	public Response getByClinicId(String clinicId) ;

}