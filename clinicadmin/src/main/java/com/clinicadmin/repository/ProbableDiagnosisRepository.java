//package com.clinicadmin.repository;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.bson.types.ObjectId;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import com.clinicadmin.entity.ProbableDiagnosis;
//
//@Repository
//public interface ProbableDiagnosisRepository extends MongoRepository<ProbableDiagnosis, ObjectId> {
//	public Optional<ProbableDiagnosis> findByIdAndHospitalId(ObjectId id, String hospitalId);
//
//	List<ProbableDiagnosis> findByHospitalId(String hospitalId);
//}
