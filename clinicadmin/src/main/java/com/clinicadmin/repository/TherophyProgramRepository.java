package com.clinicadmin.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.clinicadmin.entity.TherophyProgramEntity;

public interface TherophyProgramRepository extends MongoRepository<TherophyProgramEntity, String> {

	TherophyProgramEntity findByClinicIdAndBranchIdAndId(String cid,String bid,String id);

List<TherophyProgramEntity> findByClinicIdAndBranchId(String cid, String bid);

List<TherophyProgramEntity> findByIdIn(List<String> programIds);
List<TherophyProgramEntity> findByClinicId(String clinicId);

	}