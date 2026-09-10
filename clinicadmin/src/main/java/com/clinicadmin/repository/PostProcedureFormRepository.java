//package com.clinicadmin.repository;
//
//import java.util.Optional;
//
//import org.bson.types.ObjectId;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import com.clinicadmin.entity.PostProcedureForm;
//
//@Repository
//public interface PostProcedureFormRepository extends MongoRepository<PostProcedureForm, ObjectId> {
//	Optional<PostProcedureForm> findByHospitalIdAndId(String hospitalId,ObjectId id);
//
//}
