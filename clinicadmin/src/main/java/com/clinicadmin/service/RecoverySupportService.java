package com.clinicadmin.service;

import com.clinicadmin.dto.RecoverySupportDTO;
import com.clinicadmin.dto.Response;

public interface RecoverySupportService {

    Response saveRecoverySupport(RecoverySupportDTO dto);

    Response getAllRecoverySupports();

    Response getRecoverySupportById(String id);

//    Response getRecoverySupportByClinicIdAndId(String clinicId, String id);

    Response updateRecoverySupport(String id, RecoverySupportDTO dto);

    Response deleteRecoverySupport(String id);

	Response getRecoverySupportByClinicIdAndId(String clinicId, String id);

	Response getRecoverySupportsByClinicId(String clinicId);



}