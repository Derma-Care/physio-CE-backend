package com.clinicadmin.service;

import com.clinicadmin.dto.EquipmentDTO;
import com.clinicadmin.dto.Response;

public interface EquipmentService {

    Response createEquipment(EquipmentDTO dto);

    Response getEquipmentById(String equipmentId);

    Response getAllEquipment();

    Response updateEquipment(String equipmentId, EquipmentDTO dto);

    Response deleteEquipment(String equipmentId);
    Response getEquipmentByClinicIdAndBranchId( String clinicId, String branchId);
}