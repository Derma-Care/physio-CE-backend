package com.clinicadmin.service;

import org.springframework.http.ResponseEntity;
import com.clinicadmin.dto.ExpensesDTO;
import com.clinicadmin.dto.Response;

public interface ExpensesService {
	
	public ResponseEntity<Response> create(ExpensesDTO dto);
	public ResponseEntity<Response> getAll();
	public ResponseEntity<Response> update(String id, ExpensesDTO dto);
	public ResponseEntity<Response> delete(String id);
	public ResponseEntity<Response> getByClinicAndBranch(String clinicId, String branchId);
	public Double getTodayExpenses(String clinicId, String branchId);
	public Double getWeeklyExpenses(String clinicId, String branchId);
	public Double getMonthlyExpenses(String clinicId, String branchId);
	public Double customeFilter(String startDate, String endDate);
		

}
