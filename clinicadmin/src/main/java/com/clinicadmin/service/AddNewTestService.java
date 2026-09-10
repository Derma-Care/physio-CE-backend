package com.clinicadmin.service;



import com.clinicadmin.dto.AddNewTestDTO;
import com.clinicadmin.dto.Response;

public interface AddNewTestService {

    Response addTest(AddNewTestDTO dto);

    Response updateTest(String id, AddNewTestDTO dto);

    Response deleteTest(String id);

    Response getById(String id);

    Response getAll();

    Response getByClinicId(String clinicId);

    Response getByClinicIdAndBranchId(String clinicId, String branchId);
    Response getByClinicIdBranchIdAndTestId(String clinicId, String branchId, String testId);

}