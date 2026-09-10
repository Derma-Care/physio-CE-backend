package com.clinicadmin.service;

import org.springframework.http.ResponseEntity;
import com.clinicadmin.dto.ExpenseDTO;
import com.clinicadmin.dto.Response;


public interface ClinicExpenseService {

    ResponseEntity<Response> saveExpense(ExpenseDTO expense);

    ResponseEntity<Response> updateExpense(String id, ExpenseDTO expense);

    ResponseEntity<Response> getExpenseById(String id);

    ResponseEntity<?> getExpensesByClinicAndBranch(
            String clinicId,
            String branchId);

    ResponseEntity<Response> deleteExpense(String id);
}
