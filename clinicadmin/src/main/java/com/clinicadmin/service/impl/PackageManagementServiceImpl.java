package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PackageManagementDTO;
import com.clinicadmin.dto.ProgramWithTherophy;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.PackageManagement;
import com.clinicadmin.entity.TherophyProgramEntity;
import com.clinicadmin.repository.PackageManagementRepository;
import com.clinicadmin.repository.TherophyProgramRepository;
import com.clinicadmin.service.PackageManagementService;
import com.clinicadmin.service.TherophyProgramService;

@Service
public class PackageManagementServiceImpl implements PackageManagementService {

    @Autowired
    private PackageManagementRepository repository;
    
    @Autowired
    
    private TherophyProgramRepository  therophyProgramRepository;
    
    @Autowired
    private TherophyProgramService therophyProgramService;

    // ✅ CREATE
    @Override
    public Response createPackage(PackageManagementDTO dto) {

        Response response = new Response();

        try {
            PackageManagement entity = mapToEntity(dto);
            entity.setPackageId(generatePackageId());

            PackageManagement saved = repository.save(entity);

            PackageManagementDTO responseDto = mapToDTO(saved);

            // ✅ FIX: set noOfPrograms correctly
            responseDto.setNoOfPrograms(
                saved.getProgramIds() != null ? saved.getProgramIds().size() : 0
            );
            response.setSuccess(true);
            response.setData(responseDto);
            response.setMessage("Package created successfully");
            response.setStatus(HttpStatus.CREATED.value());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    @Override
    public Response getByClinicAndBranch(String clinicId, String branchId) {

        Response response = new Response();

        try {
            List<PackageManagement> list =
                    repository.findByClinicIdAndBranchId(clinicId, branchId);

            List<PackageManagementDTO> dtoList = new ArrayList<>();

            for (PackageManagement entity : list) {

                List<String> programIds = entity.getProgramIds();
                List<TherophyProgramEntity> programList = new ArrayList<>();

                // ✅ Fetch programs
                if (programIds != null && !programIds.isEmpty()) {
                    programList = therophyProgramRepository.findByIdIn(programIds);
                }

                // ✅ Convert package → DTO
                PackageManagementDTO dto = mapToDTO(entity);

                // ✅ Only id + programName (NO DTO change, NO null fields)
                List<Object> cleanList = programList.stream()
                        .map(p -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", p.getId());
                            map.put("programName", p.getProgramName());
                            return (Object) map;
                        })
                        .toList();

                dto.setPrograms((List) cleanList); 
                dto.setNoOfPrograms(cleanList.size());
                dtoList.add(dto);
            }

            response.setSuccess(true);
            response.setData(dtoList);
            response.setMessage("Packages fetched successfully");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
    // ✅ GET by clinicId + branchId + packageId
    @Override
    public Response getByClinicBranchAndPackageId(String clinicId, String branchId, String packageId) {

        Response response = new Response();

        try {
            Optional<PackageManagement> optional =
                    repository.findByClinicIdAndBranchIdAndPackageId(clinicId, branchId, packageId);

            if (optional.isPresent()) {

                PackageManagementDTO dto = mapToDTO(optional.get());

                response.setSuccess(true);
                response.setData(dto);
                response.setMessage("Package found");
                response.setStatus(HttpStatus.OK.value());

            } else {
                response.setSuccess(false);
                response.setData(null);
                response.setMessage("Package not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    @Override
    public Response getByClinicId(String clinicId) {

        Response response = new Response();

        try {

            List<PackageManagement> packages =
                    repository.findByClinicId(clinicId);

            if (!packages.isEmpty()) {

                List<PackageManagementDTO> dtoList = packages.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());

                response.setSuccess(true);
                response.setData(dtoList);
                response.setMessage("Packages found");
                response.setStatus(HttpStatus.OK.value());

            } else {

                response.setSuccess(false);
                response.setData(null);
                response.setMessage("No packages found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {

            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
    @Override
    public Response updatePackage(String packageId, PackageManagementDTO dto) {

        Response response = new Response();

        try {
            Optional<PackageManagement> optional = repository.findByPackageId(packageId);

            if (optional.isPresent()) {

                PackageManagement entity = optional.get();

                // ✅ Use separate update mapper
                updateEntityFromDTO(entity, dto);

                PackageManagement updated = repository.save(entity);

                response.setSuccess(true);
                response.setData(mapToDTO(updated));
                response.setMessage("Package updated successfully");
                response.setStatus(HttpStatus.OK.value());

            } else {
                response.setSuccess(false);
                response.setData(null);
                response.setMessage("Package not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    // ✅ DELETE
    @Override
    public Response deletePackage(String packageId) {

        Response response = new Response();

        try {
            Optional<PackageManagement> optional = repository.findByPackageId(packageId);

            if (optional.isPresent()) {

                repository.delete(optional.get());

                response.setSuccess(true);
                response.setData(null);
                response.setMessage("Package deleted successfully");
                response.setStatus(HttpStatus.OK.value());

            } else {
                response.setSuccess(false);
                response.setData(null);
                response.setMessage("Package not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    // ================= MAPPERS =================

    private PackageManagement mapToEntity(PackageManagementDTO dto) {

        PackageManagement entity = new PackageManagement();

        entity.setPackageName(dto.getPackageName());
        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());       
        entity.setProgramIds(dto.getProgramIds());
        entity.setPackageAmount(dto.getPackageAmount());
        entity.setDiscountAmount(dto.getDiscountAmount());
        String finalAmount = String.valueOf(Integer.valueOf(dto.getPackageAmount()) - Integer.valueOf(dto.getDiscountAmount()));
        entity.setFinalAmount(finalAmount);
        // ✅ Apply discount logic
        int finalDiscount = applyDiscountLogic(
                dto.getStartOfferDate(),
                dto.getEndOfferDate(),
                dto.getDiscountPercentage()
        );

        entity.setDiscountPercentage(finalDiscount);

        entity.setStartOfferDate(dto.getStartOfferDate());
        entity.setEndOfferDate(dto.getEndOfferDate());
        entity.setOfferType(dto.getOfferType());

        return entity;
    }

    private PackageManagementDTO mapToDTO(PackageManagement entity) {

        PackageManagementDTO dto = new PackageManagementDTO();

        dto.setPackageId(entity.getPackageId());
        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());
        dto.setPackageName(entity.getPackageName());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setPackageAmount(entity.getPackageAmount());
        dto.setFinalAmount(entity.getFinalAmount());
        dto.setProgramIds(entity.getProgramIds());
        dto.setNoOfPrograms(
        	    entity.getProgramIds() != null ? entity.getProgramIds().size() : 0
        	);

        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setStartOfferDate(entity.getStartOfferDate());
        dto.setEndOfferDate(entity.getEndOfferDate());
        dto.setOfferType(entity.getOfferType());

        return dto;
    }

    private int applyDiscountLogic(String startDate, String endDate, int discount) {

        LocalDate today = LocalDate.now();

        // ✅ No dates -> no discount
        if ((startDate == null || startDate.trim().isEmpty()) &&
            (endDate == null || endDate.trim().isEmpty())) {

            return 0;
        }

        // ✅ Only start date given -> apply discount from start date
        if (startDate != null && !startDate.trim().isEmpty() &&
            (endDate == null || endDate.trim().isEmpty())) {

            LocalDate start = parseDate(startDate);

            if (!today.isBefore(start)) {
                return discount;
            }

            return 0;
        }

        // ✅ Only end date given -> no discount
        if ((startDate == null || startDate.trim().isEmpty()) &&
            endDate != null && !endDate.trim().isEmpty()) {

            return 0;
        }

        // ✅ Both dates given
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        // ✅ Invalid range
        if (end.isBefore(start)) {
            return 0;
        }

        // ✅ Apply discount only within date range
        if (!today.isBefore(start) && !today.isAfter(end)) {
            return discount;
        }

        // ✅ Offer expired automatically
        return 0;
    }
    private LocalDate parseDate(String dateStr) {

        String[] formats = {
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "MM-dd-yyyy",
                "yyyy/MM/dd",
                "dd/MM/yyyy"
        };

        for (String format : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException e) {
                // try next
            }
        }

        throw new RuntimeException("Invalid date format: " + dateStr);
    }

    private String generatePackageId() {
        return "PKG-" + System.currentTimeMillis();
    }
    private void updateEntityFromDTO(PackageManagement entity, PackageManagementDTO dto) {

        // ✅ Update only required fields
        if (dto.getPackageName() != null) {
            entity.setPackageName(dto.getPackageName());
        }

        if (dto.getProgramIds() != null) {
            entity.setProgramIds(dto.getProgramIds());
        }
        
        if (dto.getFinalAmount() != null) {
            entity.setFinalAmount(dto.getFinalAmount());;
        }

        if (dto.getStartOfferDate() != null) {
            entity.setStartOfferDate(dto.getStartOfferDate());
        }
        
        if (dto.getDiscountAmount()!= null) {
            entity.setDiscountAmount(dto.getDiscountAmount());
        }
        
        if (dto.getPackageAmount() != null) {
            entity.setPackageAmount(dto.getPackageAmount());
        }

        if (dto.getEndOfferDate() != null) {
            entity.setEndOfferDate(dto.getEndOfferDate());
        }

        if (dto.getOfferType() != null) {
            entity.setOfferType(dto.getOfferType());
        }

     // ✅ Apply discount logic ONLY when needed
        if (dto.getDiscountPercentage() != 0 ||
            dto.getStartOfferDate() != null ||
            dto.getEndOfferDate() != null) {

            int finalDiscount = applyDiscountLogic(
                    entity.getStartOfferDate(),
                    entity.getEndOfferDate(),
                    dto.getDiscountPercentage()
            );

            entity.setDiscountPercentage(finalDiscount);
        }
    }
//    @Override
//    public Response getPackageWithPrograms( String clinicId, String branchId,String packageId) {
//
//        Response response = new Response();
//
//        try {
//            Optional<PackageManagement> optional =
//                    repository.findByClinicIdAndBranchIdAndPackageId(
//                            clinicId, branchId, packageId);
//
//            if (optional.isEmpty()) {
//                response.setSuccess(false);
//                response.setMessage("Package not found");
//                response.setStatus(HttpStatus.NOT_FOUND.value());
//                return response;
//            }
//
//            PackageManagement entity = optional.get();
//
//            List<String> programIds = entity.getProgramIds();
//            List<TherophyProgramEntity> programList = new ArrayList<>();
//
//            if (programIds != null && !programIds.isEmpty()) {
//
//                programList = therophyProgramRepository.findByIdIn(programIds);
//
//                // 🔥 CLEANUP LOGIC
//                List<String> validIds = programList.stream()
//                        .map(TherophyProgramEntity::getId)
//                        .toList();
//
//                if (!programIds.equals(validIds)) {
//                    entity.setProgramIds(validIds);
//                    repository.save(entity); // ✅ removes deleted IDs
//                }
//            }
//
//            PackageManagementDTO dto = mapToDTO(entity);
//
//            dto.setPrograms(programList);
//            dto.setNoOfPrograms(programList.size());
//
//            response.setSuccess(true);
//            response.setData(dto);
//            response.setMessage("Fetched successfully with programs");
//            response.setStatus(HttpStatus.OK.value());
//
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Error: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
//
//        return response;
//    }
    
    @Override
    public Response getPackageWithPrograms(String clinicId, String branchId, String packageId) {

        Response response = new Response();

        try {
            Optional<PackageManagement> optional =
                    repository.findByClinicIdAndBranchIdAndPackageId(
                            clinicId, branchId, packageId);

            if (optional.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Package not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return response;
            }

            PackageManagement entity = optional.get();

            List<String> programIds = entity.getProgramIds();

            // 🔥 FULL DATA LIST (Program + Therapy + Exercise)
            List<ProgramWithTherophy> programFullData = new ArrayList<>();

            if (programIds != null && !programIds.isEmpty()) {

                for (String programId : programIds) {

                    ResponseEntity<Response> programResponse =
                            therophyProgramService.getByclinicAndBranchIdAndId(
                                    clinicId, branchId, programId);

                    if (programResponse.getBody() != null &&
                            programResponse.getBody().isSuccess()) {

                        ProgramWithTherophy program =
                                (ProgramWithTherophy) programResponse.getBody().getData();

                        if (program != null) {

                            // 🔥 REMOVE IMAGE FIELD FROM EXERCISES
                            if (program.getTherophyData() != null) {

                                program.getTherophyData().forEach(therapy -> {

                                    if (therapy != null && therapy.getExercises() != null) {

                                        therapy.getExercises().forEach(exercise -> {
                                            if (exercise != null) {
                                                exercise.setImage(null); // ✅ REMOVE IMAGE
                                            }
                                        });

                                    }
                                });
                            }

                            programFullData.add(program);
                        }
                    }
                }

                // ✅ CLEANUP INVALID PROGRAM IDS
                List<String> validIds = programFullData.stream()
                        .map(ProgramWithTherophy::getId)
                        .toList();

                if (!programIds.equals(validIds)) {
                    entity.setProgramIds(validIds);
                    repository.save(entity);
                }
            }

            PackageManagementDTO dto = mapToDTO(entity);

            // 🔥 KEEP YOUR EXISTING STRUCTURE
            dto.setPrograms((List) programFullData);

            dto.setNoOfPrograms(programFullData.size());

            response.setSuccess(true);
            response.setData(dto);
            response.setMessage("Fetched successfully with full data ");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
}