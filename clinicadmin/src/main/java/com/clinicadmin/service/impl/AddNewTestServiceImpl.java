package com.clinicadmin.service.impl;



import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.AddNewTestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.AddNewTest;
import com.clinicadmin.repository.AddNewTestRepository;
import com.clinicadmin.service.AddNewTestService;

@Service
public class AddNewTestServiceImpl implements AddNewTestService {

    @Autowired
    private AddNewTestRepository repository;

    @Override
    public Response addTest(AddNewTestDTO dto) {

        AddNewTest entity = mapToEntity(dto);

        // Generate Custom Test ID
        entity.setId(generateTestId());

        // Save to MongoDB
        AddNewTest savedEntity = repository.save(entity);

        // Prepare Response
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage("Test Added Successfully");
        response.setData(mapToDTO(savedEntity));
        response.setStatus(200);

        return response;
    }
    @Override
    public Response updateTest(String id, AddNewTestDTO dto) {

        Response response = new Response();

        try {

            AddNewTest entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Not Found"));

//            entity.setTestName(dto.getTestName());
            entity.setVendor(dto.getVendor());
            entity.setVendorEmailId(dto.getVendorEmailId());
            entity.setClinicId(dto.getClinicId());
            entity.setBranchId(dto.getBranchId());

            AddNewTest updatedEntity = repository.save(entity);

            response.setSuccess(true);
            response.setMessage("Test Updated Successfully");
            response.setData(mapToDTO(updatedEntity));
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to update test: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response deleteTest(String id) {

        Response response = new Response();

        try {

            AddNewTest entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Not Found"));

            repository.delete(entity);

            response.setSuccess(true);
            response.setMessage("Test Deleted Successfully");
            response.setData(null);
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to delete test: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }
    @Override
    public Response getById(String id) {

        Response response = new Response();

        try {

            AddNewTest entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Not Found"));

            response.setSuccess(true);
            response.setMessage("Test Retrieved Successfully");
            response.setData(mapToDTO(entity));
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve test: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getAll() {

        Response response = new Response();

        try {

            List<AddNewTestDTO> list = repository.findAll()
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Tests Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve tests: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getByClinicId(String clinicId) {

        Response response = new Response();

        try {

            List<AddNewTestDTO> list = repository.findByClinicId(clinicId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Tests Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve tests: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getByClinicIdAndBranchId(String clinicId, String branchId) {

        Response response = new Response();

        try {

            List<AddNewTestDTO> list = repository
                    .findByClinicIdAndBranchId(clinicId, branchId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            if (list.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("No Tests Found");
                response.setData(null);
                response.setStatus(404);
                return response;
            }

            response.setSuccess(true);
            response.setMessage("Tests Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve tests: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    // ===========================
    // Entity -> DTO
    // ===========================

    private AddNewTestDTO mapToDTO(AddNewTest entity) {

        AddNewTestDTO dto = new AddNewTestDTO();

        dto.setId(entity.getId());
//        dto.setTestName(entity.getTestName());
        dto.setVendor(entity.getVendor());
        dto.setVendorEmailId(entity.getVendorEmailId());
        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());

        return dto;
    }

    // ===========================
    // DTO -> Entity
    // ===========================

    private AddNewTest mapToEntity(AddNewTestDTO dto) {

        AddNewTest entity = new AddNewTest();

//        entity.setTestName(dto.getTestName());
        entity.setVendor(dto.getVendor());
        entity.setVendorEmailId(dto.getVendorEmailId());
        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());

        return entity;
    }

    // ===========================
    // Custom ID Generator
    // ===========================

    private String generateTestId() {

        String id;

        do {

            id = "TST-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();

        } while (repository.existsById(id));

        return id;
    }

    @Override
    public Response getByClinicIdBranchIdAndTestId(String clinicId, String branchId, String Id) {

        Response response = new Response();

        try {

            AddNewTest test = repository.findByClinicIdAndBranchIdAndId(
                    clinicId, branchId, Id);

            if (test == null) {
                response.setSuccess(false);
                response.setMessage("Test Not Found");
                response.setData(null);
                response.setStatus(404);
                return response;
            }

            response.setSuccess(true);
            response.setMessage("Test Retrieved Successfully");
            response.setData(mapToDTO(test));
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve test: " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }
}