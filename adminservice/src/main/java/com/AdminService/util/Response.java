package com.AdminService.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Response {
    
    private boolean success;
    private Object data;
    private String message;
    private int status;

    private String hospitalName;
    private String hospitalId; 
    private String branchId;

    private String loginType;
    private boolean mainBranch;
    private String branchName;

    private String role; 
    private Map<String, List<String>> permissions;
    private List<String> fcmTokens;
	
		

		

}
