//package com.clinicadmin.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.clinicadmin.dto.ProbableDiagnosisDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.service.ProbableDiagnosisService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RestController
//@RequestMapping("/clinic-admin")
////@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
//public class ProbableDiagnosisController {
//
//    @Autowired
//    private ProbableDiagnosisService probableDiagnosisService;
//    
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//
//
//    @PostMapping("/addDiseases")
//    public ResponseEntity<Response> addDiseases(@RequestBody Object requestBody) {
//        List<ProbableDiagnosisDTO> dtoList = new ArrayList<>();
//
//        try {
//            if (requestBody instanceof List<?>) {
//                // Convert list of LinkedHashMap -> List<DTO>
//                dtoList = objectMapper.convertValue(requestBody, new TypeReference<List<ProbableDiagnosisDTO>>() {});
//            } else {
//                // Convert single object -> DTO and wrap in list
//                ProbableDiagnosisDTO dto = objectMapper.convertValue(requestBody, ProbableDiagnosisDTO.class);
//                dtoList.add(dto);
//            }
//        } catch (IllegalArgumentException e) {
//            Response error = new Response(false, "Invalid request data: " + e.getMessage(),null, null, 400, null, null, null, null, null, null);
//            return ResponseEntity.status(400).body(error);
//        }
//
//        // Call service
//        Response response = probableDiagnosisService.addDiseases(dtoList);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @GetMapping("/get-all-diseases")
//    public ResponseEntity<Response> getAllDiseases() {
//        Response response = probableDiagnosisService.getAllDiseases();
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @GetMapping("/getDisease/{id}/{hospitalId}")
//    public ResponseEntity<Response> getDiseaseByDiseaseId(@PathVariable String id, @PathVariable String hospitalId) {
//        Response response = probableDiagnosisService.getDiseaseById(id, hospitalId);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @DeleteMapping("/deleteDisease/{id}/{hospitalId}")
//    public ResponseEntity<Response> deleteDiseaseByDiseaseId(@PathVariable String id, @PathVariable String hospitalId) {
//        Response response = probableDiagnosisService.deleteDiseaseById(id, hospitalId);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @PutMapping("/updateDisease/{id}/{hospitalId}")
//    public ResponseEntity<Response> updateDiseaseByDiseaseId(
//            @PathVariable String id,
//            @PathVariable String hospitalId,
//            @RequestBody ProbableDiagnosisDTO dto) {
//        Response response = probableDiagnosisService.updateDiseaseById(id, hospitalId, dto);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @GetMapping("/diseases/{hospitalId}")
//    public ResponseEntity<Response> getDiseasesByHospitalId(@PathVariable String hospitalId) {
//        Response response = probableDiagnosisService.getAllDiseasesByHospitalId(hospitalId);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//}