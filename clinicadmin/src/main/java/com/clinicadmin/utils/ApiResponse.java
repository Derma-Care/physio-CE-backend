package com.clinicadmin.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private Integer totalRecords;
    private List<FieldError> errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().status("SUCCESS").message(message).data(data).build();
    }

    public static <T> ApiResponse<T> successList(T data, int total) {
        return ApiResponse.<T>builder().status("SUCCESS").totalRecords(total).data(data).build();
    }

    public static <T> ApiResponse<T> successMessage(String message) {
        return ApiResponse.<T>builder().status("SUCCESS").message(message).build();
    }

    public static <T> ApiResponse<T> failed(String message, List<FieldError> errors) {
        return ApiResponse.<T>builder().status("FAILED").message(message).errors(errors).build();
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FieldError {
        private String field;
        private String description;
    }
}
