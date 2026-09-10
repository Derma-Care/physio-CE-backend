package com.clinicadmin.service.impl;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.clinicadmin.dto.EquipmentDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Equipment;
import com.clinicadmin.repository.EquipmentRepository;
import com.clinicadmin.service.EquipmentService;
import com.clinicadmin.service.S3Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository repository;
    
    private final S3Service s3Service;

    @Override
    public Response createEquipment(EquipmentDTO dto) {

        Equipment equipment = convertToEntity(dto);
        equipment.setEquipmentId(generateEquipmentId());

        Equipment savedEquipment = repository.save(equipment);

        Response response = new Response();
        response.setSuccess(true);
        response.setMessage("Equipment Created Successfully");
        response.setStatus(201);
        response.setData(convertToDto(savedEquipment));

        return response;
    }
  
    @Override
    public Response getEquipmentById(String equipmentId) {

        Equipment equipment = repository.findById(equipmentId).orElse(null);

        Response response = new Response();

        if (equipment == null) {
            response.setSuccess(false);
            response.setMessage("Equipment Not Found");
            response.setStatus(404);
            return response;
        }

        response.setSuccess(true);
        response.setMessage("Equipment Retrieved Successfully");
        response.setStatus(200);
        response.setData(convertToDto(equipment));

        return response;
    }
 		@Override
    		public Response getAllEquipment() {

    		    List<EquipmentDTO> equipmentList = repository.findAll()
    		            .stream()
    		            .map(this::convertToDto)
    		            .collect(Collectors.toList());

    		    Response response = new Response();

    		    if (equipmentList.isEmpty()) {
    		        response.setSuccess(false);
    		        response.setMessage("No Equipment Records Found");
    		        response.setStatus(404);
    		        return response;
    		    }

    		    response.setSuccess(true);
    		    response.setMessage("Equipment List Retrieved Successfully");
    		    response.setStatus(200);
    		    response.setData(equipmentList);

    		    return response;
    		}
    				@Override
    				public Response getEquipmentByClinicIdAndBranchId(
    				        String clinicId,
    				        String branchId) {

    				    List<EquipmentDTO> equipmentList = repository
    				            .findByClinicIdAndBranchId(clinicId, branchId)
    				            .stream()
    				            .map(this::convertToDto)
    				            .collect(Collectors.toList());

    				    Response response = new Response();

    				    if (equipmentList.isEmpty()) {
    				        response.setSuccess(false);
    				        response.setMessage("No Equipment Records Found");
    				        response.setStatus(404);
    				        return response;
    				    }

    				    response.setSuccess(true);
    				    response.setMessage("Equipment List Retrieved Successfully");
    				    response.setStatus(200);
    				    response.setData(equipmentList);

    				    return response;
    				}
    			


 @Override
 public Response updateEquipment(
    				        String equipmentId,
    				        EquipmentDTO dto) {

    				    Equipment existing = repository.findById(equipmentId).orElse(null);

    				    Response response = new Response();

    				    if (existing == null) {
    				        response.setSuccess(false);
    				        response.setMessage("Equipment Not Found");
    				        response.setStatus(404);
    				        return response;
    				    }

    				    existing.setClinicId(dto.getClinicId());
    				    existing.setBranchId(dto.getBranchId());
    				    existing.setName(dto.getName());
    				    existing.setCategory(dto.getCategory());
    				    existing.setType(dto.getType());
    				    existing.setBrand(dto.getBrand());
    				    existing.setModel(dto.getModel());
    				    existing.setSerialNo(dto.getSerialNo());
    				    existing.setStatus(dto.getStatus());
    				    existing.setDepartment(dto.getDepartment());

    				    existing.setPurchaseDate(dto.getPurchaseDate());
    				    existing.setWarrantyExpiry(dto.getWarrantyExpiry());
    				    existing.setAmcStartDate(dto.getAmcStartDate());
    				    existing.setAmcEndDate(dto.getAmcEndDate());

    				    existing.setPurchaseCost(dto.getPurchaseCost());
    				    existing.setCurrentValue(dto.getCurrentValue());

    				    existing.setNextServiceDate(dto.getNextServiceDate());
    				    existing.setLastServiceDate(dto.getLastServiceDate());

    				    existing.setAssignedStaff(dto.getAssignedStaff());

    				    if (dto.getImageUrl() != null
    				            && !dto.getImageUrl().isBlank()) {
    				        existing.setImageUrl(dto.getImageUrl());
    				    }

    				    existing.setNotes(dto.getNotes());
    				    existing.setVendorDetails(dto.getVendorDetails());

    				    Equipment updated = repository.save(existing);

    				    response.setSuccess(true);
    				    response.setMessage("Equipment Updated Successfully");
    				    response.setStatus(200);
    				    response.setData(convertToDto(updated));

    				    return response;
    				}

   
    @Override
    public Response deleteEquipment(String equipmentId) {

        Equipment equipment = repository.findById(equipmentId).orElse(null);

        if (equipment == null) {

            Response response = new Response();
            response.setSuccess(false);
            response.setMessage("Equipment Not Found");
            response.setStatus(404);

            return response;
        }

        repository.delete(equipment);

        Response response = new Response();
        response.setSuccess(true);
        response.setMessage("Equipment Deleted Successfully");
        response.setStatus(200);
        response.setData(equipmentId);

        return response;
    }
 


    // ===========================
    // Convert DTO -> Entity
    // ===========================

    private Equipment convertToEntity(
            EquipmentDTO dto) {

        Equipment equipment = new Equipment();

        equipment.setEquipmentId(dto.getEquipmentId());
        equipment.setClinicId(dto.getClinicId());
        equipment.setBranchId(dto.getBranchId());
        equipment.setName(dto.getName());
        equipment.setCategory(dto.getCategory());
        equipment.setType(dto.getType());
        equipment.setBrand(dto.getBrand());
        equipment.setModel(dto.getModel());
        equipment.setSerialNo(dto.getSerialNo());
        equipment.setStatus(dto.getStatus());
        equipment.setDepartment(dto.getDepartment());

        equipment.setPurchaseDate(dto.getPurchaseDate());
        equipment.setWarrantyExpiry(dto.getWarrantyExpiry());
        equipment.setAmcStartDate(dto.getAmcStartDate());
        equipment.setAmcEndDate(dto.getAmcEndDate());

        equipment.setPurchaseCost(dto.getPurchaseCost());
        equipment.setCurrentValue(dto.getCurrentValue());

        equipment.setNextServiceDate(dto.getNextServiceDate());
        equipment.setLastServiceDate(dto.getLastServiceDate());

        equipment.setAssignedStaff(dto.getAssignedStaff());
        equipment.setImageUrl(dto.getImageUrl());
        equipment.setNotes(dto.getNotes());

        equipment.setVendorDetails(dto.getVendorDetails());

        return equipment;
    }

    // ===========================
    // Convert Entity -> DTO
    // ===========================

    private EquipmentDTO convertToDto(
            Equipment equipment) {

        EquipmentDTO dto = new EquipmentDTO();

        dto.setEquipmentId(equipment.getEquipmentId());
        dto.setClinicId(equipment.getClinicId());
        dto.setBranchId(equipment.getBranchId());
        dto.setName(equipment.getName());
        dto.setCategory(equipment.getCategory());
        dto.setType(equipment.getType());
        dto.setBrand(equipment.getBrand());
        dto.setModel(equipment.getModel());
        dto.setSerialNo(equipment.getSerialNo());
        dto.setStatus(equipment.getStatus());
        dto.setDepartment(equipment.getDepartment());

        dto.setPurchaseDate(equipment.getPurchaseDate());
        dto.setWarrantyExpiry(equipment.getWarrantyExpiry());
        dto.setAmcStartDate(equipment.getAmcStartDate());
        dto.setAmcEndDate(equipment.getAmcEndDate());

        dto.setPurchaseCost(equipment.getPurchaseCost());
        dto.setCurrentValue(equipment.getCurrentValue());

        dto.setNextServiceDate(equipment.getNextServiceDate());
        dto.setLastServiceDate(equipment.getLastServiceDate());

        dto.setAssignedStaff(equipment.getAssignedStaff());

        dto.setNotes(equipment.getNotes());

        dto.setVendorDetails(equipment.getVendorDetails());

        // Generate signed URL
        if (equipment.getImageUrl() != null
                && !equipment.getImageUrl().isBlank()) {

            dto.setImageUrl(
                    s3Service.generateSignedUrl(
                            equipment.getImageUrl()));
        }

        dto.setNotes(equipment.getNotes());

        dto.setVendorDetails(equipment.getVendorDetails());

        return dto;
    
    }

private String generateEquipmentId() {
    return "EQU-" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 10)
            .toUpperCase();
}
}