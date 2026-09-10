package com.clinicadmin.service.impl;


import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TestOrderDTO;
import com.clinicadmin.entity.TestOrder;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.TestOrderRepository;
import com.clinicadmin.service.EmailService;
import com.clinicadmin.service.TestOrderService;

@Service
public class TestOrderServiceImpl implements TestOrderService {
	@Autowired
	private AdminServiceClient adminServiceClient;
    @Autowired
    private TestOrderRepository repository;
    @Autowired
    private EmailService
    emailService;
    @Override
    public Response create(TestOrderDTO dto) {

        Response response = new Response();

        try {

            // Convert DTO to Entity
            TestOrder entity = mapToEntity(dto);

            // Generate Test Order ID
            entity.setId(generateTestOrderId());

            // Default Status
            if (entity.getStatus() == null || entity.getStatus().isBlank()) {
                entity.setStatus("Pending");
            }

            // Ordered Time
            if (entity.getOrderedAt() == null) {
                entity.setOrderedAt(Instant.now());
            }

            // Save Test Order
            TestOrder savedEntity = repository.save(entity);

            // ============================================
            // Fetch Clinic & Branch Details
            // ============================================

            String clinicName = "";
            String branchName = "";
            String branchEmail = "";
            String branchContactNumber = "";

            try {

                // Get Clinic Details
                ResponseEntity<Response> clinicResponse =
                        adminServiceClient.getClinicById(savedEntity.getClinicId());

                if (clinicResponse.getBody() != null &&
                        clinicResponse.getBody().getData() != null) {

                    Map<String, Object> clinic =
                            (Map<String, Object>) clinicResponse.getBody().getData();

                    clinicName = String.valueOf(clinic.get("name"));
                }

                // Get Branch Details
                ResponseEntity<Response> branchResponse =
                        adminServiceClient.getBranchByClinicAndBranchId(
                                savedEntity.getClinicId(),
                                savedEntity.getBranchId());

                if (branchResponse.getBody() != null &&
                        branchResponse.getBody().getData() != null) {

                    Map<String, Object> branch =
                            (Map<String, Object>) branchResponse.getBody().getData();

                    branchName = String.valueOf(branch.get("branchName"));
                    branchEmail = String.valueOf(branch.get("email"));
                    branchContactNumber = String.valueOf(branch.get("contactNumber"));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // ============================================
            // Send Email to Vendor
            // ============================================

            if (savedEntity.getVendorEmail() != null &&
                    !savedEntity.getVendorEmail().isBlank()) {

                emailService.sendTestOrderEmail(
                        savedEntity,
                        clinicName,
                        branchName,
                        branchEmail,
                        branchContactNumber
                );
            }

            response.setSuccess(true);
            response.setMessage("Test Order Created Successfully");
            response.setData(mapToDTO(savedEntity));
            response.setStatus(200);

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage("Failed to create Test Order : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }
    @Override
    public Response update(String id, TestOrderDTO dto) {

        Response response = new Response();

        try {

            TestOrder entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Order Not Found"));

            // Update only non-null fields

            if (dto.getPatientId() != null)
                entity.setPatientId(dto.getPatientId());

            if (dto.getPatientName() != null)
                entity.setPatientName(dto.getPatientName());

            if (dto.getMobileNumber() != null)
                entity.setMobileNumber(dto.getMobileNumber());

            if (dto.getClinicId() != null)
                entity.setClinicId(dto.getClinicId());

            if (dto.getBranchId() != null)
                entity.setBranchId(dto.getBranchId());

            if (dto.getBookingId() != null)
                entity.setBookingId(dto.getBookingId());

            if (dto.getDoctorId() != null)
                entity.setDoctorId(dto.getDoctorId());

            if (dto.getDoctorName() != null)
                entity.setDoctorName(dto.getDoctorName());

            if (dto.getTestName() != null)
                entity.setTestName(dto.getTestName());

            if (dto.getTestNameId() != null)
                entity.setTestNameId(dto.getTestNameId());

            if (dto.getVendorId() != null)
                entity.setVendorId(dto.getVendorId());

            if (dto.getVendorName() != null)
                entity.setVendorName(dto.getVendorName());

            if (dto.getVendorEmail() != null)
                entity.setVendorEmail(dto.getVendorEmail());

            if (dto.getStatus() != null)
                entity.setStatus(dto.getStatus());

            if (dto.getOrderedAt() != null)
                entity.setOrderedAt(dto.getOrderedAt());

            TestOrder updatedEntity = repository.save(entity);

            response.setSuccess(true);
            response.setMessage("Test Order Updated Successfully");
            response.setData(mapToDTO(updatedEntity));
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to update Test Order : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response delete(String id) {

        Response response = new Response();

        try {

            TestOrder entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Order Not Found"));

            repository.delete(entity);

            response.setSuccess(true);
            response.setMessage("Test Order Deleted Successfully");
            response.setData(null);
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to delete Test Order : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getById(String id) {

        Response response = new Response();

        try {

            TestOrder entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test Order Not Found"));

            response.setSuccess(true);
            response.setMessage("Test Order Retrieved Successfully");
            response.setData(mapToDTO(entity));
            response.setStatus(200);

        } catch (RuntimeException e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setStatus(404);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve Test Order : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getAll() {

        Response response = new Response();

        try {

            List<TestOrderDTO> list = repository.findAll()
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Test Orders Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve Test Orders : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getByClinicIdAndBranchId(String clinicId, String branchId) {

        Response response = new Response();

        try {

            List<TestOrderDTO> list = repository
                    .findByClinicIdAndBranchId(clinicId, branchId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            if (list.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("No Test Orders Found");
                response.setData(null);
                response.setStatus(404);

                return response;
            }

            response.setSuccess(true);
            response.setMessage("Test Orders Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve Test Orders : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    @Override
    public Response getByClinicIdBranchIdAndTestOrderId(String clinicId,
                                                        String branchId,
                                                        String id) {

        Response response = new Response();

        try {

            TestOrder entity = repository.findByClinicIdAndBranchIdAndId(
                    clinicId,
                    branchId,
                    id);

            if (entity == null) {

                response.setSuccess(false);
                response.setMessage("Test Order Not Found");
                response.setData(null);
                response.setStatus(404);

                return response;
            }

            response.setSuccess(true);
            response.setMessage("Test Order Retrieved Successfully");
            response.setData(mapToDTO(entity));
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve Test Order : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }

    // ==========================
    // Entity -> DTO
    // ==========================

    private TestOrderDTO mapToDTO(TestOrder entity) {

        TestOrderDTO dto = new TestOrderDTO();

        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatientId());
        dto.setPatientName(entity.getPatientName());
        dto.setMobileNumber(entity.getMobileNumber());

        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());

        dto.setBookingId(entity.getBookingId());

        dto.setDoctorId(entity.getDoctorId());
        dto.setDoctorName(entity.getDoctorName());

        dto.setTestName(entity.getTestName());
        dto.setTestNameId(entity.getTestNameId());

        dto.setVendorId(entity.getVendorId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorEmail(entity.getVendorEmail());

        dto.setStatus(entity.getStatus());
        dto.setOrderedAt(entity.getOrderedAt());

        return dto;
    }

    // ==========================
    // DTO -> Entity
    // ==========================

    private TestOrder mapToEntity(TestOrderDTO dto) {

        TestOrder entity = new TestOrder();

        entity.setPatientId(dto.getPatientId());
        entity.setPatientName(dto.getPatientName());
        entity.setMobileNumber(dto.getMobileNumber());

        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());

        entity.setBookingId(dto.getBookingId());

        entity.setDoctorId(dto.getDoctorId());
        entity.setDoctorName(dto.getDoctorName());

        entity.setTestName(dto.getTestName());
        entity.setTestNameId(dto.getTestNameId());

        entity.setVendorId(dto.getVendorId());
        entity.setVendorName(dto.getVendorName());
        entity.setVendorEmail(dto.getVendorEmail());

        entity.setStatus(dto.getStatus());
        entity.setOrderedAt(dto.getOrderedAt());

        return entity;
    }

    // ==========================
    // Custom ID Generator
    // ==========================

    private String generateTestOrderId() {

        String id;

        do {

            id = "TEST-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

        } while (repository.existsById(id));

        return id;
    }
    @Override
    public Response getByClinicBranchPatientBooking(
            String clinicId,
            String branchId,
            String patientId,
            String bookingId) {

        Response response = new Response();

        try {

            List<TestOrderDTO> list = repository
                    .findByClinicIdAndBranchIdAndPatientIdAndBookingId(
                            clinicId, branchId, patientId, bookingId)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            if (list.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("No Test Orders Found");
                response.setData(null);
                response.setStatus(404);
                return response;
            }

            response.setSuccess(true);
            response.setMessage("Test Orders Retrieved Successfully");
            response.setData(list);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve Test Orders : " + e.getMessage());
            response.setData(null);
            response.setStatus(500);
        }

        return response;
    }
}