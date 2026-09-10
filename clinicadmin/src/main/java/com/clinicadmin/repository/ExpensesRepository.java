package com.clinicadmin.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.clinicadmin.entity.ExpensesEntity;

@Repository
public interface ExpensesRepository extends MongoRepository<ExpensesEntity, String> {
	
	List<ExpensesEntity> findByClinicIdAndBranchId(String cid,String bid );
	
	List<ExpensesEntity> findByClinicIdAndBranchIdAndDate(
	        String clinicId, String branchId, LocalDate date);

	List<ExpensesEntity> findByClinicIdAndBranchIdAndDateBetween(
	        String clinicId, String branchId, LocalDate startDate, LocalDate endDate);

	List<ExpensesEntity> findByDateBetween(LocalDate startDate, LocalDate today);

}
