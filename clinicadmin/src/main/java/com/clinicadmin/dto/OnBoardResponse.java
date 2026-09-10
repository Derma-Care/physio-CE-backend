package com.clinicadmin.dto;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnBoardResponse {

    private String message;
    private HttpStatus httpStatus;
    private int statusCode;
    private String role;
    
    private Map<String, Map<String, List<String>>> permissions;
}
