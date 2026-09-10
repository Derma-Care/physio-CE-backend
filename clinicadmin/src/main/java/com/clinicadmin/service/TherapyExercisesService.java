package com.clinicadmin.service;

import java.util.List;

import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapyExercisesDTO;

public interface TherapyExercisesService {

    // CREATE
    ResponseStructure<TherapyExercisesDTO> createTherapyExercises(TherapyExercisesDTO dto);

    // UPDATE
    ResponseStructure<TherapyExercisesDTO> updateTherapyExercisesById(
            String therapyExercisesId, TherapyExercisesDTO dto);

    // GET BY therapyExercisesId
    ResponseStructure<TherapyExercisesDTO> getTherapyExercisesById(
            String therapyExercisesId);

    // ✅ GET BY clinicId + branchId
    ResponseStructure<List<TherapyExercisesDTO>> getByClinicIdAndBranchId(
            String clinicId, String branchId);

    // ✅ GET BY clinicId + branchId + therapyExercisesId
    ResponseStructure<TherapyExercisesDTO> getByClinicIdBranchIdAndTherapyId(
            String clinicId, String branchId, String therapyExercisesId);

    // DELETE
    ResponseStructure<String> deleteTherapyExercisesById(
            String therapyExercisesId);
    ResponseStructure<List<TherapyExercisesDTO>> getByClinicId(String clinicId);
}