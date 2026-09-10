package com.clinicadmin.utils;



import com.clinicadmin.dto.DoctorSlotDTO;
import com.clinicadmin.entity.DoctorSlot;

public class DoctorSlotMapper {

    public static DoctorSlotDTO doctorSlotEntitytoDTO(DoctorSlot slot) {
        DoctorSlotDTO dto = new DoctorSlotDTO();
        dto.setDoctorId(slot.getDoctorId());
        dto.setHospitalId(slot.getHospitalId());
        dto.setDate(slot.getDate());
        dto.setAvailableSlots(slot.getAvailableSlots());
        return dto;
    }

    public static DoctorSlot  doctorSlotDTOtoEntity(DoctorSlotDTO dto) {
        DoctorSlot entity = new DoctorSlot();
        entity.setDoctorId(dto.getDoctorId());
        entity.setHospitalId(dto.getHospitalId());
        entity.setDate(dto.getDate());
        entity.setAvailableSlots(dto.getAvailableSlots());
        return entity;
    }
}