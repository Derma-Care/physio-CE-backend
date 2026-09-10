package com.clinicadmin.service;

import com.clinicadmin.dto.NurseDTO;
import com.clinicadmin.dto.Response;

public interface NurseService {

	public Response nureseOnboarding(NurseDTO dto);

    public Response getAllNursesByHospital(String hospitalId);

    public Response getNurseByHospitalAndNurseId(String hospitalId, String nurseId);

//    public Response updateNurse(String hospitalId, String nurseId, NurseDTO dto);

    public Response deleteNurse(String hospitalId, String nurseId);

	public Response updateNurse(String nurseId, NurseDTO dto);

	Response getAllNursesByBranchIdAndHospitalId(String hospitalId, String branchId);

//	public Response nurseLogin(NurseLoginDTO loginDTO);
//
//	Response resetLoginPassword(ResetNurseLoginPasswordDTO dto);

}
