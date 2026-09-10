package com.dermacare.bookingService.util;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON response
@Builder
public class Response {
	private boolean success;
	private Object data;
	private Map<String,Object> counts;
	private String message;
	private int status;
	private String hospitalName;
	private String hospitalId;
	
}
