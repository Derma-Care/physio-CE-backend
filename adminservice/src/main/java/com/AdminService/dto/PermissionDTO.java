package com.AdminService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private String id;
    private Map<String, Map<String, List<String>>> permissions;

}