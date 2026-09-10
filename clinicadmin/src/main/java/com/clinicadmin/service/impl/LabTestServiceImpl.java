package com.clinicadmin.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.LabTestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.LabTest;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.LabTestRepository;
import com.clinicadmin.service.LabTestService;

@Service
public class LabTestServiceImpl implements LabTestService {

	@Autowired
	private LabTestRepository labTestRepository;

	@Autowired
	AdminServiceClient adminServiceClient;

	@Override
	public Response addLabTest(LabTestDTO dto) {
	    Response response = new Response();

	    // Call the admin service to fetch clinic details
	    ResponseEntity<Response> clinicResponseEntity = adminServiceClient.getClinicById(dto.getHospitalId());
	    Response clinicResponse = clinicResponseEntity.getBody();

	    // Check if hospital exists
	    if (clinicResponse == null || !clinicResponse.isSuccess() || clinicResponse.getData() == null) {
	        response.setSuccess(false);
	        response.setMessage("Hospital with ID " + dto.getHospitalId() + " does not exist.");
	        response.setStatus(HttpStatus.NOT_FOUND.value());
	        return response;
	    }

	    // Check if test name already exists for the given hospital
	    Optional<LabTest> existingTest = labTestRepository.findByHospitalIdAndTestNameIgnoreCase(dto.getHospitalId(), dto.getTestName());
	    if (existingTest.isPresent()) {
	        response.setSuccess(false);
	        response.setMessage("Lab Test with name '" + dto.getTestName() + "' already exists in this hospital.");
	        response.setStatus(HttpStatus.CONFLICT.value()); // 409 Conflict
	        return response;
	    }

	    // Proceed with adding the lab test
	    LabTest test = new LabTest();
	    test.setTestName(dto.getTestName());
	    test.setHospitalId(dto.getHospitalId());
	    test.setDescription(dto.getDescription());
	    test.setPurpose(dto.getPurpose());

	    LabTest saved = labTestRepository.save(test);

	    LabTestDTO resDto = new LabTestDTO();
	    resDto.setId(saved.getId().toString());
	    resDto.setHospitalId(saved.getHospitalId());
	    resDto.setTestName(saved.getTestName());
	    resDto.setDescription(saved.getDescription());
	    resDto.setPurpose(saved.getPurpose());

	    response.setSuccess(true);
	    response.setData(resDto);
	    response.setMessage("Lab Test added successfully");
	    response.setStatus(HttpStatus.OK.value());
	    return response;
	}

	@Override
	public Response getAllLabTests() {
		Response response = new Response();
		try {
			List<LabTest> tests = labTestRepository.findAll();
			if (!tests.isEmpty()) {
				List<LabTestDTO> dtoList = tests.stream()
						.map(t -> new LabTestDTO(t.getId().toString(), t.getTestName(), t.getHospitalId(),t.getDescription(),t.getPurpose()))
						.collect(Collectors.toList());
				response.setSuccess(true);
				response.setData(dtoList);
				response.setMessage("Lab Tests retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("No Lab Tests found");
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Exception while fetching Lab Tests: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public Response getLabTestById(String id, String hospitalId) {
		Response response = new Response();
		try {
			Optional<LabTest> labTest = labTestRepository.findByIdAndHospitalId(new ObjectId(id), hospitalId);
			if (labTest.isPresent()) {
				LabTest test = labTest.get();
				LabTestDTO dto = new LabTestDTO(test.getId().toString(), test.getTestName(), test.getHospitalId(),test.getDescription(),test.getPurpose());
				response.setSuccess(true);
				response.setData(dto);
				response.setMessage("Lab Test retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(true);
				response.setData(Collections.emptyList());
				response.setMessage("Lab Test not found with ID: " + id);
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Exception while fetching Lab Test: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public Response deleteLabTestById(String id, String hospitalId) {
		Response response = new Response();
		try {
			ObjectId objectId = new ObjectId(id);
			Optional<LabTest> test = labTestRepository.findByIdAndHospitalId(objectId, hospitalId);
			if (test.isPresent()) {
				labTestRepository.deleteById(objectId);
				response.setSuccess(true);
				response.setMessage("Lab Test deleted successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(true);
				response.setMessage("Lab Test not found with ID: " + id);
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Exception while deleting Lab Test: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public Response updateLabTestById(String id, String hospitalId, LabTestDTO dto) {
		Response response = new Response();
		try {
			Optional<LabTest> test = labTestRepository.findByIdAndHospitalId(new ObjectId(id), hospitalId);
			if (test.isPresent()) {
				LabTest update = test.get();
				update.setTestName(dto.getTestName());
				update.setDescription(dto.getDescription());
				update.setPurpose(dto.getPurpose());
				LabTest saved = labTestRepository.save(update);
				LabTestDTO updatedDTO = new LabTestDTO(saved.getId().toString(), saved.getTestName(),
						saved.getHospitalId(),saved.getDescription(),saved.getPurpose());

				response.setSuccess(true);
				response.setData(updatedDTO);
				response.setMessage("Lab Test updated successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(true);
				response.setMessage("Lab Test not found with ID: " + id);
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("Exception while updating Lab Test: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}
	//----------------------------------------------------Get All Lab Tests by HospitalId-------------------------------------------------
	@Override
	public Response getAllLabTestsByHospitalId(String hospitalId) {
	    Response response = new Response();
	    try {
	        List<LabTest> tests = labTestRepository.findByHospitalId(hospitalId);
	        if (!tests.isEmpty()) {
	            List<LabTestDTO> dtoList = tests.stream()
	                    .map(t -> new LabTestDTO(t.getId().toString(), t.getTestName(), t.getHospitalId(),t.getDescription(),t.getPurpose()))
	                    .collect(Collectors.toList());

	            response.setSuccess(true);
	            response.setData(dtoList);
	            response.setMessage("Lab Tests retrieved successfully for hospitalId: " + hospitalId);
	            response.setStatus(HttpStatus.OK.value());
	        } else {
	            response.setSuccess(true);
	            response.setData(Collections.emptyList());
	            response.setMessage("No Lab Tests found for hospitalId: " + hospitalId);
	            response.setStatus(HttpStatus.OK.value());
	        }
	    } catch (Exception e) {
	        response.setSuccess(false);
	        response.setMessage("Exception while retrieving Lab Tests by hospitalId: " + e.getMessage());
	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
	    }
	    return response;
	}
	@Override
	public Response addOrGetLabTest(LabTestDTO dto) {
	    Response response = new Response();
	    try {
	        // First check hospital exists via Admin Service
	        ResponseEntity<Response> clinicResponseEntity = adminServiceClient.getClinicById(dto.getHospitalId());
	        Response clinicResponse = clinicResponseEntity.getBody();

	        if (clinicResponse == null || !clinicResponse.isSuccess() || clinicResponse.getData() == null) {
	            response.setSuccess(false);
	            response.setMessage("Hospital with ID " + dto.getHospitalId() + " does not exist.");
	            response.setStatus(HttpStatus.NOT_FOUND.value());
	            return response;
	        }

	        // Search if test already exists
	        Optional<LabTest> existingTest = labTestRepository
	                .findByHospitalIdAndTestNameIgnoreCase(dto.getHospitalId(), dto.getTestName());

	        if (existingTest.isPresent()) {
	            LabTest test = existingTest.get();
	            LabTestDTO resDto = new LabTestDTO(
	                    test.getId().toString(),
	                    test.getTestName(),
	                    test.getHospitalId(),
	                    test.getDescription(),
	                    test.getPurpose()
	            );
	            response.setSuccess(true);
	            response.setData(resDto);
	            response.setMessage("Lab Test already exists, returning existing record.");
	            response.setStatus(HttpStatus.OK.value());
	            return response;
	        }

	        // If not present → Add new test
	        LabTest newTest = new LabTest();
	        newTest.setTestName(dto.getTestName());
	        newTest.setHospitalId(dto.getHospitalId());
	        newTest.setDescription(dto.getDescription());
	        newTest.setPurpose(dto.getPurpose());

	        LabTest saved = labTestRepository.save(newTest);

	        LabTestDTO resDto = new LabTestDTO(
	                saved.getId().toString(),
	                saved.getTestName(),
	                saved.getHospitalId(),
	                saved.getDescription(),
	                saved.getPurpose()
	        );

	        response.setSuccess(true);
	        response.setData(resDto);
	        response.setMessage("Lab Test added successfully.");
	        response.setStatus(HttpStatus.CREATED.value()); // 201
	        return response;

	    } catch (Exception e) {
	        response.setSuccess(false);
	        response.setMessage("Exception in addOrGetLabTest: " + e.getMessage());
	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
	        return response;
	    }
	}


}
