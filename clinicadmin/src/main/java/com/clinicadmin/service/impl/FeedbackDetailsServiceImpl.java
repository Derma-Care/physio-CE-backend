package com.clinicadmin.service.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.FeedbackDetailsDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ServiceInfo;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.entity.FeedbackDetails;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.feignclient.NotificationFeign;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.repository.FeedbackDetailsRepository;
import com.clinicadmin.service.FeedbackDetailsServcie;
import com.clinicadmin.service.PushNotificationService;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class FeedbackDetailsServiceImpl
        implements FeedbackDetailsServcie {

    @Autowired
    private PhysiotherapyFeignClient physiotherapyDoctorFeign;
    
    @Autowired
    private FeedbackDetailsRepository repository;
    
    @Autowired
    private CustomerOnboardingRepository customerOnboardingRepository;
    
    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private AdminServiceClient adminServiceClient;
    
    @Autowired
    private NotificationFeign notificationFeign;
    
     
    @Override
    public Response createFeedback(
            FeedbackDetailsDTO feedbackDetailsDTO) {

        Response response = new Response();

        try {

            // ================= MAP TO ENTITY =================

            FeedbackDetails entity =
                    mapToEntity(
                            feedbackDetailsDTO);

            // ================= ID GENERATION =================

            String feedbackId =
                    "FDBK-" +
                    java.time.LocalDateTime.now()
                            .format(
                                    java.time.format.DateTimeFormatter
                                            .ofPattern(
                                                    "ddMM-HHmmss"));

            entity.setId(feedbackId);
            // ================= DATE & TIME =================

            String currentDateTime =
                    java.time.LocalDateTime.now()
                            .toString();

            entity.setCreatedAt(currentDateTime);
            entity.setUpdatedAt(currentDateTime);
            // ================= SAVE =================

            FeedbackDetails saved =
                    repository.save(entity);
            
         // ================= PUSH NOTIFICATION ON CREATE =================
           // triggerSessionNotificationIfNeeded(saved);
            Map<String,String> map = new LinkedHashMap<>();  
            if(feedbackDetailsDTO.getTherapistId() != null) {
			map.put("therapistId",feedbackDetailsDTO.getTherapistId());
			map.put("patientName",feedbackDetailsDTO.getPatientName() );
			map.put("whatWentWell",feedbackDetailsDTO.getWhatWentWell());
			map.put("rating",feedbackDetailsDTO.getRating() );
			map.put("improvements",feedbackDetailsDTO.getImprovements());      	
            notificationFeign.therapistSessionFeedback(map);}
            // ================= RESPONSE =================

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Feedback created successfully");

            response.setData(
                    mapToDTO(saved));

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    
    
    @Override
    public Response getAllFeedbacks() {

        Response response = new Response();

        try {

            List<FeedbackDetailsDTO> result =
                    repository.findAll()
                            .stream()
                            .map(this::mapToDTO)
                            .toList();

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "All feedbacks fetched successfully");

            response.setData(result);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    
    @Override
    public Response getFeedbackById(String id) {

        Response response = new Response();

        try {

            FeedbackDetails feedback =
                    repository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Feedback not found"));

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Feedback fetched successfully");

            response.setData(
                    mapToDTO(feedback));

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    
    @Override
    public Response updateFeedback(
            String id,
            FeedbackDetailsDTO feedbackDetailsDTO) {

        Response response = new Response();

        try {

            FeedbackDetails existing =
                    repository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Feedback not found"));


            if (feedbackDetailsDTO.getPatientId() != null) {
                existing.setPatientId(
                        feedbackDetailsDTO.getPatientId());
            }

            if (feedbackDetailsDTO.getPatientName() != null) {
                existing.setPatientName(
                        feedbackDetailsDTO.getPatientName());
            }

            if (feedbackDetailsDTO.getMobileNumber() != null) {
                existing.setMobileNumber(
                        feedbackDetailsDTO.getMobileNumber());
            }

            if (feedbackDetailsDTO.getBookingId() != null) {
                existing.setBookingId(
                        feedbackDetailsDTO.getBookingId());
            }

            if (feedbackDetailsDTO.getDoctorId() != null) {
                existing.setDoctorId(
                        feedbackDetailsDTO.getDoctorId());
            }

            if (feedbackDetailsDTO.getDoctorName() != null) {
                existing.setDoctorName(
                        feedbackDetailsDTO.getDoctorName());
            }

            if (feedbackDetailsDTO.getTherapistId() != null) {
                existing.setTherapistId(
                        feedbackDetailsDTO.getTherapistId());
            }

            if (feedbackDetailsDTO.getTherapistName() != null) {
                existing.setTherapistName(
                        feedbackDetailsDTO.getTherapistName());
            }

            if (feedbackDetailsDTO.getTherapistRecordId() != null) {
                existing.setTherapistRecordId(
                        feedbackDetailsDTO.getTherapistRecordId());
            }

            if (feedbackDetailsDTO.getStaffId() != null) {
                existing.setStaffId(
                        feedbackDetailsDTO.getStaffId());
            }

            if (feedbackDetailsDTO.getStaffName() != null) {
                existing.setStaffName(
                        feedbackDetailsDTO.getStaffName());
            }

            if (feedbackDetailsDTO.getServiceType() != null) {
                existing.setServiceType(
                        feedbackDetailsDTO.getServiceType());
            }

            if (feedbackDetailsDTO.getService() != null) {
                existing.setService(
                        feedbackDetailsDTO.getService());
            }

            if (feedbackDetailsDTO.getTotalNoOfSessions() > 0) {
                existing.setTotalNoOfSessions(
                        feedbackDetailsDTO.getTotalNoOfSessions());
            }

            if (feedbackDetailsDTO.getNoOfSessionsCompleted() >= 0) {
                existing.setNoOfSessionsCompleted(
                        feedbackDetailsDTO.getNoOfSessionsCompleted());
            }

            existing.setHalfSessionsCompleted(
                    feedbackDetailsDTO.isHalfSessionsCompleted());

            existing.setFullSessionsCompleted(
                    feedbackDetailsDTO.isFullSessionsCompleted());

            if (feedbackDetailsDTO.getWhatWentWell() != null) {
                existing.setWhatWentWell(
                        feedbackDetailsDTO.getWhatWentWell());
            }

            if (feedbackDetailsDTO.getImprovements() != null) {
                existing.setImprovements(
                        feedbackDetailsDTO.getImprovements());
            }
         // ================= UPDATE DATE & TIME =================

            existing.setUpdatedAt(
                    java.time.LocalDateTime.now()
                            .toString());


            // ================= SAVE =================

            FeedbackDetails updated =
                    repository.save(existing);
            
//         // ================= PUSH NOTIFICATION ON UPDATE =================
//            triggerSessionNotificationIfNeeded(updated);

            // ================= RESPONSE =================

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Feedback updated successfully");

            response.setData(
                    mapToDTO(updated));

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    
    @Override
    public Response deleteFeedback(String id) {

        Response response = new Response();

        try {

            FeedbackDetails feedback =
                    repository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Feedback not found"));

            repository.delete(feedback);

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Feedback deleted successfully");

            response.setData(null);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    @Override
    public Response getFeedbackDetails(
            String clinicId,
            String branchId) {

        Response response = new Response();

        try {

            Response paymentResponse =
                    physiotherapyDoctorFeign
                            .getPayments(
                                    clinicId,
                                    branchId);

            // 👉 If Feign already failed, return directly
            if (paymentResponse == null || !paymentResponse.isSuccess()) {
                return paymentResponse;
            }
            
            List<Map<String, Object>> payments =
                    (List<Map<String, Object>>) paymentResponse.getData();

            

            if (payments == null || payments.isEmpty()) {

                response.setSuccess(paymentResponse.isSuccess());   // reuse
                response.setStatus(paymentResponse.getStatus());    // reuse
                response.setMessage(paymentResponse.getMessage());  // reuse
                response.setData(Collections.emptyList());

                return response;
            }

            List<FeedbackDetailsDTO> result =
                    new ArrayList<>();

            for (Map<String, Object> payment
                    : payments) {

                FeedbackDetailsDTO data =
                        new FeedbackDetailsDTO();
                
                data.setClinicId(clinicId);

                data.setBranchId(branchId);

                // ================= BASIC =================
                String patientId =
                        String.valueOf(
                                payment.get("patientId"));

                data.setPatientId(patientId);

                CustomerOnbording customer =
                        customerOnboardingRepository
                                .findByPatientIdAndHospitalIdAndBranchId(
                                        patientId,
                                        clinicId,
                                        branchId)
                                .orElse(null);

                if (customer != null) {

                    data.setPatientName(
                            customer.getFullName());

                    data.setMobileNumber(
                            customer.getMobileNumber());

                } else {

                    data.setPatientName(null);
                    data.setMobileNumber(null);
                }
                data.setBookingId(
                        String.valueOf(
                                payment.get("bookingId")));

                data.setDoctorId(
                        String.valueOf(
                                payment.get("doctorId")));

                data.setDoctorName(
                        String.valueOf(
                                payment.get("doctorName")));

                data.setTherapistId(
                        String.valueOf(
                                payment.get("therapistId")));

                data.setTherapistName(
                        String.valueOf(
                                payment.get("therapistName")));

                data.setTherapistRecordId(
                        String.valueOf(
                                payment.get(
                                        "therapistRecordId")));

                String serviceType =
                        String.valueOf(
                                payment.get("serviceType"));

                data.setServiceType(serviceType);

             // ================= COUNTS =================

                int totalSessions = 0;
                int completedSessions = 0;

                List<ServiceInfo> service =
                        new ArrayList<>();

                List<Map<String, Object>> therapyWithSessions =
                        (List<Map<String, Object>>)
                                payment.get("therapyWithSessions");

                if (therapyWithSessions != null
                        && !therapyWithSessions.isEmpty()) {

                    // ================= PACKAGE =================

                    if ("PACKAGE".equalsIgnoreCase(serviceType)) {

                        for (Map<String, Object> pkg : therapyWithSessions) {

                            String packageId =
                                    String.valueOf(
                                            pkg.get("packageId"));

                            String packageName =
                                    String.valueOf(
                                            pkg.get("packageName"));

                            if (packageName != null
                                    && !"null".equalsIgnoreCase(packageName)) {

                                ServiceInfo info =
                                        new ServiceInfo();

                                info.setServiceId(packageId);

                                info.setServiceName(packageName);

                                service.add(info);
                            }

                            List<Map<String, Object>> programs =
                                    (List<Map<String, Object>>)
                                            pkg.get("programs");

                            if (programs == null
                                    || programs.isEmpty()) {

                                continue;
                            }

                            for (Map<String, Object> program : programs) {

                                List<Map<String, Object>> therapyData =
                                        (List<Map<String, Object>>)
                                                program.get("therapyData");

                                if (therapyData == null
                                        || therapyData.isEmpty()) {

                                    continue;
                                }

                                for (Map<String, Object> therapy : therapyData) {

                                    List<Map<String, Object>> exercises =
                                            (List<Map<String, Object>>)
                                                    therapy.get("exercises");

                                    if (exercises == null
                                            || exercises.isEmpty()) {

                                        continue;
                                    }

                                    for (Map<String, Object> exercise : exercises) {

                                        List<Map<String, Object>> sessions =
                                                (List<Map<String, Object>>)
                                                        exercise.get("sessions");

                                        if (sessions == null
                                                || sessions.isEmpty()) {

                                            continue;
                                        }

                                        totalSessions += sessions.size();

                                        for (Map<String, Object> session : sessions) {

                                            String status =
                                                    String.valueOf(
                                                            session.get("status"));

                                            if ("Completed"
                                                    .equalsIgnoreCase(status)) {

                                                completedSessions++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= PROGRAM =================
                    // Note: for PROGRAM type, each element of
                    // therapyWithSessions IS a program directly
                    // (no "programs" wrapper key at this level)

                    else if ("PROGRAM".equalsIgnoreCase(serviceType)) {

                        for (Map<String, Object> program : therapyWithSessions) {

                            String programId =
                                    String.valueOf(
                                            program.get("programId"));

                            String programName =
                                    String.valueOf(
                                            program.get("programName"));

                            if (programName != null
                                    && !"null".equalsIgnoreCase(programName)) {

                                ServiceInfo info =
                                        new ServiceInfo();

                                info.setServiceId(programId);

                                info.setServiceName(programName);

                                service.add(info);
                            }

                            List<Map<String, Object>> therapyData =
                                    (List<Map<String, Object>>)
                                            program.get("therapyData");

                            if (therapyData == null
                                    || therapyData.isEmpty()) {

                                continue;
                            }

                            for (Map<String, Object> therapy : therapyData) {

                                List<Map<String, Object>> exercises =
                                        (List<Map<String, Object>>)
                                                therapy.get("exercises");

                                if (exercises == null
                                        || exercises.isEmpty()) {

                                    continue;
                                }

                                for (Map<String, Object> exercise : exercises) {

                                    List<Map<String, Object>> sessions =
                                            (List<Map<String, Object>>)
                                                    exercise.get("sessions");

                                    if (sessions == null
                                            || sessions.isEmpty()) {

                                        continue;
                                    }

                                    totalSessions += sessions.size();

                                    for (Map<String, Object> session : sessions) {

                                        String status =
                                                String.valueOf(
                                                        session.get("status"));

                                        if ("Completed"
                                                .equalsIgnoreCase(status)) {

                                            completedSessions++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= THERAPY =================
                    // Note: for THERAPY type, each element of
                    // therapyWithSessions IS a therapy directly
                    // (no "programs"/"therapyData" wrapper at this level)

                    else if ("THERAPY".equalsIgnoreCase(serviceType)) {

                        for (Map<String, Object> therapy : therapyWithSessions) {

                            String therapyId =
                                    String.valueOf(
                                            therapy.get("therapyId"));

                            String therapyName =
                                    String.valueOf(
                                            therapy.get("therapyName"));

                            if (therapyName != null
                                    && !"null".equalsIgnoreCase(therapyName)) {

                                ServiceInfo info =
                                        new ServiceInfo();

                                info.setServiceId(therapyId);

                                info.setServiceName(therapyName);

                                service.add(info);
                            }

                            List<Map<String, Object>> exercises =
                                    (List<Map<String, Object>>)
                                            therapy.get("exercises");

                            if (exercises == null
                                    || exercises.isEmpty()) {

                                continue;
                            }

                            for (Map<String, Object> exercise : exercises) {

                                List<Map<String, Object>> sessions =
                                        (List<Map<String, Object>>)
                                                exercise.get("sessions");

                                if (sessions == null
                                        || sessions.isEmpty()) {

                                    continue;
                                }

                                totalSessions += sessions.size();

                                for (Map<String, Object> session : sessions) {

                                    String status =
                                            String.valueOf(
                                                    session.get("status"));

                                    if ("Completed"
                                            .equalsIgnoreCase(status)) {

                                        completedSessions++;
                                    }
                                }
                            }
                        }
                    }

                    // ================= EXERCISE =================
                    // Note: for EXERCISE type, each element of
                    // therapyWithSessions IS an exercise directly
                    // (fully flat, sessions right underneath)

                    else if ("EXERCISE".equalsIgnoreCase(serviceType)) {

                        for (Map<String, Object> exercise : therapyWithSessions) {

                            String exerciseId =
                                    String.valueOf(
                                            exercise.get("exerciseId"));

                            String exerciseName =
                                    String.valueOf(
                                            exercise.get("exerciseName"));

                            if (exerciseName != null
                                    && !"null".equalsIgnoreCase(exerciseName)) {

                                ServiceInfo info =
                                        new ServiceInfo();

                                info.setServiceId(exerciseId);

                                info.setServiceName(exerciseName);

                                service.add(info);
                            }

                            List<Map<String, Object>> sessions =
                                    (List<Map<String, Object>>)
                                            exercise.get("sessions");

                            if (sessions == null
                                    || sessions.isEmpty()) {

                                continue;
                            }

                            totalSessions += sessions.size();

                            for (Map<String, Object> session : sessions) {

                                String status =
                                        String.valueOf(
                                                session.get("status"));

                                if ("Completed"
                                        .equalsIgnoreCase(status)) {

                                    completedSessions++;
                                }
                            }
                        }
                    }
                }
                // ================= REMOVE DUPLICATES =================

                service =
                        service.stream()
                                .collect(
                                        Collectors.collectingAndThen(
                                                Collectors.toMap(
                                                        ServiceInfo::getServiceId,
                                                        s -> s,
                                                        (a, b) -> a),
                                                m -> new ArrayList<>(
                                                        m.values())));

                data.setService(service);

                // ================= FINAL COUNTS =================

                data.setTotalNoOfSessions(
                        totalSessions);

                data.setNoOfSessionsCompleted(
                        completedSessions);
             // ================= HALF/FULL COMPLETED =================

                boolean isFullCompleted =
                        totalSessions > 0
                        && completedSessions == totalSessions;

                boolean isHalfCompleted =
                        totalSessions > 0
                        && !isFullCompleted
                        && completedSessions >= Math.ceil(totalSessions / 2.0);

                data.setHalfSessionsCompleted(isHalfCompleted);
                data.setFullSessionsCompleted(isFullCompleted);

                
                result.add(data);
            }
                
            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage("Feedback details fetched successfully");
            response.setData(result);

            return response;

        } catch (FeignException.BadRequest ex) {

            response.setSuccess(false);
            response.setMessage("Payment service returned no data");
            response.setData(null);

            return response;

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(400);
            response.setMessage(e.getMessage());
            response.setData(null);

            return response;
        }
    }
    private FeedbackDetails mapToEntity(
            FeedbackDetailsDTO dto) {

        FeedbackDetails entity =
                new FeedbackDetails();

        entity.setId(dto.getId());
        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());
        
        entity.setPatientId(dto.getPatientId());
        entity.setPatientName(dto.getPatientName());
        entity.setMobileNumber(dto.getMobileNumber());

        entity.setBookingId(dto.getBookingId());

        entity.setDoctorId(dto.getDoctorId());
        entity.setDoctorName(dto.getDoctorName());

        entity.setTherapistId(dto.getTherapistId());
        entity.setTherapistName(dto.getTherapistName());
        entity.setTherapistRecordId(
                dto.getTherapistRecordId());

        entity.setStaffId(dto.getStaffId());
        entity.setStaffName(dto.getStaffName());
        entity.setRating(dto.getRating());
        entity.setServiceType(dto.getServiceType());
        entity.setService(dto.getService());
        entity.setHalfNotificationSent(dto.isHalfNotificationSent());
        entity.setFullNotificationSent(dto.isFullNotificationSent());
        entity.setTotalNoOfSessions(
                dto.getTotalNoOfSessions());

        entity.setNoOfSessionsCompleted(
                dto.getNoOfSessionsCompleted());

        entity.setHalfSessionsCompleted(
                dto.isHalfSessionsCompleted());

        entity.setFullSessionsCompleted(
                dto.isFullSessionsCompleted());

        entity.setWhatWentWell(
                dto.getWhatWentWell());

        entity.setImprovements(
                dto.getImprovements());
        
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
    private FeedbackDetailsDTO mapToDTO(
            FeedbackDetails entity) {

        FeedbackDetailsDTO dto =
                new FeedbackDetailsDTO();

        dto.setId(entity.getId());
        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());
        dto.setPatientId(entity.getPatientId());
        dto.setPatientName(entity.getPatientName());
        dto.setMobileNumber(entity.getMobileNumber());

        dto.setBookingId(entity.getBookingId());
        dto.setRating(entity.getRating());
        dto.setDoctorId(entity.getDoctorId());
        dto.setDoctorName(entity.getDoctorName());

        dto.setTherapistId(entity.getTherapistId());
        dto.setTherapistName(entity.getTherapistName());
        dto.setTherapistRecordId(
                entity.getTherapistRecordId());

        dto.setStaffId(entity.getStaffId());
        dto.setStaffName(entity.getStaffName());

        dto.setServiceType(entity.getServiceType());
        dto.setService(entity.getService());

        dto.setTotalNoOfSessions(
                entity.getTotalNoOfSessions());

        dto.setNoOfSessionsCompleted(
                entity.getNoOfSessionsCompleted());

        dto.setHalfSessionsCompleted(
                entity.isHalfSessionsCompleted());

        dto.setFullSessionsCompleted(
                entity.isFullSessionsCompleted());

        dto.setWhatWentWell(
                entity.getWhatWentWell());

        dto.setImprovements(
                entity.getImprovements());
        // ================= AUDIT =================

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setHalfNotificationSent(entity.isHalfNotificationSent());
        dto.setFullNotificationSent(entity.isFullNotificationSent());

        return dto;
    }


    @Override
    public Response getAllFeedbacksByClinicIdAndBranchId(
            String clinicId,
            String branchId) {

        Response response = new Response();

        try {

            List<FeedbackDetailsDTO> feedbackList =
                    repository.findByClinicIdAndBranchId(
                                    clinicId,
                                    branchId)
                            .stream()
                            .map(this::mapToDTO)
                            .toList();

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Feedbacks fetched successfully");

            response.setData(feedbackList);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage(e.getMessage());
            response.setData(null);
        }

        return response;
    }
    
//    @Override
//    public Response getDoctorFeedbackSummary(
//            String clinicId,
//            String doctorId) {
//
//        Response response = new Response();
//
//        try {
//
//            List<FeedbackDetails> feedbacks =
//                    repository.findByClinicIdAndDoctorId(
//                            clinicId,
//                            doctorId);
//
//            if (feedbacks.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("No feedback found");
//                return response;
//            }
//
//            DoctorFeedbackSummaryDTO dto =
//                    new DoctorFeedbackSummaryDTO();
//
//            FeedbackDetails first = feedbacks.get(0);
//
//            dto.setClinicId(clinicId);
//            dto.setDoctorId(first.getDoctorId());
//            dto.setDoctorName(first.getDoctorName());
//
//            // Total persons given rating
//            long totalRatedPersons =
//                    feedbacks.stream()
//                            .filter(f ->
//                                    f.getRating() != null
//                                    && !f.getRating().trim().isEmpty())
//                            .count();
//
//            dto.setTotalPatientsRated(totalRatedPersons);
//
//            // Average Rating
//            double avgRating =
//                    feedbacks.stream()
//                            .filter(f ->
//                                    f.getRating() != null
//                                    && !f.getRating().trim().isEmpty())
//                            .mapToDouble(f ->
//                                    Double.parseDouble(f.getRating()))
//                            .average()
//                            .orElse(0.0);
//
//            dto.setAverageRating(
//                    Math.round(avgRating * 100.0) / 100.0);
//
//            // Patients who gave ratings
//            List<PatientRatingDTO> patients =
//                    feedbacks.stream()
//                            .filter(f ->
//                                    f.getRating() != null
//                                    && !f.getRating().trim().isEmpty())
//                            .map(f -> {
//                                PatientRatingDTO patient =
//                                        new PatientRatingDTO();
//
//                                patient.setPatientId(
//                                        f.getPatientId());
//
//                                patient.setPatientName(
//                                        f.getPatientName());
//
//                                patient.setMobileNumber(
//                                        f.getMobileNumber());
//
//                                patient.setRating(
//                                        f.getRating());
//
//                                patient.setWhatWentWell(
//                                        f.getWhatWentWell());
//
//                                patient.setImprovements(
//                                        f.getImprovements());
//
//                                return patient;
//                            })
//                            .toList();
//
//            dto.setPatients(patients);
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage(
//                    "Doctor feedback summary fetched successfully");
//            response.setData(dto);
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//            response.setData(null);
//        }
//
//        return response;
//    }
    
    private void triggerSessionNotificationIfNeeded(
            FeedbackDetails feedback) {

        String bookingId   = feedback.getBookingId();
        String patientName = feedback.getPatientName();
        String mobile      = feedback.getMobileNumber();
        String clinicId    = feedback.getClinicId();

        // ================= FETCH FCM TOKEN USING EXISTING FEIGN =================

        ResponseEntity<Response> clinicResponse =
                adminServiceClient.getClinicById(clinicId);

        if (clinicResponse == null
                || clinicResponse.getBody() == null
                || !clinicResponse.getBody().isSuccess()
                || clinicResponse.getBody().getData() == null) {

            log.warn("Clinic not found | ClinicId: {} | BookingId: {}",
                    clinicId, bookingId);
            return;
        }

        // ================= GET FCM TOKEN FROM RESPONSE =================

        Map<String, Object> clinicData =
                (Map<String, Object>) clinicResponse
                        .getBody().getData();

        String fcmToken =
                String.valueOf(clinicData.get("fcmToken"));

        if (fcmToken == null
                || fcmToken.isBlank()
                || "null".equalsIgnoreCase(fcmToken)) {

            log.warn("FCM token not found | ClinicId: {}",
                    clinicId);
            return;
        }

        // ================= FULL COMPLETED =================

        if (feedback.isFullSessionsCompleted()) {

            pushNotificationService
                    .sendFullSessionNotification(
                            fcmToken,
                            bookingId,
                            patientName,
                            mobile);

        // ================= HALF COMPLETED =================

        } else if (feedback.isHalfSessionsCompleted()) {

            pushNotificationService
                    .sendHalfSessionNotification(
                            fcmToken,
                            bookingId,
                            patientName,
                            mobile);
        }
    }
    @Override
    public void processFeedbackNotification(
            String clinicId,
            String branchId) {

        try {

            log.info(
                    "processFeedbackNotification started | ClinicId:{} | BranchId:{}",
                    clinicId,
                    branchId);

            Response paymentResponse =
                    physiotherapyDoctorFeign.getPayments(
                            clinicId,
                            branchId);

            log.info(
                    "Payment Response : {}",
                    paymentResponse);

            if (paymentResponse == null
                    || !paymentResponse.isSuccess()
                    || paymentResponse.getData() == null) {

                log.warn(
                        "Payment response is empty");

                return;
            }

            List<Map<String, Object>> payments =
                    (List<Map<String, Object>>)
                            paymentResponse.getData();

            log.info(
                    "Payments count : {}",
                    payments.size());

            for (Map<String, Object> payment : payments) {

                FeedbackDetailsDTO data =
                        new FeedbackDetailsDTO();

                data.setClinicId(clinicId);

                data.setBranchId(branchId);

                data.setBookingId(
                        String.valueOf(
                                payment.get("bookingId")));

                data.setPatientId(
                        String.valueOf(
                                payment.get("patientId")));

                CustomerOnbording customer =
                        customerOnboardingRepository
                                .findByPatientIdAndHospitalIdAndBranchId(
                                        data.getPatientId(),
                                        clinicId,
                                        branchId)
                                .orElse(null);

                if (customer != null) {

                    data.setPatientName(
                            customer.getFullName());

                    data.setMobileNumber(
                            customer.getMobileNumber());
                }

                int totalSessions = 0;

                int completedSessions = 0;

                List<Map<String, Object>> therapyWithSessions =
                        (List<Map<String, Object>>)
                                payment.get(
                                        "therapyWithSessions");

                if (therapyWithSessions == null) {

                    log.warn(
                            "therapyWithSessions is null | BookingId:{}",
                            data.getBookingId());

                    continue;
                }

                for (Map<String, Object> pkg
                        : therapyWithSessions) {

                    List<Map<String, Object>> programs =
                            (List<Map<String, Object>>)
                                    pkg.get("programs");

                    if (programs == null) {
                        continue;
                    }

                    for (Map<String, Object> program
                            : programs) {

                        List<Map<String, Object>> therapyData =
                                (List<Map<String, Object>>)
                                        program.get(
                                                "therapyData");

                        if (therapyData == null) {
                            continue;
                        }

                        for (Map<String, Object> therapy
                                : therapyData) {

                            List<Map<String, Object>> exercises =
                                    (List<Map<String, Object>>)
                                            therapy.get(
                                                    "exercises");

                            if (exercises == null) {
                                continue;
                            }

                            for (Map<String, Object> exercise
                                    : exercises) {

                                List<Map<String, Object>> sessions =
                                        (List<Map<String, Object>>)
                                                exercise.get(
                                                        "sessions");

                                if (sessions == null) {
                                    continue;
                                }

                                totalSessions +=
                                        sessions.size();

                                for (Map<String, Object> session
                                        : sessions) {

                                    String status =
                                            String.valueOf(
                                                    session.get(
                                                            "status"));

                                    if ("Completed"
                                            .equalsIgnoreCase(
                                                    status)) {

                                        completedSessions++;
                                    }
                                }
                            }
                        }
                    }
                }

                log.info(
                        "BookingId:{} | Total:{} | Completed:{}",
                        data.getBookingId(),
                        totalSessions,
                        completedSessions);

                checkAndSendNotification(
                        data,
                        totalSessions,
                        completedSessions);
            }

        } catch (Exception e) {

            log.error(
                    "Notification processing failed | ClinicId:{} | BranchId:{}",
                    clinicId,
                    branchId,
                    e);

            e.printStackTrace();
        }
    }
    
    private void checkAndSendNotification(
            FeedbackDetailsDTO data,
            int totalSessions,
            int completedSessions) {

        log.info("Entered checkAndSendNotification");

        boolean isFullCompleted =
                totalSessions > 0
                && completedSessions == totalSessions;

        boolean isHalfCompleted =
                totalSessions > 0
                && !isFullCompleted
                && completedSessions >= Math.ceil(totalSessions / 2.0);

        log.info(
                "Half:{} | Full:{}",
                isHalfCompleted,
                isFullCompleted);

        if (!(isHalfCompleted || isFullCompleted)) {

            log.info("Notification not required");
            return;
        }

        // Fetch existing feedback by bookingId
        FeedbackDetails feedback =
                repository.findByBookingId(data.getBookingId())
                        .orElseGet(() -> mapToEntity(data));

        feedback.setClinicId(data.getClinicId());
        feedback.setBranchId(data.getBranchId());
        feedback.setPatientId(data.getPatientId());
        feedback.setPatientName(data.getPatientName());
        feedback.setMobileNumber(data.getMobileNumber());
        feedback.setBookingId(data.getBookingId());

        feedback.setTotalNoOfSessions(totalSessions);
        feedback.setNoOfSessionsCompleted(completedSessions);

        // Send Half Notification Only Once
        if (isHalfCompleted && !feedback.isHalfNotificationSent()) {

            feedback.setHalfSessionsCompleted(true);

            log.info("Sending Half Session Notification");

            triggerSessionNotificationIfNeeded(feedback);

//            feedback.setHalfNotificationSent(true);

            repository.save(feedback);
        }

        // Send Full Notification Only Once
        if (isFullCompleted && !feedback.isFullNotificationSent()) {

            feedback.setFullSessionsCompleted(true);

            log.info("Sending Full Session Notification");

            triggerSessionNotificationIfNeeded(feedback);

//            feedback.setFullNotificationSent(true);

            repository.save(feedback);
        }
    }
}
    
