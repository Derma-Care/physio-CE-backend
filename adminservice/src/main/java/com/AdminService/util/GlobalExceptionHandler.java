package com.AdminService.util;



import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
     Map<String, Object> errorResponse = new HashMap<>();
     Map<String, String> fieldErrors = new HashMap<>();

     ex.getBindingResult().getFieldErrors().forEach(error -> {
         fieldErrors.put(error.getField(), error.getDefaultMessage());
     });

     errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
     errorResponse.put("message", "Validation Failed");
     errorResponse.put("Data", fieldErrors);

     return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
 }
}
