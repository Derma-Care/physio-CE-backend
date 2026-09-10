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

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TreatmentDTO;
import com.clinicadmin.entity.Treatment;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.TreatmentRepository;
import com.clinicadmin.service.TreatmentService;

@Service
public class TreatmentServiceImpl implements TreatmentService {

    @Autowired
    private TreatmentRepository treatmentRepository;
    
    @Autowired                                        
    AdminServiceClient adminServiceClient;

    @Override
    public Response addTreatment(TreatmentDTO dto) {
        Response response = new Response();

        // Check if hospital exists by calling admin service
        ResponseEntity<Response> clinicResponseEntity = adminServiceClient.getClinicById(dto.getHospitalId());
        Response clinicResponse = clinicResponseEntity.getBody();

        // Validate the clinic response
        if (clinicResponse == null || !clinicResponse.isSuccess() || clinicResponse.getData() == null) {
            response.setSuccess(false);
            response.setMessage("Hospital with ID " + dto.getHospitalId() + " does not exist.");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        }

        // 🔑 Check for duplicate treatment in the same hospital
        Optional<Treatment> existing = treatmentRepository
                .findByHospitalIdAndTreatmentName(dto.getHospitalId(), dto.getTreatmentName());

        if (existing.isPresent()) {
            response.setSuccess(false);
            response.setMessage("Treatment '" + dto.getTreatmentName() + "' already exists in this hospital.");
            response.setStatus(HttpStatus.CONFLICT.value()); // 409 Conflict
            return response;
        }

        // Proceed to save treatment
        Treatment treatment = new Treatment();
        treatment.setTreatmentName(dto.getTreatmentName());
        treatment.setHospitalId(dto.getHospitalId());
        Treatment saved = treatmentRepository.save(treatment);

        TreatmentDTO responseDto = new TreatmentDTO();
        responseDto.setId(saved.getId().toString());
        responseDto.setTreatmentName(saved.getTreatmentName());
        responseDto.setHospitalId(saved.getHospitalId());
        
        response.setSuccess(true);
        response.setData(responseDto);
        response.setMessage("Treatment added successfully");
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    @Override
    public Response getAllTreatments() {
        Response response = new Response();
        try {
            List<Treatment> treatments = treatmentRepository.findAll();
            if (!treatments.isEmpty()) {
                List<TreatmentDTO> dtoList = treatments.stream()
                        .map(t -> new TreatmentDTO(t.getId().toString(), t.getTreatmentName(),t.getHospitalId()))
                        .collect(Collectors.toList());
                response.setSuccess(true);
                response.setData(dtoList);
                response.setMessage("Treatments retrieved successfully");
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setSuccess(true);
                response.setData(Collections.emptyList());
                response.setMessage("No data found");
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error while retrieving treatments: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    @Override
    public Response getTreatmentById(String id,String hospitalId ) {
        Response response = new Response();
        try {
            Optional<Treatment> optional = treatmentRepository.findByIdAndHospitalId(new ObjectId(id),hospitalId);
            if (optional.isPresent()) {
                Treatment t = optional.get();
                TreatmentDTO dto = new TreatmentDTO(t.getId().toString(), t.getTreatmentName(),t.getHospitalId());
                response.setSuccess(true);
                response.setData(dto);
                response.setMessage("Treatment retrieved successfully");
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setSuccess(true);
                response.setData(Collections.emptyList());
                response.setMessage("No treatment found with ID: " + id);
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error retrieving treatment: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    @Override
    public Response deleteTreatmentById(String id ,String hospitalId) {
        Response response = new Response();
        try {
            Optional<Treatment> optional = treatmentRepository.findByIdAndHospitalId(new ObjectId(id),hospitalId);
            if (optional.isPresent()) {
                treatmentRepository.deleteById(new ObjectId(id));
                response.setSuccess(true);
                response.setMessage("Treatment deleted successfully");
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setSuccess(true);
                response.setMessage("No treatment found with ID: " + id);
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error deleting treatment: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    @Override
    public Response updateTreatmentById(String id, String hospitalId,TreatmentDTO dto) {
        Response response = new Response();
        try {
            Optional<Treatment> optional = treatmentRepository.findByIdAndHospitalId(new ObjectId(id),hospitalId);
            if (optional.isPresent()) {
                Treatment t = optional.get();
                t.setTreatmentName(dto.getTreatmentName());
                Treatment saved = treatmentRepository.save(t);
                TreatmentDTO updatedDto = new TreatmentDTO(saved.getId().toString(), saved.getTreatmentName(),saved.getHospitalId());
                response.setSuccess(true);
                response.setData(updatedDto);
                response.setMessage("Treatment updated successfully");
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setSuccess(true);
                response.setMessage("No treatment found with ID: " + id);
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error updating treatment: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
  //----------------------------------------------------Get All Treatments by HospitalId-------------------------------------------------
    @Override
    public Response getAllTreatmentsByHospitalId(String hospitalId) {
        Response response = new Response();
        try {
            List<Treatment> treatments = treatmentRepository.findByHospitalId(hospitalId);
            if (!treatments.isEmpty()) {
                List<TreatmentDTO> dtoList = treatments.stream()
                        .map(t -> new TreatmentDTO(t.getId().toString(), t.getTreatmentName(), t.getHospitalId()))
                        .collect(Collectors.toList());

                response.setSuccess(true);
                response.setData(dtoList);
                response.setMessage("Treatments retrieved successfully for hospitalId: " + hospitalId);
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setSuccess(true);
                response.setData(Collections.emptyList());
                response.setMessage("No treatments found for hospitalId: " + hospitalId);
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error while retrieving treatments by hospitalId: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }
    
    @Override
    public Response addOrGetTreatment(TreatmentDTO dto) {
        Response response = new Response();
        try {
            // 1️⃣ Check if hospital exists via Admin Service
            ResponseEntity<Response> clinicResponseEntity = adminServiceClient.getClinicById(dto.getHospitalId());
            Response clinicResponse = clinicResponseEntity.getBody();

            if (clinicResponse == null || !clinicResponse.isSuccess() || clinicResponse.getData() == null) {
                response.setSuccess(false);
                response.setMessage("Hospital with ID " + dto.getHospitalId() + " does not exist.");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return response;
            }

            // 2️⃣ Check if treatment already exists for this hospital
            Optional<Treatment> existingTreatment = treatmentRepository
                    .findByHospitalIdAndTreatmentName(dto.getHospitalId(), dto.getTreatmentName());

            if (existingTreatment.isPresent()) {
                Treatment t = existingTreatment.get();
                TreatmentDTO resDto = new TreatmentDTO(t.getId().toString(), t.getTreatmentName(), t.getHospitalId());
                response.setSuccess(true);
                response.setData(resDto);
                response.setMessage("Treatment already exists, returning existing record.");
                response.setStatus(HttpStatus.OK.value());
                return response;
            }

            // 3️⃣ If not found → Add new treatment
            Treatment newTreatment = new Treatment();
            newTreatment.setTreatmentName(dto.getTreatmentName());
            newTreatment.setHospitalId(dto.getHospitalId());

            Treatment saved = treatmentRepository.save(newTreatment);

            TreatmentDTO resDto = new TreatmentDTO(saved.getId().toString(), saved.getTreatmentName(), saved.getHospitalId());
            response.setSuccess(true);
            response.setData(resDto);
            response.setMessage("Treatment added successfully.");
            response.setStatus(HttpStatus.CREATED.value()); // 201
            return response;

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Exception in addOrGetTreatment: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }


}
