package com.clinicadmin.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.clinicadmin.dto.ExpenseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Expense;
import com.clinicadmin.repository.ExpenseRepository;
import com.clinicadmin.service.ClinicExpenseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClinicExpenseServiceImpl implements ClinicExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public ResponseEntity<Response> saveExpense(ExpenseDTO expense) {

        Response response = new Response();

        try {
        	ObjectMapper mapper = new ObjectMapper();
    		mapper.registerModule(new JavaTimeModule());
    		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);    		
        	Expense expenseEntity = mapper.convertValue(expense, Expense.class);      	
            Expense savedExpense = expenseRepository.save(expenseEntity);
            response.setSuccess(true);
            response.setData(savedExpense);
            response.setMessage("Expense saved successfully");
            response.setStatus(HttpStatus.CREATED.value());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
           System.out.println(e.getMessage());
            response.setSuccess(false);
            response.setMessage("Failed to save expense");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<Response> getExpenseById(String id) {

        Response response = new Response();

        try {

        	Expense expense = expenseRepository.findById(id)
        	        .orElseThrow(() -> new RuntimeException("Expense not found"));

        	ObjectMapper mapper = new ObjectMapper();
    		mapper.registerModule(new JavaTimeModule());
    		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);    		
        	
        	ExpenseDTO expenseDTO = mapper.convertValue(expense, ExpenseDTO.class);

        	response.setSuccess(true);
        	response.setData(expenseDTO);
        	response.setMessage("Expense retrieved successfully");
        	response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Override
    public ResponseEntity<?> getExpensesByClinicAndBranch(
            String clinicId, String branchId) {

        Response response = new Response();

        try {

            List<Expense> expenses =
                    expenseRepository.findByClinicIdAndBranchId(
                            clinicId,
                            branchId);
            ObjectMapper mapper = new ObjectMapper();
    		mapper.registerModule(new JavaTimeModule());
    		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);    		
        	
            List<ExpenseDTO> expenseDTOs =
            		mapper.convertValue(
                            expenses,
                            new TypeReference<List<ExpenseDTO>>() {}); 
            Map<String,Object> map = new LinkedHashMap<>();

            map.put("total", expenses.stream().map(n->n.getAmount()).filter(Objects::nonNull)
            		.mapToDouble(n->n.doubleValue()).sum());
            map.put("success", true);
            map.put("data", expenseDTOs);
            map.put("message", "Expenses retrieved successfully");
            map.put("status", HttpStatus.OK.value());            
            return ResponseEntity.status(200).body(map);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to retrieve expenses");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Override
    public ResponseEntity<Response> updateExpense(String id, ExpenseDTO expense) {

        Response response = new Response();
        try {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Expense not found with id: " + id));

        if (expense.getTitle() != null) {
            existing.setTitle(expense.getTitle());
        }

        if (expense.getCategory() != null) {
            existing.setCategory(expense.getCategory());
        }

        if (expense.getDate() != null) {
            existing.setDate(expense.getDate());
        }

        if (expense.getAmount() != null) {
            existing.setAmount(expense.getAmount());
        }

        if (expense.getMode() != null) {
            existing.setMode(expense.getMode());
        }

        if (expense.getNotes() != null) {
            existing.setNotes(expense.getNotes());
        }

        if (expense.getTransactionId() != null) {
            existing.setTransactionId(expense.getTransactionId());
        }

        if (expense.getClinicId() != null) {
            existing.setClinicId(expense.getClinicId());
        }

        if (expense.getBranchId() != null) {
            existing.setBranchId(expense.getBranchId());
        }

        Expense updatedExpense = expenseRepository.save(existing);

        response.setSuccess(true);
        response.setMessage("Expense updated successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(updatedExpense);

        return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Override
    public ResponseEntity<Response> deleteExpense(String id) {

        Response response = new Response();

        try {

            Expense expense = expenseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Expense not found"));

            expenseRepository.delete(expense);

            response.setSuccess(true);
            response.setMessage("Expense deleted successfully");
            response.setStatus(HttpStatus.OK.value());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

