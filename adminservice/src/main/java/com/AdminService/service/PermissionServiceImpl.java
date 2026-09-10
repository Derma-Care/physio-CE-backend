package com.AdminService.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.AdminService.dto.PermissionDTO;
import com.AdminService.entity.Permission;
import com.AdminService.repository.PermissionRepository;
import com.AdminService.util.Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public Response create(PermissionDTO dto) {

        Response response = new Response();

        try {

            Optional<Permission> existing = permissionRepository.findTopByOrderByIdAsc();

            if (existing.isPresent()) {

                Permission oldPermission = existing.get();

                // Compare permissions
                if (oldPermission.getPermissions().equals(dto.getPermissions())) {
                    response.setSuccess(false);
                    response.setMessage("Permissions already exist.");
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    return response;
                }
            }

            // Create NEW document if permissions changed
            Permission permission = convertToEntity(dto);
            Permission savedPermission = permissionRepository.save(permission);

            response.setSuccess(true);
            response.setData(convertToDTO(savedPermission));
            response.setMessage("Permissions created successfully.");
            response.setStatus(HttpStatus.CREATED.value());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to create permissions : " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
    @Override
    public Response update(String id, PermissionDTO dto) {

        Response response = new Response();

        try {

            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Permissions not found."));

            Map<String, Map<String, List<String>>> existingPermissions =
                    permission.getPermissions();

            if (existingPermissions == null) {
                existingPermissions = new LinkedHashMap<>();
            }

            Map<String, Map<String, List<String>>> incomingPermissions =
                    dto.getPermissions();

            // Loop through plans
            for (Map.Entry<String, Map<String, List<String>>> planEntry
                    : incomingPermissions.entrySet()) {

                String planId = planEntry.getKey();

                Map<String, List<String>> incomingModules =
                        planEntry.getValue();

                // Get existing plan
                Map<String, List<String>> existingModules =
                        existingPermissions.get(planId);

                // If plan doesn't exist, create it
                if (existingModules == null) {

                    existingModules = new LinkedHashMap<>();

                    existingPermissions.put(
                            planId,
                            existingModules
                    );
                }

                // Add or update modules
                for (Map.Entry<String, List<String>> moduleEntry
                        : incomingModules.entrySet()) {

                    String moduleName = moduleEntry.getKey();

                    List<String> newPermissions =
                            moduleEntry.getValue();

                    // If module exists -> override
                    // If module doesn't exist -> add
                    existingModules.put(
                            moduleName,
                            newPermissions
                    );
                }
            }

            // Save updated permissions
            permission.setPermissions(existingPermissions);

            Permission updatedPermission =
                    permissionRepository.save(permission);

            response.setSuccess(true);
            response.setData(convertToDTO(updatedPermission));
            response.setMessage(
                    "Permissions updated successfully."
            );
            response.setStatus(
                    HttpStatus.OK.value()
            );

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(
                    "Failed to update permissions : "
                            + e.getMessage()
            );
            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }

    @Override
    public Response getById(String id) {

        Response response = new Response();

        try {

            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Permissions not found."));

            response.setSuccess(true);
            response.setData(convertToDTO(permission));
            response.setMessage("Permissions fetched successfully.");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to fetch permissions : " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
    @Override
    public Response getAll() {

        Response response = new Response();

        try {

            Permission permission = permissionRepository
                    .findTopByOrderByIdAsc()
                    .orElseThrow(() ->
                            new RuntimeException("Permissions not found."));

            // Prepare response with ID and permissions
            Map<String, Object> responseData =
                    new LinkedHashMap<>();

            responseData.put(
                    "id",
                    permission.getId()
            );

            responseData.put(
                    "permissions",
                    permission.getPermissions()
            );

            response.setSuccess(true);
            response.setData(responseData);
            response.setMessage("Permissions fetched successfully.");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    "Failed to fetch permissions : "
                            + e.getMessage()
            );

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }
    @Override
    public Response delete(String id) {

        Response response = new Response();

        try {

            Permission permission = permissionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Permissions not found."));

            permissionRepository.delete(permission);

            response.setSuccess(true);
            response.setMessage("Permissions deleted successfully.");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage("Failed to delete permissions : " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    // ===========================
    // DTO -> Entity
    // ===========================
    private Permission convertToEntity(PermissionDTO dto) {

        Permission entity = new Permission();

        entity.setId(dto.getId());
       
        entity.setPermissions(dto.getPermissions());

        return entity;
    }

    // ===========================
    // Entity -> DTO
    // ===========================
    private PermissionDTO convertToDTO(Permission entity) {

        PermissionDTO dto = new PermissionDTO();

        dto.setId(entity.getId());
 
        dto.setPermissions(entity.getPermissions());

        return dto;
    }
    
    @Override
    public Response getByPlanId(
            String id,
            String planId) {

        Response response = new Response();

        try {

            // Convert plan ID to plan name
            String planName;

            switch (planId) {

                case "1":
                    planName = "Basic";
                    break;

                case "2":
                    planName = "Pro";
                    break;

                case "3":
                    planName = "Elite";
                    break;

                case "4":
                    planName = "Enterprise";
                    break;

                default:

                    response.setSuccess(false);
                    response.setMessage(
                            "Invalid plan ID. Use 1=Basic, 2=Pro, 3=Elite, 4=Enterprise."
                    );
                    response.setStatus(
                            HttpStatus.BAD_REQUEST.value()
                    );

                    return response;
            }

            // Get permission document using ID
            Permission permission = permissionRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Permissions not found for id: " + id
                            )
                    );

            // Get all permissions
            Map<String, Map<String, List<String>>> allPermissions =
                    permission.getPermissions();

            // Get permissions for selected plan
            Map<String, List<String>> planPermissions =
                    allPermissions.get(planName);

            // Plan not found
            if (planPermissions == null) {

                response.setSuccess(false);
                response.setMessage(
                        "Permissions not found for plan: " + planName
                );
                response.setStatus(
                        HttpStatus.NOT_FOUND.value()
                );

                return response;
            }

            // Prepare response
            Map<String, Object> planData =
                    new LinkedHashMap<>();

            planData.put("id", permission.getId());
            planData.put("planId", planId);
            planData.put("planName", planName);
            planData.put("permissions", planPermissions);

            response.setSuccess(true);
            response.setData(planData);
            response.setMessage(
                    "Permissions fetched successfully for plan: "
                            + planName
            );
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(
                    "Failed to fetch permissions : "
                            + e.getMessage()
            );
            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }
    
    @Override
    public Response getByPlanId(String planId) {

        Response response = new Response();

        try {

            // ==============================
            // Convert plan ID to plan name
            // ==============================

            String planName;

            switch (planId) {

                case "1":
                    planName = "Basic";
                    break;

                case "2":
                    planName = "Pro";
                    break;

                case "3":
                    planName = "Elite";
                    break;

                case "4":
                    planName = "Enterprise";
                    break;

                default:

                    response.setSuccess(false);
                    response.setMessage(
                            "Invalid plan ID. Use 1=Basic, 2=Pro, 3=Elite, 4=Enterprise."
                    );
                    response.setStatus(HttpStatus.BAD_REQUEST.value());

                    return response;
            }

            // ==============================
            // Get permissions document
            // ==============================

            Permission permission = permissionRepository
                    .findTopByOrderByIdAsc()
                    .orElseThrow(() ->
                            new RuntimeException("Permissions not found.")
                    );

            Map<String, Map<String, List<String>>> allPermissions =
                    permission.getPermissions();

            // ==============================
            // Get selected plan permissions
            // ==============================

            Map<String, List<String>> planPermissions =
                    allPermissions.get(planName);

            if (planPermissions == null) {

                response.setSuccess(false);
                response.setMessage(
                        "Permissions not found for plan: " + planName
                );
                response.setStatus(HttpStatus.NOT_FOUND.value());

                return response;
            }

            // ==============================
            // Prepare response
            // ==============================

            Map<String, Object> planData =
                    new LinkedHashMap<>();

            planData.put("id", permission.getId());
            planData.put("planId", planId);
            planData.put("planName", planName);
            planData.put("permissions", planPermissions);

            response.setSuccess(true);
            response.setData(planData);

            response.setMessage(
                    "Permissions fetched successfully for plan: "
                            + planName
            );

            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    "Failed to fetch permissions : "
                            + e.getMessage()
            );

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }
    
    @Override
    public Response updateByPlanId(
            String planId,
            PermissionDTO dto) {

        Response response = new Response();

        try {

            // ==============================
            // 1. Convert plan ID to plan name
            // ==============================

            String planName;

            switch (planId) {

                case "1":
                    planName = "Basic";
                    break;

                case "2":
                    planName = "Pro";
                    break;

                case "3":
                    planName = "Elite";
                    break;

                case "4":
                    planName = "Enterprise";
                    break;

                default:

                    response.setSuccess(false);
                    response.setMessage(
                            "Invalid plan ID. Use 1=Basic, 2=Pro, 3=Elite, 4=Enterprise."
                    );
                    response.setStatus(HttpStatus.BAD_REQUEST.value());

                    return response;
            }

            // ==============================
            // 2. Find the permissions document
            // ==============================

            Permission permission = permissionRepository
                    .findTopByOrderByIdAsc()
                    .orElseThrow(() ->
                            new RuntimeException("Permissions not found.")
                    );

            // ==============================
            // 3. Get existing permissions
            // ==============================

            Map<String, Map<String, List<String>>> allPermissions =
                    permission.getPermissions();

            if (allPermissions == null) {
                allPermissions = new LinkedHashMap<>();
            }

            // ==============================
            // 4. Get/Create selected plan
            // ==============================

            Map<String, List<String>> existingPlanPermissions =
                    allPermissions.get(planName);

            if (existingPlanPermissions == null) {

                existingPlanPermissions = new LinkedHashMap<>();

                allPermissions.put(
                        planName,
                        existingPlanPermissions
                );
            }

            // ==============================
            // 5. Validate incoming permissions
            // ==============================

            Map<String, Map<String, List<String>>> incomingPermissions =
                    dto.getPermissions();

            if (incomingPermissions == null
                    || incomingPermissions.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Permissions are required.");
                response.setStatus(HttpStatus.BAD_REQUEST.value());

                return response;
            }

            // ==============================
            // 6. Get permissions for plan
            // ==============================

            Map<String, List<String>> incomingPlanPermissions =
                    incomingPermissions.get(planName);

            if (incomingPlanPermissions == null
                    || incomingPlanPermissions.isEmpty()) {

                response.setSuccess(false);

                response.setMessage(
                        "Permissions for plan "
                                + planName
                                + " are required."
                );

                response.setStatus(
                        HttpStatus.BAD_REQUEST.value()
                );

                return response;
            }

            // ==============================
            // 7. Add or override modules
            // ==============================

            for (Map.Entry<String, List<String>> entry
                    : incomingPlanPermissions.entrySet()) {

                String moduleName = entry.getKey();

                List<String> modulePermissions =
                        entry.getValue();

                // Existing module -> override
                // New module -> add
                existingPlanPermissions.put(
                        moduleName,
                        modulePermissions
                );
            }

            // ==============================
            // 8. Save
            // ==============================

            permission.setPermissions(allPermissions);

            Permission updatedPermission =
                    permissionRepository.save(permission);

            // ==============================
            // 9. Prepare response
            // ==============================

            Map<String, Object> responseData =
                    new LinkedHashMap<>();

            responseData.put(
                    "id",
                    updatedPermission.getId()
            );

            responseData.put(
                    "planId",
                    planId
            );

            responseData.put(
                    "planName",
                    planName
            );

            responseData.put(
                    "permissions",
                    updatedPermission
                            .getPermissions()
                            .get(planName)
            );

            response.setSuccess(true);
            response.setData(responseData);

            response.setMessage(
                    "Permissions updated successfully for plan: "
                            + planName
            );

            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    "Failed to update permissions : "
                            + e.getMessage()
            );

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }
    @Override
    public Response updateByPlanId(
            String id,
            String planId,
            PermissionDTO dto) {

        Response response = new Response();

        try {

            // ==============================
            // 1. Convert plan ID to plan name
            // ==============================

            String planName;

            switch (planId) {

                case "1":
                    planName = "Basic";
                    break;

                case "2":
                    planName = "Pro";
                    break;

                case "3":
                    planName = "Elite";
                    break;

                case "4":
                    planName = "Enterprise";
                    break;

                default:

                    response.setSuccess(false);
                    response.setMessage(
                            "Invalid plan ID. Use 1=Basic, 2=Pro, 3=Elite, 4=Enterprise."
                    );
                    response.setStatus(HttpStatus.BAD_REQUEST.value());

                    return response;
            }

            // ==============================
            // 2. Find Permission document
            // ==============================

            Permission permission = permissionRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Permissions not found for id: " + id
                            )
                    );

            // ==============================
            // 3. Validate request
            // ==============================

            if (dto == null
                    || dto.getPermissions() == null
                    || dto.getPermissions().isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Permissions are required.");
                response.setStatus(HttpStatus.BAD_REQUEST.value());

                return response;
            }

            // ==============================
            // 4. Get existing permissions
            // ==============================

            Map<String, Map<String, List<String>>> allPermissions =
                    permission.getPermissions();

            if (allPermissions == null) {
                allPermissions = new LinkedHashMap<>();
            }

            // ==============================
            // 5. Get incoming permissions
            // ==============================

            Map<String, List<String>> incomingPlanPermissions =
                    dto.getPermissions().get(planName);

            if (incomingPlanPermissions == null) {

                response.setSuccess(false);
                response.setMessage(
                        "Permissions for plan "
                                + planName
                                + " are required."
                );
                response.setStatus(HttpStatus.BAD_REQUEST.value());

                return response;
            }

            // ==============================
            // 6. Replace complete plan permissions
            // ==============================

            Map<String, List<String>> updatedPlanPermissions =
                    new LinkedHashMap<>(incomingPlanPermissions);

            allPermissions.put(
                    planName,
                    updatedPlanPermissions
            );

            // ==============================
            // 7. Save
            // ==============================

            permission.setPermissions(allPermissions);

            Permission updatedPermission =
                    permissionRepository.save(permission);

            // ==============================
            // 8. Prepare response
            // ==============================

            Map<String, Object> planData =
                    new LinkedHashMap<>();

            planData.put(
                    "id",
                    updatedPermission.getId()
            );

            planData.put(
                    "planId",
                    planId
            );

            planData.put(
                    "planName",
                    planName
            );

            planData.put(
                    "permissions",
                    updatedPermission
                            .getPermissions()
                            .get(planName)
            );

            response.setSuccess(true);
            response.setData(planData);

            response.setMessage(
                    "Permissions updated successfully for plan: "
                            + planName
            );

            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);

            response.setMessage(
                    "Failed to update permissions: "
                            + e.getMessage()
            );

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }
    
    @Override
    public Response deleteByPlanId(
            String id,
            String planId,
            PermissionDTO dto) {

        Response response = new Response();

        try {

            // ==============================
            // 1. Convert plan ID to plan name
            // ==============================

            String planName;

            switch (planId) {

                case "1":
                    planName = "Basic";
                    break;

                case "2":
                    planName = "Pro";
                    break;

                case "3":
                    planName = "Elite";
                    break;

                case "4":
                    planName = "Enterprise";
                    break;

                default:
                    response.setSuccess(false);
                    response.setMessage(
                            "Invalid plan ID. Use 1=Basic, 2=Pro, 3=Elite, 4=Enterprise."
                    );
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    return response;
            }

            // ==============================
            // 2. Validate request
            // ==============================

            if (dto == null
                    || dto.getPermissions() == null
                    || dto.getPermissions().isEmpty()) {

                response.setSuccess(false);
                response.setMessage("Permissions are required for deletion.");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            // ==============================
            // 3. Find permission document
            // ==============================

            Permission permission = permissionRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Permissions not found for id: " + id
                            )
                    );

            Map<String, Map<String, List<String>>> allPermissions =
                    permission.getPermissions();

            // ==============================
            // 4. Check plan
            // ==============================

            if (allPermissions == null
                    || !allPermissions.containsKey(planName)) {

                response.setSuccess(false);
                response.setMessage(
                        "Permissions not found for plan: " + planName
                );
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return response;
            }

            Map<String, List<String>> existingPlanPermissions =
                    allPermissions.get(planName);

            // ==============================
            // 5. Get incoming plan permissions
            // ==============================

            Map<String, Map<String, List<String>>> incomingPermissions =
                    dto.getPermissions();

            Map<String, List<String>> permissionsToDelete =
                    incomingPermissions.get(planName);

            if (permissionsToDelete == null
                    || permissionsToDelete.isEmpty()) {

                response.setSuccess(false);
                response.setMessage(
                        "Permissions for plan " + planName
                                + " are required."
                );
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            // ==============================
            // 6. Remove selected permissions
            // ==============================

            for (Map.Entry<String, List<String>> entry
                    : permissionsToDelete.entrySet()) {

                String moduleName = entry.getKey();

                List<String> actionsToDelete = entry.getValue();

                if (!existingPlanPermissions.containsKey(moduleName)) {

                    response.setSuccess(false);
                    response.setMessage(
                            "Module not found: " + moduleName
                    );
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return response;
                }

                List<String> existingActions =
                        existingPlanPermissions.get(moduleName);

                existingActions.removeAll(actionsToDelete);
            }

            // ==============================
            // 7. Save
            // ==============================

            permission.setPermissions(allPermissions);

            Permission updatedPermission =
                    permissionRepository.save(permission);

            // ==============================
            // 8. Response
            // ==============================

            Map<String, Object> responseData =
                    new LinkedHashMap<>();

            responseData.put("planId", planId);
            responseData.put("planName", planName);

            responseData.put(
                    "permissions",
                    updatedPermission
                            .getPermissions()
                            .get(planName)
            );

            response.setSuccess(true);
            response.setData(responseData);
            response.setMessage(
                    "Selected permissions deleted successfully for plan: "
                            + planName
            );
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(
                    "Failed to delete permissions : "
                            + e.getMessage()
            );
            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return response;
    }

}