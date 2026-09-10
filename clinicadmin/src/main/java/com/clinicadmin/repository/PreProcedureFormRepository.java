//package com.clinicadmin.repository;
//
//import java.util.Optional;
//
//import org.bson.types.ObjectId;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import com.clinicadmin.entity.PreProcedureForm;
//
//@Repository
//public interface PreProcedureFormRepository extends MongoRepository<PreProcedureForm, ObjectId> {
//	Optional<PreProcedureForm> findByHospitalIdAndId(String hospitalId,ObjectId Id);
//
//}
