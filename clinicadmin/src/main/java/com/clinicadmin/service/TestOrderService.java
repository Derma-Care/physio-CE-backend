package com.clinicadmin.service;


import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TestOrderDTO;

public interface TestOrderService {

    Response create(TestOrderDTO dto);

    Response getAll();

    Response getById(String id);

    Response update(String id, TestOrderDTO dto);

    Response delete(String id);


	Response getByClinicIdBranchIdAndTestOrderId(String clinicId, String branchId, String id);

	Response getByClinicIdAndBranchId(String clinicId, String branchId);
	Response getByClinicBranchPatientBooking(
	        String clinicId,
	        String branchId,
	        String patientId,
	        String bookingId);

}