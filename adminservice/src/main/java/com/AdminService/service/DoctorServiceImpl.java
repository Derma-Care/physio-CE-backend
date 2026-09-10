package com.AdminService.service;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.AdminService.dto.DoctorsDTO;
import com.AdminService.feign.ClinicAdminFeign;
import com.AdminService.util.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final ClinicAdminFeign clinicAdminFeign;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<Response> addDoctor(DoctorsDTO dto) {
        try {
            return clinicAdminFeign.addDoctor(dto);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to add doctor");
        }
    }

    @Override
    public ResponseEntity<Response> getAllDoctors() {
        try {
            return clinicAdminFeign.getAllDoctors();
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to fetch doctors list");
        }
    }

    @Override
    public ResponseEntity<Response> getDoctorById(String doctorId) {
        try {
            return clinicAdminFeign.getDoctorById(doctorId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to fetch doctor details");
        }
    }

    @Override
    public ResponseEntity<Response> updateDoctorById(String doctorId, DoctorsDTO dto) {
        try {
            return clinicAdminFeign.updateDoctorById(doctorId, dto);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to update doctor");
        }
    }

    @Override
    public ResponseEntity<Response> deleteDoctorById(String doctorId) {
        try {
            return clinicAdminFeign.deleteDoctorById(doctorId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to delete doctor");
        }
    }

    @Override
    public ResponseEntity<Response> deleteDoctorsByClinic(String clinicId) {
        try {
            return clinicAdminFeign.deleteDoctorsByClinic(clinicId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to delete doctors by clinic");
        }
    }

    @Override
    public ResponseEntity<Response> getDoctorByClinicAndDoctorId(String clinicId, String doctorId) {
        try {
            return clinicAdminFeign.getDoctorByClinicAndDoctorId(clinicId, doctorId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to fetch doctor for clinic");
        }
    }

    @Override
    public ResponseEntity<Response> getDoctorsByHospitalId(String hospitalId) {
        try {
            return clinicAdminFeign.getDoctorsByHospitalId(hospitalId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to fetch doctors by hospital");
        }
    }

    @Override
    public ResponseEntity<Response> getDoctorsByHospitalIdAndBranchId(String hospitalId, String branchId) {
        try {
            return clinicAdminFeign.getDoctorsByHospitalIdAndBranchId(hospitalId, branchId);
        } catch (FeignException ex) {
            return handleFeignException(ex, "Failed to fetch doctors by branch");
        }
    }

    private ResponseEntity<Response> handleFeignException(FeignException ex, String adminMessage) {
        HttpStatus status = HttpStatus.resolve(ex.status()) != null
                ? HttpStatus.valueOf(ex.status())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            if (ex.responseBody().isPresent()) {
                String body = new String(ex.responseBody().get().array(), StandardCharsets.UTF_8);
                Response clinicResponse = objectMapper.readValue(body, Response.class);

                // Use Clinic Admin message if available, otherwise fallback to Admin message
                String finalMessage = (clinicResponse.getMessage() != null && !clinicResponse.getMessage().isEmpty())
                        ? clinicResponse.getMessage()
                        : adminMessage;

                clinicResponse.setMessage(finalMessage);
                clinicResponse.setSuccess(false);
                return ResponseEntity.status(status).body(clinicResponse);
            }
        } catch (Exception ignored) {}

        Response fallback = new Response();
        fallback.setSuccess(false);
        fallback.setMessage(adminMessage);
        fallback.setStatus(status.value());
        fallback.setData(null);
        return ResponseEntity.status(status).body(fallback);
    }
}
