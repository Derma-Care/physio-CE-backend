package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.ExpenseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Expense;
import com.clinicadmin.service.ClinicExpenseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class ClinicExpenseController {

    private final ClinicExpenseService expenseService;

    // Create Expense
    @PostMapping("/clinic/expenses/create")
    public ResponseEntity<Response> createExpense(
            @RequestBody ExpenseDTO expense) {

        return expenseService.saveExpense(expense);
    }

    // Update Expense By Id
    @PutMapping("/expenses/update/id/{id}")
    public ResponseEntity<Response> updateExpense(
            @PathVariable String id,
            @RequestBody ExpenseDTO expense) {

        return expenseService.updateExpense(id, expense);
    }

    // Get Expense By Id
    @GetMapping("/expenses/id/{id}")
    public ResponseEntity<Response> getExpenseById(
            @PathVariable String id) {

        return expenseService.getExpenseById(id);
    }

    // Get Expenses By ClinicId And BranchId
    @GetMapping("/expenses/clinic/{clinicId}/branch/{branchId}")
    public ResponseEntity<?> getExpensesByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return expenseService.getExpensesByClinicAndBranch(
                clinicId,
                branchId);
    }

    // Delete Expense
    @DeleteMapping("/expenses/delete/id/{id}")
    public ResponseEntity<Response> deleteExpense(
            @PathVariable String id) {

        return expenseService.deleteExpense(id);
    }
}