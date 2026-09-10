package com.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * success / data / message / statusCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {
    private Boolean success;
    private T       data;
    private String  message;
    private Integer statusCode;
}