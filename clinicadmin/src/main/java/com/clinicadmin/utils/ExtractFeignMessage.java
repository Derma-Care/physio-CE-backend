package com.clinicadmin.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;

public class ExtractFeignMessage {
	
	public static String clearMessage(FeignException e) {
	
	 String errorMessage = "An unexpected error occurred";
	    try {
	        String responseBody = e.contentUTF8();  // Get clean response body

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(responseBody);

	        if (root.has("message")) {
	            errorMessage = root.get("message").asText();
	            return errorMessage;
	        }else {
	        	return "Message Not Found";
	        }
	    } catch (Exception parseEx) {
	        return parseEx.getMessage();
	    }
	}

}
