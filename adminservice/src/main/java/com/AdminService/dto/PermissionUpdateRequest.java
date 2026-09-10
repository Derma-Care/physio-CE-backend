package com.AdminService.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PermissionUpdateRequest {

    private Map<String, List<String>> permissions;

}