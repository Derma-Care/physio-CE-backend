package com.AdminService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.AdminService.dto.PermissionDTO;
import com.AdminService.service.PermissionService;
import com.AdminService.util.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor

public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/createPermissions")
    public ResponseEntity<Response> create(@RequestBody PermissionDTO dto) {
        Response response = permissionService.create(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/updatePermissions/{id}")
    public ResponseEntity<Response> update(@PathVariable String id,
                                           @RequestBody PermissionDTO dto) {
        Response response = permissionService.update(id, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/getPermissions/{id}")
    public ResponseEntity<Response> get(@PathVariable String id) {
        Response response = permissionService.getById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/getAllPermisssions")
    public ResponseEntity<Response> getAll() {
        Response response = permissionService.getAll();
        return ResponseEntity.status(response.getStatus()).body(response);

        
    }
    
    @GetMapping("/getPermissionsByIdAndPlanId/{id}/{planId}")
    public ResponseEntity<Response> getByPlan(
            @PathVariable String id,
            @PathVariable String planId) {

        Response response =
                permissionService.getByPlanId(id, planId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @PutMapping("/updatePermissionsByIdAndPlaneId/{id}/{planId}")
    public ResponseEntity<Response> updateByPlanId(
            @PathVariable String id,
            @PathVariable String planId,
            @RequestBody PermissionDTO dto) {

        Response response =
                permissionService.updateByPlanId(
                        id,
                        planId,
                        dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    
    @DeleteMapping("/deletePermissionByPlanId/{id}/{planId}")
    public ResponseEntity<Response> deleteByPlanId(
            @PathVariable String id,
            @PathVariable String planId,
            @RequestBody PermissionDTO dto) {

        Response response =
                permissionService.deleteByPlanId(
                        id,
                        planId,
                        dto
                );

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @DeleteMapping("/deletePermissions/{id}")
    public ResponseEntity<Response> delete(@PathVariable String id) {
        Response response = permissionService.delete(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/getPermissionsByPlanId/{planId}")
    public ResponseEntity<Response> getByPlan(
            @PathVariable String planId) {

        Response response =
                permissionService.getByPlanId(planId);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @PutMapping("/updatePermissionsByPlanId/{planId}")
    public ResponseEntity<Response> updateByPlan(
            @PathVariable String planId,
            @RequestBody PermissionDTO dto) {

        Response response =
                permissionService.updateByPlanId(planId, dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}