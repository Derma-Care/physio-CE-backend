package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clinicadmin.dto.ExpensesDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.ExpensesService;

@RestController
@RequestMapping("/clinic-admin/expenses")
public class ExpensesController {
	
	@Autowired
	private ExpensesService service;

	 @PostMapping("/create")
	    public ResponseEntity<Response> create(@RequestBody ExpensesDTO dto) {
	        return service.create(dto);
	    }

	    // ✅ GET ALL
	    @GetMapping("/getAll")
	    public ResponseEntity<Response> getAll() {
	        return service.getAll();
	    }

	    // ✅ UPDATE
	    @PutMapping("/update/{id}")
	    public ResponseEntity<Response> update(@PathVariable String id,
	                                           @RequestBody ExpensesDTO dto) {
	        return service.update(id, dto);
	    }

	    // ✅ DELETE
	    @DeleteMapping("/delete/{id}")
	    public ResponseEntity<Response> delete(@PathVariable String id) {
	        return service.delete(id);
	    }

	    // ✅ GET BY CLINIC + BRANCH
	    @GetMapping("/filter/{clinicId}/{branchId}")
	    public ResponseEntity<Response> getByClinicAndBranch(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId) {

	        return service.getByClinicAndBranch(clinicId, branchId);
	    }

	    // ✅ TODAY EXPENSES
	    @GetMapping("/today/{clinicId}/{branchId}")
	    public Double getTodayExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId) {

	        return service.getTodayExpenses(clinicId, branchId);
	    }

	    // ✅ WEEKLY EXPENSES
	    @GetMapping("/weekly/{clinicId}/{branchId}")
	    public Double getWeeklyExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId) {

	        return service.getWeeklyExpenses(clinicId, branchId);
	    }

	    // ✅ MONTHLY EXPENSES
	    @GetMapping("/monthly/{clinicId}/{branchId}")
	    public Double getMonthlyExpenses(
	    		@PathVariable String clinicId,
	    		@PathVariable String branchId) {

	        return service.getMonthlyExpenses(clinicId, branchId);
	    }
	    
	    
	    @GetMapping("/custom/{startDate}/{endDate}")
	    public Double customFilter(
	    		@PathVariable String startDate,
	    		@PathVariable String endDate) {

	        return service.customeFilter(startDate, endDate);
	    }
	    
}
