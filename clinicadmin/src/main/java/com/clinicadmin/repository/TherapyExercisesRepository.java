package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.TherapyExercises;

public interface TherapyExercisesRepository extends MongoRepository<TherapyExercises, String> {

    List<TherapyExercises> findByClinicId(String clinicId);

    List<TherapyExercises> findByBranchId(String branchId);

    List<TherapyExercises> findByClinicIdAndBranchId(String clinicId, String branchId);

    Optional<TherapyExercises> findByTherapyExercisesId(String therapyExercisesId);

    List<TherapyExercises> findByTherapyExercisesId(List<String> therapyExerciseIds);

    boolean existsByTherapyExercisesId(String therapyExercisesId);

    void deleteByTherapyExercisesId(String therapyExercisesId);

    Optional<TherapyExercises> findByClinicIdAndBranchIdAndTherapyExercisesId(
            String clinicId,
            String branchId,
            String therapyExercisesId);

    List<TherapyExercises> findByTherapyExercisesIdInAndClinicIdAndBranchId(
            List<String> therapyExerciseIds,
            String clinicId,
            String branchId);
}