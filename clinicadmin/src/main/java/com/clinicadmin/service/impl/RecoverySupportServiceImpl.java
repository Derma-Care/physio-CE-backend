package com.clinicadmin.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.RecoverySupportDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.RecoverySupport;
import com.clinicadmin.repository.RecoverySupportRepository;
import com.clinicadmin.service.RecoverySupportService;
import com.clinicadmin.service.S3Service;

@Service
public class RecoverySupportServiceImpl implements RecoverySupportService {

    @Autowired
    private RecoverySupportRepository repository;

    @Autowired
    private S3Service s3Service;

    @Override
    public Response saveRecoverySupport(RecoverySupportDTO dto) {

        Response response = new Response();

        RecoverySupport support = convertToEntity(dto);

        repository.save(support);

        response.setSuccess(true);
        response.setData(convertToDto(support));
        response.setMessage("Recovery support saved successfully");
        response.setStatus(HttpStatus.CREATED.value());

        return response;
    }

    @Override
    public Response getAllRecoverySupports() {

        Response response = new Response();

        List<RecoverySupportDTO> data = repository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        response.setSuccess(true);
        response.setData(data);
        response.setMessage("Recovery support list fetched successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }

    @Override
    public Response getRecoverySupportById(String id) {

        Response response = new Response();

        Optional<RecoverySupport> optional = repository.findById(id);

        if (optional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Recovery support not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        }

        response.setSuccess(true);
        response.setData(convertToDto(optional.get()));
        response.setMessage("Recovery support fetched successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }

    @Override
    public Response updateRecoverySupport(String id, RecoverySupportDTO dto) {

        Response response = new Response();

        Optional<RecoverySupport> optional = repository.findById(id);

        if (optional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Recovery support not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        }

        RecoverySupport support = optional.get();

        // update fields directly
        support.setClinicId(dto.getClinicId());
        support.setName(dto.getName());
        support.setDescription(dto.getDescription());
        support.setImage(dto.getImage());
        support.setCategory(dto.getCategory());

        repository.save(support);

        response.setSuccess(true);
        response.setData(convertToDto(support));
        response.setMessage("Recovery support updated successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }

    @Override
    public Response deleteRecoverySupport(String id) {

        Response response = new Response();

        Optional<RecoverySupport> optional = repository.findById(id);

        if (optional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Recovery support not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        }

        repository.deleteById(id);

        response.setSuccess(true);
        response.setMessage("Recovery support deleted successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }
    
    @Override
    public Response getRecoverySupportsByClinicId(String clinicId) {

        Response response = new Response();

        List<RecoverySupportDTO> recoverySupports = repository.findByClinicId(clinicId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        response.setSuccess(true);
        response.setData(recoverySupports);
        response.setMessage("Recovery supports fetched successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }
    
    @Override
    public  Response getRecoverySupportByClinicIdAndId(String clinicId, String id){

        Response response = new Response();

        Optional<RecoverySupport> optional =
                repository.findByClinicIdAndId(clinicId, id);

        if (optional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Recovery support not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        }

        response.setSuccess(true);
        response.setData(convertToDto(optional.get()));
        response.setMessage("Recovery support fetched successfully");
        response.setStatus(HttpStatus.OK.value());

        return response;
    }
    
    private RecoverySupport convertToEntity(RecoverySupportDTO dto) {

        RecoverySupport support = new RecoverySupport();

        support.setClinicId(dto.getClinicId());
        support.setName(dto.getName());
        support.setDescription(dto.getDescription());
        support.setImage(dto.getImage());
        support.setCategory(dto.getCategory());

        return support;
    }

    private RecoverySupportDTO convertToDto(RecoverySupport support) {

        RecoverySupportDTO dto = new RecoverySupportDTO();

        dto.setId(support.getId());
        dto.setClinicId(support.getClinicId());
        dto.setName(support.getName());
        dto.setDescription(support.getDescription());
        dto.setCategory(support.getCategory());

        if (support.getImage() != null && !support.getImage().isBlank()) {
            dto.setImage(s3Service.generateSignedUrl(support.getImage()));
        }

        return dto;
    }
}