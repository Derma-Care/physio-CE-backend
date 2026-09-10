package com.clinicadmin.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TherapistRecordDTO;
import com.clinicadmin.dto.TherapistRecordRequest;
import com.clinicadmin.entity.TherapistRecord;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.repository.TherapistRecordRepository;
import com.clinicadmin.service.FeedbackDetailsServcie;
import com.clinicadmin.service.S3Service;
import com.clinicadmin.service.TherapistRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TherapistRecordServiceImpl implements TherapistRecordService {

    @Autowired
    private TherapistRecordRepository repository;

    @Autowired
    private PhysiotherapyFeignClient physiotherapyFeignClient;

    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private FeedbackDetailsServcie feedbackDetailsService;

    @Override
    public ResponseStructure<TherapistRecordDTO> saveRecord(TherapistRecordDTO dto) {

        if (dto == null) {
            return ResponseStructure.buildResponse(
                    null,
                    "Request body is null",
                    HttpStatus.BAD_REQUEST,
                    400
            );
        }

        TherapistRecord record = mapToEntity(dto);
        record.setStatus("COMPLETED");

        // ✅ Store raw file keys in DB
        if (dto.getBeforeImage() != null && !dto.getBeforeImage().isBlank()) {
            record.setBeforeImage(dto.getBeforeImage());
        }
        if (dto.getAfterImage() != null && !dto.getAfterImage().isBlank()) {
            record.setAfterImage(dto.getAfterImage());
        }
        if (dto.getBeforeVideo() != null && !dto.getBeforeVideo().isBlank()) {
            record.setBeforeVideo(dto.getBeforeVideo());
        }
        if (dto.getAfterVideo() != null && !dto.getAfterVideo().isBlank()) {
            record.setAfterVideo(dto.getAfterVideo());
        }
        if (dto.getVoiceRecord() != null && !dto.getVoiceRecord().isBlank()) {
            record.setVoiceRecord(dto.getVoiceRecord());
        }
        if (dto.getConsentPdfUrl() != null && !dto.getConsentPdfUrl().isBlank()) {
            record.setConsentPdfUrl(dto.getConsentPdfUrl());
        }

        record.setTherapistRecordId(dto.getTherapistRecordId());
        record.setSessionId(dto.getSessionId());

        TherapistRecord saved = repository.save(record);

        // Call Physiotherapy Service
        try {
            if (dto.getTherapistRecordId() != null
                    && !dto.getTherapistRecordId().trim().isEmpty()
                    && dto.getSessionId() != null
                    && !dto.getSessionId().trim().isEmpty()) {

                physiotherapyFeignClient.updateSessionStatus(
                        dto.getTherapistRecordId().trim(),
                        dto.getSessionId().trim()
                );
                
                feedbackDetailsService.processFeedbackNotification(
                        dto.getClinicId(),
                        dto.getBranchId()
                );
            

            }
        } catch (Exception e) {
            System.out.println("Physio update failed");
            e.printStackTrace();
        }

        // ✅ Return signed URLs in response (not raw keys)
        return ResponseStructure.buildResponse(
                mapToDTOWithSignedUrls(saved),
                "Record saved successfully",
                HttpStatus.CREATED,
                201
        );
    }

    // ================= GET =================
    @Override
    public ResponseStructure<TherapistRecordDTO> getByIds(
            String clinicId, String branchId, String therapistRecordId,String sessionId) {

        TherapistRecord record = repository
        		.findByClinicIdAndBranchIdAndTherapistRecordIdAndSessionId(
        		        clinicId, branchId, therapistRecordId, sessionId)
        		.orElseThrow(() -> new RuntimeException("Record not found"));

        return ResponseStructure.buildResponse(
        		 mapToDTOWithSignedUrls(record), // ✅
                "Record fetched successfully",
                HttpStatus.OK,
                200
        );
    }

    // ================= MAPPING =================

    private TherapistRecord mapToEntity(TherapistRecordDTO dto) {

        TherapistRecord record = new TherapistRecord();
//       record.s
        record.setClinicId(dto.getClinicId());
        record.setBranchId(dto.getBranchId());
        record.setPatientId(dto.getPatientId());
        record.setBookingId(dto.getBookingId());
        record.setTherapistId(dto.getTherapistId());
        record.setTherapistName(dto.getTherapistName());

        record.setPatientName(dto.getPatientName());
//        record.setTherapy(dto.getTherapy());

//        record.setDate(dto.getDate());
        record.setCompletedDate(dto.getCompletedDate());
        record.setCompletedTime(dto.getCompletedTime());

        record.setDuration(dto.getDuration());
//        record.setExercises(dto.getExercises());

        record.setPainBefore(dto.getPainBefore());
        record.setPainAfter(dto.getPainAfter());

        record.setTherapistNotes(dto.getTherapistNotes());
        record.setPatientResponse(dto.getPatientResponse());
        record.setSessionId(dto.getSessionId());

        record.setResult(dto.getResult());
//        record.setStatus(dto.getStatus());
        record.setMode(dto.getMode());
        record.setNextPlan(dto.getNextPlan());
        record.setRepetationDone(dto.getRepetationDone());
        record.setSetsDone(dto.getSetsDone());
        record.setServiceType(dto.getServiceType());


        record.setLatitude(dto.getLatitude());
        record.setLongitude(dto.getLongitude());

        // ✔ 1. If frontend sends location → use it
        if (dto.getLocation() != null && !dto.getLocation().isEmpty()) {

            record.setLocation(dto.getLocation());

        } 
        // ✔ 2. Else generate automatically
        else if (dto.getLatitude() != null && dto.getLongitude() != null) {

            String city = getCityFromLatLong(
                    dto.getLatitude(),
                    dto.getLongitude()
            );

            record.setLocation(city);
        }

        return record;
    }
    private TherapistRecordDTO mapToDTO(TherapistRecord record) {

        TherapistRecordDTO dto = new TherapistRecordDTO();

        dto.setId(record.getId());
        dto.setTherapistRecordId(record.getTherapistRecordId());
        dto.setClinicId(record.getClinicId());
        dto.setBranchId(record.getBranchId());

        dto.setPatientId(record.getPatientId());
        dto.setBookingId(record.getBookingId());
        dto.setTherapistId(record.getTherapistId());
        dto.setTherapistName(record.getTherapistName());
        
        dto.setPatientName(record.getPatientName());
//        dto.setTherapy(record.getTherapy());

//        dto.setDate(record.getDate());
        dto.setCompletedDate(record.getCompletedDate());
        dto.setCompletedTime(record.getCompletedTime());

        dto.setDuration(record.getDuration());
//        dto.setExercises(record.getExercises());

        dto.setPainBefore(record.getPainBefore());
        dto.setPainAfter(record.getPainAfter());

        dto.setTherapistNotes(record.getTherapistNotes());
        dto.setPatientResponse(record.getPatientResponse());
        dto.setSessionId(record.getSessionId());

        dto.setResult(record.getResult());
        dto.setStatus(record.getStatus());
        dto.setMode(record.getMode());
        dto.setNextPlan(record.getNextPlan());
//        dto.setVoiceRecord(record.getVoiceRecord());
        dto.setRepetationDone(record.getRepetationDone());
        dto.setSetsDone(record.getSetsDone());
        dto.setServiceType(record.getServiceType());
        dto.setLatitude(record.getLatitude());
        dto.setLongitude(record.getLongitude());
        dto.setLocation(record.getLocation());
        
        
        
        dto.setBeforeImage(record.getBeforeImage());
        dto.setAfterImage(record.getAfterImage());
        dto.setBeforeVideo(record.getBeforeVideo());
        dto.setAfterVideo(record.getAfterVideo());
        dto.setVoiceRecord(record.getVoiceRecord());
        dto.setConsentPdfUrl(record.getConsentPdfUrl());

        return dto;
    }
    
 // ===================== getByPatientIdAndBookingId =====================

    @Override
    public ResponseStructure<List<TherapistRecordDTO>> getByPatientIdAndBookingId(
            String patientId,
            String bookingId) {

        List<TherapistRecord> records =
                repository.findAllByPatientIdAndBookingId(patientId, bookingId);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("No records found");
        }

        List<TherapistRecordDTO> dtoList = records.stream()
                .map(this::mapToDTOWithSignedUrls) // ✅
                .toList();

        return ResponseStructure.buildResponse(
                dtoList,
                "Records fetched successfully",
                HttpStatus.OK,
                200
        );
    }
    
 // ================= GET BY SESSION =================
    @Override
    public ResponseStructure<TherapistRecordDTO> getBySession(
            String clinicId,
            String branchId,
            String bookingId,
            String patientId,
            String sessionId) {

        TherapistRecord record = repository
                .findByClinicIdAndBranchIdAndBookingIdAndPatientIdAndSessionId(
                        clinicId,
                        branchId,
                        bookingId,
                        patientId,
                        sessionId
                )
                .orElse(null);

        if (record == null) {
            return ResponseStructure.buildResponse(
                    null,
                    "Therapist record not found",
                    HttpStatus.NOT_FOUND,
                    404
            );
        }

        return ResponseStructure.buildResponse(
                mapToDTOWithSignedUrls(record), // ✅
                "Therapist record fetched successfully",
                HttpStatus.OK,
                200
        );
    }
    private String getCityFromLatLong(String lat, String lon) {

        try {

            String url = "https://nominatim.openstreetmap.org/reverse?lat="
                    + lat + "&lon=" + lon + "&format=json";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "clinic-admin-app");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) return "Unknown";

            Map<String, Object> address = (Map<String, Object>) body.get("address");
            if (address == null) return "Unknown";

            // 🔥 Extract exact fields
            String road = (String) address.getOrDefault("road", "");
            String area = (String) address.getOrDefault("suburb",
                            address.getOrDefault("neighbourhood", ""));
            String city = (String) address.getOrDefault("city",
                            address.getOrDefault("town",
                            address.getOrDefault("village", "")));
            String state = (String) address.getOrDefault("state", "");
            String country = (String) address.getOrDefault("country", "");

            // 🔥 Build clean format (no nulls, no extra commas)
            return Stream.of(road, area, city, state, country)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(", "));

        } catch (Exception e) {
            return "Unknown";
        }
    }
    
   
    public Response getTherapistSessionDetails(
            TherapistRecordRequest request) {

        Response response = new Response();

        try {

            Optional<TherapistRecord> optional =
                    repository
                    .findByClinicIdAndBranchIdAndPatientIdAndBookingIdAndTherapistIdAndTherapistRecordId(
                            request.getClinicId(),
                            request.getBranchId(),
                            request.getPatientId(),
                            request.getBookingId(),
                            request.getTherapistId(),
                            request.getTherapistRecordId()
                    );

            if (!optional.isPresent()) {

                response.setSuccess(true);
                response.setData(null);
                response.setMessage("Record not found");
                response.setStatus(200);

                return response;
            }

            TherapistRecord record = optional.get();

            Map<String, Object> map = new LinkedHashMap<>();

            map.put("therapistNotes", record.getTherapistNotes());
            map.put("sessionId", record.getSessionId());
            map.put("setsDone", record.getSetsDone());
            map.put("repetationDone", record.getRepetationDone());
            map.put("serviceType", record.getServiceType());

            response.setSuccess(true);
            response.setData(map);
            response.setMessage("Therapist session details fetched successfully");
            response.setStatus(200);

            return response;

        } catch (Exception e) {

            response.setSuccess(false);
            response.setData(null);
            response.setMessage("Something went wrong");
            response.setStatus(500);

            return response;
        }
    }
    
    @Override
    public ResponseStructure<TherapistRecordDTO> getCompletedTherapyRecord(
            String clinicId,
            String branchId,
            String therapistRecordId,
            String sessionId) {

        TherapistRecord record = repository
                .findByClinicIdAndBranchIdAndTherapistRecordIdAndSessionId(
                        clinicId,
                        branchId,
                        therapistRecordId,
                        sessionId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        ObjectMapper mapper = new ObjectMapper();

        TherapistRecordDTO dto =
                mapper.convertValue(record, TherapistRecordDTO.class);

        ResponseStructure<TherapistRecordDTO> response =
                new ResponseStructure<>();

        response.setStatusCode(200);
        response.setMessage("Record fetched successfully");
        response.setData(mapToDTOWithSignedUrls(record)); // ✅ signed URLs


        return response;
    }
    
    private TherapistRecordDTO mapToDTOWithSignedUrls(TherapistRecord record) {

        // ✅ First map normally
        TherapistRecordDTO dto = mapToDTO(record);

        // ✅ Then replace raw keys with fresh signed URLs
        if (dto.getBeforeImage() != null && !dto.getBeforeImage().isBlank()) {
            dto.setBeforeImage(s3Service.generateSignedUrl(dto.getBeforeImage()));
        }
        if (dto.getAfterImage() != null && !dto.getAfterImage().isBlank()) {
            dto.setAfterImage(s3Service.generateSignedUrl(dto.getAfterImage()));
        }
        if (dto.getBeforeVideo() != null && !dto.getBeforeVideo().isBlank()) {
            dto.setBeforeVideo(s3Service.generateSignedUrl(dto.getBeforeVideo()));
        }
        if (dto.getAfterVideo() != null && !dto.getAfterVideo().isBlank()) {
            dto.setAfterVideo(s3Service.generateSignedUrl(dto.getAfterVideo()));
        }
        if (dto.getVoiceRecord() != null && !dto.getVoiceRecord().isBlank()) {
            dto.setVoiceRecord(s3Service.generateSignedUrl(dto.getVoiceRecord()));
        }
        if (dto.getConsentPdfUrl() != null && !dto.getConsentPdfUrl().isBlank()) {
            dto.setConsentPdfUrl(s3Service.generateSignedUrl(dto.getConsentPdfUrl()));
        }

        return dto;
    }
}