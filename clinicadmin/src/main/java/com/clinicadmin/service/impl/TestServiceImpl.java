package com.clinicadmin.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.clinicadmin.dto.TestDTO;
import com.clinicadmin.entity.Test;
import com.clinicadmin.repository.TestRepository;
import com.clinicadmin.service.TestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestRepository repository;

    @Override
    public TestDTO addTest(TestDTO dto) {

        if (repository.existsByTestNameAndClinicIdAndBranchId(
                dto.getTestName(),
                dto.getClinicId(),
                dto.getBranchId())) {

            throw new RuntimeException("Test already exists.");
        }

        Test test = new Test();

        test.setTestId(TestIdGenerator.generateTestId());
        test.setTestName(dto.getTestName());
        test.setClinicId(dto.getClinicId());
        test.setBranchId(dto.getBranchId());

        repository.save(test);

        dto.setTestId(test.getTestId());

        return dto;
    }

    @Override
    public List<TestDTO> getAllTests() {

        return repository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TestDTO getByTestId(String testId) {

        Test test = repository.findByTestId(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        return convertToDTO(test);
    }

    @Override
    public TestDTO updateTest(String testId, TestDTO dto) {

        Test test = repository.findByTestId(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        test.setTestName(dto.getTestName());
        test.setClinicId(dto.getClinicId());
        test.setBranchId(dto.getBranchId());

        repository.save(test);

        return convertToDTO(test);
    }

    @Override
    public String deleteTest(String testId) {

        Test test = repository.findByTestId(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        repository.delete(test);

        return "Test deleted successfully.";
    }

    private TestDTO convertToDTO(Test test) {

        TestDTO dto = new TestDTO();

        dto.setTestId(test.getTestId());
        dto.setTestName(test.getTestName());
        dto.setClinicId(test.getClinicId());
        dto.setBranchId(test.getBranchId());

        return dto;
    }
    public class TestIdGenerator {

        public static String generateTestId() {

            return "TST-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 6)
                            .toUpperCase();
        }
    }
    @Override
    public TestDTO getByClinicIdAndBranchIdAndTestId(
            String clinicId,
            String branchId,
            String testId) {

        Test test = repository.findByClinicIdAndBranchIdAndTestId(
                clinicId,
                branchId,
                testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        return convertToDTO(test);
    }
    @Override
    public List<TestDTO> getByClinicIdAndBranchId(String clinicId, String branchId) {

        return repository.findByClinicIdAndBranchId(clinicId, branchId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

}