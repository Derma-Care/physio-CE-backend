package com.clinicadmin.service.impl;

import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapyExercisesDTO;
import com.clinicadmin.entity.TherapyExercises;
import com.clinicadmin.repository.TherapyExercisesRepository;
import com.clinicadmin.service.TherapyExercisesService;

@Service
public class TherapyExercisesServiceImpl implements TherapyExercisesService {

    @Autowired
    private TherapyExercisesRepository repository;

    // ================= CREATE =================
    @Override
    public ResponseStructure<TherapyExercisesDTO> createTherapyExercises(TherapyExercisesDTO dto) {

        TherapyExercises entity = toEntity(dto);
        entity.setTherapyExercisesId(generateUniqueId());

        TherapyExercises saved = repository.save(entity);

        return ResponseStructure.buildResponse(
                toDTO(saved),
                "Therapy Exercise Created Successfully",
                HttpStatus.CREATED,
                201
        );
    }

    // ================= GET BY ID =================
    @Override
    public ResponseStructure<TherapyExercisesDTO> getTherapyExercisesById(String therapyExercisesId) {

        TherapyExercises entity = repository.findByTherapyExercisesId(therapyExercisesId)
                .orElseThrow(() -> new RuntimeException("Therapy Exercise Not Found"));

        return ResponseStructure.buildResponse(
                toDTO(entity),
                "Fetched Successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= GET BY clinicId + branchId =================
    @Override
    public ResponseStructure<List<TherapyExercisesDTO>> getByClinicIdAndBranchId(
            String clinicId, String branchId) {

        List<TherapyExercisesDTO> list = repository
                .findByClinicIdAndBranchId(clinicId, branchId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseStructure.buildResponse(
                list,
                "Fetched Successfully",
                HttpStatus.OK,
                200
        );
    }
    @Override
    public ResponseStructure<List<TherapyExercisesDTO>> getByClinicId(String clinicId) {

        List<TherapyExercisesDTO> list = repository
                .findByClinicId(clinicId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseStructure.buildResponse(
                list,
                "Fetched Successfully",
                HttpStatus.OK,
                200
        );
    }
 // ================= UPDATE =================
    @Override
    public ResponseStructure<TherapyExercisesDTO> updateTherapyExercisesById(
            String therapyExercisesId, TherapyExercisesDTO dto) {

        TherapyExercises entity = repository.findByTherapyExercisesId(therapyExercisesId)
                .orElseThrow(() -> new RuntimeException("Therapy Exercise Not Found"));

        // Basic fields
        if (dto.getClinicId() != null)
            entity.setClinicId(dto.getClinicId());

        if (dto.getBranchId() != null)
            entity.setBranchId(dto.getBranchId());

        if (dto.getName() != null)
            entity.setName(dto.getName());

        if (dto.getVideo() != null)
            entity.setVideo(dto.getVideo());

        if (dto.getImage() != null)
            entity.setImage(encode(dto.getImage()));

        if (dto.getSession() != null)
            entity.setSession(dto.getSession());

        if (dto.getDuration() != null)
            entity.setDuration(dto.getDuration());

        if (dto.getFrequency() != null)
            entity.setFrequency(dto.getFrequency());

        if (dto.getNotes() != null)
            entity.setNotes(dto.getNotes());

        // Price fields
        if (dto.getPricePerSession() != 0)
            entity.setPricePerSession(dto.getPricePerSession());

        if (dto.getGst() != 0)
            entity.setGst(dto.getGst());

        if (dto.getOtherTax() != 0)
            entity.setOtherTax(dto.getOtherTax());

        if (dto.getDiscountPercentage() != 0)
            entity.setDiscountPercentage(dto.getDiscountPercentage());

        if (dto.getSets() != 0)
            entity.setSets(dto.getSets());

        if (dto.getRepetitions() != 0)
            entity.setRepetitions(dto.getRepetitions());

        // ✅ New Fields
        if (dto.getTechnique() != null)
            entity.setTechnique(dto.getTechnique());

        if (dto.getMachine() != null)
            entity.setMachine(dto.getMachine());

        if (dto.getIntensity() != null)
            entity.setIntensity(dto.getIntensity());

        if (dto.getAssistanceLevel() != null)
            entity.setAssistanceLevel(dto.getAssistanceLevel());

        if (dto.getType() != null)
            entity.setType(dto.getType());

        if (dto.getArea() != null)
            entity.setArea(dto.getArea());

        if (dto.getMetric() != null)
            entity.setMetric(dto.getMetric());

        if (dto.getValue() != null)
            entity.setValue(dto.getValue());

        if (dto.getUnit() != null)
            entity.setUnit(dto.getUnit());
        
        if (dto.getBodyPart() != null)
            entity.setBodyPart(dto.getBodyPart());

        if (dto.getActivityType() != null)
            entity.setActivityType(dto.getActivityType());

        if (dto.getActivityDuration() != null)
            entity.setActivityDuration(dto.getActivityDuration());

        // ================= CALCULATION =================
        double base = entity.getPricePerSession();

        double discountAmount = base * entity.getDiscountPercentage() / 100;
        double discountedPrice = base - discountAmount;

        double gstAmount = discountedPrice * entity.getGst() / 100;
        double otherTaxAmount = discountedPrice * entity.getOtherTax() / 100;

        double finalTotal = discountedPrice + gstAmount + otherTaxAmount;

        entity.setDiscountAmount(discountAmount);
        entity.setTotalPrice((int) finalTotal);

        // ================= SAVE =================
        TherapyExercises updated = repository.save(entity);

        return ResponseStructure.buildResponse(
                toDTO(updated),
                "Updated Successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= GET BY clinicId + branchId + therapyExercisesId =================
    @Override
    public ResponseStructure<TherapyExercisesDTO> getByClinicIdBranchIdAndTherapyId(
            String clinicId, String branchId, String therapyExercisesId) {

        TherapyExercises entity = repository
                .findByClinicIdAndBranchIdAndTherapyExercisesId(
                        clinicId, branchId, therapyExercisesId)
                .orElseThrow(() -> new RuntimeException("Therapy Exercise Not Found"));

        return ResponseStructure.buildResponse(
                toDTO(entity),
                "Fetched Successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= DELETE =================
    @Override
    public ResponseStructure<String> deleteTherapyExercisesById(String therapyExercisesId) {

        TherapyExercises entity = repository.findByTherapyExercisesId(therapyExercisesId)
                .orElseThrow(() -> new RuntimeException("Therapy Exercise Not Found"));

        repository.delete(entity);

        return ResponseStructure.buildResponse(
                "Deleted Successfully",
                "Deleted",
                HttpStatus.OK,
                200
        );
    }

    // ================= ID GENERATION =================
    private String generateCustomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "THER-" + sb.toString();
    }

    private String generateUniqueId() {
        String id;
        do {
            id = generateCustomId();
        } while (repository.existsByTherapyExercisesId(id));
        return id;
    }

    // ================= DTO → ENTITY =================
    private TherapyExercises toEntity(TherapyExercisesDTO dto) {
        TherapyExercises e = new TherapyExercises();

        e.setClinicId(dto.getClinicId());
        e.setBranchId(dto.getBranchId());
        e.setName(dto.getName());
        e.setVideo(dto.getVideo());

       
        e.setImage(encode(dto.getImage()));

        e.setSession(dto.getSession());
        e.setDuration(dto.getDuration());
        e.setFrequency(dto.getFrequency());
        e.setNotes(dto.getNotes());
        e.setPricePerSession(dto.getPricePerSession());
        e.setGst(dto.getGst());
        e.setOtherTax(dto.getOtherTax());
        e.setSets(dto.getSets());
        e.setRepetitions(dto.getRepetitions());
        e.setTechnique(dto.getTechnique());
        e.setMachine(dto.getMachine());
        e.setIntensity(dto.getIntensity());
        e.setAssistanceLevel(dto.getAssistanceLevel());
        e.setType(dto.getType());
        e.setArea(dto.getArea());
        e.setMetric(dto.getMetric());
        e.setValue(dto.getValue());
        e.setUnit(dto.getUnit());
        e.setActivityType(dto.getActivityType());
        e.setActivityDuration(dto.getActivityDuration());
        e.setBodyPart(dto.getBodyPart());
        e.setDiscountAmount(dto.getDiscountAmount());
        e.setDiscountPercentage(dto.getDiscountPercentage());

     // ✅ TOTAL PRICE (Per Session Only)
        double base = dto.getPricePerSession();

     // ✅ Discount first
     double discountAmount = base * dto.getDiscountPercentage() / 100;
     double discountedPrice = base - discountAmount;

     // ✅ Taxes after discount
     double gstAmount = discountedPrice * dto.getGst() / 100;
     double otherTaxAmount = discountedPrice * dto.getOtherTax() / 100;

     // ✅ Final total
     double finalTotal = discountedPrice + gstAmount + otherTaxAmount;

     // Set values
     e.setDiscountPercentage(dto.getDiscountPercentage());
     e.setDiscountAmount(discountAmount);
     e.setTotalPrice((int) finalTotal);
     
     return e;
     
    }
    // ================= ENTITY → DTO =================
    private TherapyExercisesDTO toDTO(TherapyExercises e) {
        TherapyExercisesDTO dto = new TherapyExercisesDTO();

        dto.setTherapyExercisesId(e.getTherapyExercisesId());
        dto.setClinicId(e.getClinicId());
        dto.setBranchId(e.getBranchId());
        dto.setName(e.getName());

        dto.setVideo(e.getVideo());
      dto.setImage(decode(e.getImage()));

        dto.setSession(e.getSession());
        dto.setDuration(e.getDuration());
        dto.setFrequency(e.getFrequency());
        dto.setNotes(e.getNotes());
      
        // ✅ NEW FIELDS
        dto.setPricePerSession(e.getPricePerSession());
        dto.setGst(e.getGst());
        dto.setOtherTax(e.getOtherTax());
        dto.setSets(e.getSets());
        dto.setRepetitions(e.getRepetitions());
        dto.setTechnique(e.getTechnique());
        dto.setMachine(e.getMachine());
        dto.setIntensity(e.getIntensity());
        dto.setAssistanceLevel(e.getAssistanceLevel());
        dto.setType(e.getType());
        dto.setArea(e.getArea());
        dto.setMetric(e.getMetric());
        dto.setValue(e.getValue());
        dto.setUnit(e.getUnit());
        dto.setActivityType(e.getActivityType());
        dto.setActivityDuration(e.getActivityDuration());
        dto.setBodyPart(e.getBodyPart());
        dto.setTotalPrice(e.getTotalPrice());
        dto.setDiscountAmount(e.getDiscountAmount());
        dto.setDiscountPercentage(e.getDiscountPercentage());

        return dto;
    }

    // ================= ENCODE / DECODE =================
    private String encode(String value) {
        if (value == null) return null;
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    private String decode(String value) {
        if (value == null) return null;
        return new String(Base64.getDecoder().decode(value));
    }
}