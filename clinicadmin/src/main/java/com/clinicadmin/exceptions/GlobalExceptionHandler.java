package com.clinicadmin.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.clinicadmin.dto.Response;
import com.clinicadmin.utils.ExtractFeignMessage;

import feign.FeignException.FeignClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response> handleResourceNotFound(ResourceNotFoundException ex) {
        Response response = new Response();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

//    // Optional: catch other unhandled exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Response> handleGenericException(Exception ex) {
//        Response response = new Response();
//        response.setSuccess(false);
//        response.setMessage("Internal server error occurred ");
//        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        response.setData(null);
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        Response response = new Response();
        response.setSuccess(false);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Validation failed");
        response.setData(errors);

        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<Response> handleGenericException(FeignClientException ex) {
    	Response res = new Response();
    	res.setStatus(ex.status());
      	res.setMessage(ExtractFeignMessage.clearMessage(ex));
      	res.setSuccess(false);   
       return ResponseEntity
                .status(ex.status())
                .body(res);
    }
}
