//package com.clinicadmin.service.impl;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
////import com.clinicadmin.dto.ProbableDiagnosisDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.entity.ProbableDiagnosis;
//import com.clinicadmin.feignclient.AdminServiceClient;
//import com.clinicadmin.repository.ProbableDiagnosisRepository;
//import com.clinicadmin.service.ProbableDiagnosisService;
//
//@Service
//public class ProbableDiagnosisServiceImpl implements ProbableDiagnosisService {
//
//    @Autowired
//    ProbableDiagnosisRepository probableDiagnosisRepository;
//
//    @Autowired
//    AdminServiceClient adminServiceClient;
//
//    //-----------------------------------------------------Adding Diseases (single or multiple)-------------------------------------------------
//    @Override
//    public Response addDiseases(List<ProbableDiagnosisDTO> dtoList) {
//        Response response = new Response();
//
//        if (dtoList == null || dtoList.isEmpty()) {
//            response.setSuccess(false);
//            response.setMessage("No diseases provided");
//            response.setStatus(HttpStatus.BAD_REQUEST.value());
//            return response;
//        }
//
//        List<ProbableDiagnosisDTO> savedDtos = dtoList.stream().map(dto -> {
//            ProbableDiagnosis entity = new ProbableDiagnosis();
//            entity.setDiseaseName(dto.getDiseaseName());
//            entity.setHospitalId(dto.getHospitalId());
//            entity.setProbableSymptoms(dto.getProbableSymptoms());
//            entity.setNotes(dto.getNotes());
//
//            ProbableDiagnosis saved = probableDiagnosisRepository.save(entity);
//
//            return new ProbableDiagnosisDTO(
//                    saved.getId().toString(),
//                    saved.getDiseaseName(),
//                    saved.getHospitalId(),
//                    saved.getProbableSymptoms(),
//                    saved.getNotes()
//            );
//        }).collect(Collectors.toList());
//
//        response.setSuccess(true);
//        response.setData(savedDtos);
//        response.setMessage("Diseases added successfully");
//        response.setStatus(HttpStatus.OK.value());
//        return response;
//    }
//
//    //----------------------------------------------------Get all Diseases-------------------------------------------------
//    @Override
//    public Response getAllDiseases() {
//        Response response = new Response();
//        try {
//            List<ProbableDiagnosis> diseases = probableDiagnosisRepository.findAll();
//            if (!diseases.isEmpty()) {
//                List<ProbableDiagnosisDTO> diseasesDTO = diseases.stream()
//                        .map(a -> new ProbableDiagnosisDTO(
//                                a.getId().toString(),
//                                a.getDiseaseName(),
//                                a.getHospitalId(),
//                                a.getProbableSymptoms(),
//                                a.getNotes()
//                        ))
//                        .collect(Collectors.toList());
//
//                response.setSuccess(true);
//                response.setData(diseasesDTO);
//                response.setMessage("Diseases retrieved successfully");
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            } else {
//                response.setSuccess(true);
//                response.setData(Collections.emptyList());
//                response.setMessage("No Data Found");
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            }
//
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Exception occurred while getting Diseases: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            return response;
//        }
//    }
//
//    //----------------------------------------------------Get Disease By Id-------------------------------------------------
//    @Override
//    public Response getDiseaseById(String id, String hospitalId) {
//        Response response = new Response();
//        try {
//            ObjectId dsId = new ObjectId(id);
//            Optional<ProbableDiagnosis> savedDisease = probableDiagnosisRepository.findByIdAndHospitalId(dsId, hospitalId);
//
//            if (savedDisease.isPresent()) {
//                ProbableDiagnosis ds = savedDisease.get();
//                ProbableDiagnosisDTO resDto = new ProbableDiagnosisDTO(
//                        ds.getId().toString(),
//                        ds.getDiseaseName(),
//                        ds.getHospitalId(),
//                        ds.getProbableSymptoms(),
//                        ds.getNotes()
//                );
//                response.setSuccess(true);
//                response.setData(resDto);
//                response.setMessage("Disease retrieved successfully");
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            }
//
//            response.setSuccess(true);
//            response.setData(Collections.emptyList());
//            response.setMessage("Data not found with this Id: " + id);
//            response.setStatus(HttpStatus.OK.value());
//            return response;
//
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Exception occurred while getting Disease by Id: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            return response;
//        }
//    }
//
//    //----------------------------------------------------Delete Disease By Id-------------------------------------------------
//    @Override
//    public Response deleteDiseaseById(String id, String hospitalId) {
//        Response response = new Response();
//        try {
//            ObjectId dsId = new ObjectId(id);
//            Optional<ProbableDiagnosis> savedDisease = probableDiagnosisRepository.findByIdAndHospitalId(dsId, hospitalId);
//            if (savedDisease.isPresent()) {
//                probableDiagnosisRepository.deleteById(dsId);
//                response.setSuccess(true);
//                response.setMessage("Disease deleted successfully");
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            }
//            response.setSuccess(true);
//            response.setMessage("Data not found with this Id: " + id);
//            response.setStatus(HttpStatus.OK.value());
//            return response;
//
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Exception occurred while deleting Disease: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            return response;
//        }
//    }
//
//    //-----------------------Update Disease by DiseaseId-------------------------------------------------
//    @Override
//    public Response updateDiseaseById(String id, String hospitalId, ProbableDiagnosisDTO dto) {
//        Response response = new Response();
//        try {
//            if (id == null || hospitalId == null || dto == null) {
//                response.setSuccess(false);
//                response.setMessage("Id, HospitalId, and DTO cannot be null");
//                response.setStatus(HttpStatus.BAD_REQUEST.value());
//                return response;
//            }
//
//            Optional<ProbableDiagnosis> savedDs = probableDiagnosisRepository.findByIdAndHospitalId(new ObjectId(id), hospitalId);
//
//            if (savedDs.isPresent()) {
//                ProbableDiagnosis upDs = savedDs.get();
//
//                if (dto.getDiseaseName() == null || dto.getDiseaseName().isEmpty()) {
//                    response.setSuccess(false);
//                    response.setMessage("Disease name cannot be null or empty");
//                    response.setStatus(HttpStatus.BAD_REQUEST.value());
//                    return response;
//                }
//
//                upDs.setDiseaseName(dto.getDiseaseName());
//                upDs.setProbableSymptoms(dto.getProbableSymptoms());
//                upDs.setNotes(dto.getNotes());
//
//                ProbableDiagnosis savedUpDs = probableDiagnosisRepository.save(upDs);
//
//                ProbableDiagnosisDTO upDTO = new ProbableDiagnosisDTO(
//                        savedUpDs.getId().toString(),
//                        savedUpDs.getDiseaseName(),
//                        savedUpDs.getHospitalId(),
//                        savedUpDs.getProbableSymptoms(),
//                        savedUpDs.getNotes()
//                );
//
//                response.setSuccess(true);
//                response.setData(upDTO);
//                response.setMessage("Disease updated successfully");
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            }
//
//            response.setSuccess(false);
//            response.setMessage("Data not found with this Id: " + id);
//            response.setStatus(HttpStatus.NOT_FOUND.value());
//            return response;
//
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Exception occurred while updating disease: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            return response;
//        }
//    }
//
//    //----------------------------------------------------Get all Diseases by HospitalId-------------------------------------------------
//    @Override
//    public Response getAllDiseasesByHospitalId(String hospitalId) {
//        Response response = new Response();
//        try {
//            List<ProbableDiagnosis> diseases = probableDiagnosisRepository.findByHospitalId(hospitalId);
//            if (!diseases.isEmpty()) {
//                List<ProbableDiagnosisDTO> diseasesDTO = diseases.stream()
//                        .map(a -> new ProbableDiagnosisDTO(
//                                a.getId().toString(),
//                                a.getDiseaseName(),
//                                a.getHospitalId(),
//                                a.getProbableSymptoms(),
//                                a.getNotes()
//                        ))
//                        .collect(Collectors.toList());
//
//                response.setSuccess(true);
//                response.setData(diseasesDTO);
//                response.setMessage("Diseases retrieved successfully for hospitalId: " + hospitalId);
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            } else {
//                response.setSuccess(true);
//                response.setData(Collections.emptyList());
//                response.setMessage("No diseases found for hospitalId: " + hospitalId);
//                response.setStatus(HttpStatus.OK.value());
//                return response;
//            }
//        } catch (Exception e) {
//            response.setSuccess(false);
//            response.setMessage("Exception occurred while getting diseases by hospitalId: " + e.getMessage());
//            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            return response;
//        }
//    }
//}