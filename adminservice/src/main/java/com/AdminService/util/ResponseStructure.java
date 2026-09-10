package com.AdminService.util;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResponseStructure<T> {

	private T data;
	private String message;
	private HttpStatus httpStatus;
	private Integer statusCode;

	public static <T> ResponseStructure<T> buildResponse(T data, String message, HttpStatus httpStatus,
			int statusCode) {
		return new ResponseStructure<>(data, message, httpStatus, statusCode);
	}
}
