package com.clinicadmin.service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TreatmentDTO;
import org.springframework.http.ResponseEntity;

public interface TreatmentService {
    Response addTreatment(TreatmentDTO dto);
    Response getAllTreatments();
//    Response getTreatmentById(String id);
//    Response deleteTreatmentById(String id);
//    Response updateTreatmentById(String id, TreatmentDTO dto);
	Response getTreatmentById(String id, String hospitalId);
	Response deleteTreatmentById(String id, String hospitalId);
	Response updateTreatmentById(String id, String hospitalId, TreatmentDTO dto);
	Response getAllTreatmentsByHospitalId(String hospitalId);
	Response addOrGetTreatment(TreatmentDTO dto);

}
