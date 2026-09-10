package com.clinicadmin.service;

import com.clinicadmin.dto.LabTestDTO;
import com.clinicadmin.dto.Response;

public interface LabTestService {
    public Response addLabTest(LabTestDTO dto);
    public Response getAllLabTests();
//    public Response getLabTestById(String id);
//    public Response deleteLabTestById(String id);
//    public Response updateLabTestById(String id, LabTestDTO dto);
	Response getLabTestById(String id, String hospitalId);
	Response deleteLabTestById(String id, String hospitalId);
	Response updateLabTestById(String id, String hospitalId, LabTestDTO dto);
	Response getAllLabTestsByHospitalId(String hospitalId);
	Response addOrGetLabTest(LabTestDTO dto);
}
