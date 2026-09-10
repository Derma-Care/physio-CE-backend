package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.TestDTO;

public interface TestService {

    TestDTO addTest(TestDTO dto);

    List<TestDTO> getAllTests();

    TestDTO getByTestId(String testId);

    TestDTO updateTest(String testId, TestDTO dto);

    String deleteTest(String testId);
    TestDTO getByClinicIdAndBranchIdAndTestId(
            String clinicId,
            String branchId,
            String testId);
    List<TestDTO> getByClinicIdAndBranchId(String clinicId, String branchId);

}