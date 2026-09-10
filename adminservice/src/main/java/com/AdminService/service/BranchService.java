package com.AdminService.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.AdminService.dto.BranchDTO;
import com.AdminService.util.Response;

public interface BranchService {
	public Response createBranch(BranchDTO branch);
	ResponseEntity<?> getBranchById(String branchId);
	Response updateBranch(String branchId, BranchDTO branch);
	Response deleteBranch(String branchId);
	Response getAllBranches();
	 ResponseEntity<?> getBranchByClinicId(String clinicId);
	 Response getBranchesByClinicId(String clinicId);
	 ResponseEntity<?> getBranchByClinicAndBranchId(String clinicId, String branchId);
//	 public Response startBranchVerification(String branchId);
//	 public Response verifyBranch(String branchId);
//	public Response rejectBranch(String branchId, String reason);
	 Response updatePermissions(String clinicId, String branchId, Map<String, List<String>> permissions);
     
		
}
