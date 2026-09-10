package com.clinicadmin.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.DoctorFeedbackDTO;
import com.clinicadmin.dto.DoctorFeedbackSummaryDTO;
import com.clinicadmin.dto.DoctorPatientFeedbackDTO;
import com.clinicadmin.dto.DoctorRatingNotificationDTO;
import com.clinicadmin.dto.HospitalFeedbackDTO;
import com.clinicadmin.dto.PatientFeedbackDTO;
import com.clinicadmin.dto.ReceptionistFeedbackDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherapistFeedbackDTO;
import com.clinicadmin.entity.DoctorFeedback;
import com.clinicadmin.entity.HospitalFeedback;
import com.clinicadmin.entity.PatientFeedback;
import com.clinicadmin.entity.ReceptionistFeedback;
import com.clinicadmin.entity.TherapistFeedback;
import com.clinicadmin.feignclient.NotificationFeign;
import com.clinicadmin.repository.PatientFeedbackRepository;
import com.clinicadmin.service.PatientFeedbackService;

@Service
public class PatientFeedbackServiceImpl implements PatientFeedbackService {

    @Autowired
    private PatientFeedbackRepository repository;
    
    @Autowired
    private NotificationFeign notificationFeign;
    

	// ================= CREATE =================

	@Override
	public Response createFeedback(PatientFeedbackDTO dto) {

		PatientFeedback feedback = mapToEntity(dto);
		feedback.setCreatedAt(LocalDateTime.now());
		feedback.setUpdatedAt(LocalDateTime.now());
		PatientFeedback saved = repository.save(feedback);

		if (dto.getDoctorFeedback() != null && dto.getDoctorFeedback().getTargetId() != null) {

			DoctorRatingNotificationDTO notification = new DoctorRatingNotificationDTO();

			notification.setDoctorId(dto.getDoctorFeedback().getTargetId());

			notification.setPatientName(dto.getPatientName());

			notification.setRating(dto.getDoctorFeedback().getRating());

			notification.setFeedback(dto.getDoctorFeedback().getFeedbackText());
            try {
			notificationFeign.sendDoctorRatingNotification(notification);
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}}
		        try {
	        	if(dto.getTherapistFeedback() != null && dto.getTherapistFeedback().getTargetId() != null ) {
	        	Map<String,String> map = new LinkedHashMap<>();
	        	if(dto.getTherapistFeedback().getTargetId()!=null) {
				map.put("therapistId",dto.getTherapistFeedback().getTargetId());
				map.put("patientName",dto.getPatientName() );
				map.put("feedbackText", dto.getTherapistFeedback().getFeedbackText());
				map.put("rating",dto.getTherapistFeedback().getRating() );
	        	notificationFeign.therapistOverallFeedback(map);}}
	        }catch (Exception e) {}

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedback created successfully");
		response.setStatus(HttpStatus.CREATED.value());
		response.setData(mapToDTO(saved));

		return response;
	}

	// ================= GET ALL =================

	@Override
	public Response getAllFeedbacks() {

		List<PatientFeedbackDTO> list = repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("All feedbacks fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(list);

		return response;
	}

	// ================= GET BY ID =================

	@Override
	public Response getFeedbackById(String id) {

		PatientFeedback feedback = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Feedback not found"));

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedback fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(mapToDTO(feedback));

		return response;
	}

	@Override
	public Response getByClinicIdAndBranchId(String clinicId, String branchId) {

		List<PatientFeedback> feedbackList = repository.findByClinicIdAndBranchId(clinicId, branchId);

		List<PatientFeedbackDTO> list = feedbackList.stream().map(this::mapToDTO).collect(Collectors.toList());

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedbacks fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(list);

		return response;
	}

	@Override
	public Response getByClinicIdAndBranchIdAndPatientId(String clinicId, String branchId, String patientId) {

		List<PatientFeedback> feedbackList = repository.findByClinicIdAndBranchIdAndPatientId(clinicId, branchId,
				patientId);

		List<PatientFeedbackDTO> list = feedbackList.stream().map(this::mapToDTO).collect(Collectors.toList());

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedbacks fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(list);

		return response;
	}

	// ================= UPDATE =================

	@Override
	public Response updateFeedback(String id, PatientFeedbackDTO dto) {

		PatientFeedback existing = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Feedback not found"));

		// ================= BASIC DETAILS =================

		existing.setPatientId(dto.getPatientId());
		existing.setPatientName(dto.getPatientName());
		existing.setPatientPhone(dto.getPatientPhone());
		existing.setDate(dto.getDate());

		// ================= HOSPITAL FEEDBACK =================

		HospitalFeedback hospitalFeedback = new HospitalFeedback();

		hospitalFeedback.setFeedbackText(dto.getHospitalFeedback().getFeedbackText());

		hospitalFeedback.setRating(dto.getHospitalFeedback().getRating());

		existing.setHospitalFeedback(hospitalFeedback);

		// ================= DOCTOR FEEDBACK =================

		DoctorFeedback doctorFeedback = new DoctorFeedback();

		doctorFeedback.setTargetId(dto.getDoctorFeedback().getTargetId());

		doctorFeedback.setFeedbackText(dto.getDoctorFeedback().getFeedbackText());

		doctorFeedback.setRating(dto.getDoctorFeedback().getRating());

		existing.setDoctorFeedback(doctorFeedback);

		// ================= RECEPTIONIST FEEDBACK =================

		ReceptionistFeedback receptionistFeedback = new ReceptionistFeedback();

		receptionistFeedback.setTargetId(dto.getReceptionistFeedback().getTargetId());

		receptionistFeedback.setFeedbackText(dto.getReceptionistFeedback().getFeedbackText());

		receptionistFeedback.setRating(dto.getReceptionistFeedback().getRating());

		existing.setReceptionistFeedback(receptionistFeedback);

		existing.setUpdatedAt(LocalDateTime.now());

		// ================= THERAPIST FEEDBACK =================

		TherapistFeedback therapistFeedback = new TherapistFeedback();

		therapistFeedback.setTargetId(dto.getTherapistFeedback().getTargetId());

		therapistFeedback.setFeedbackText(dto.getTherapistFeedback().getFeedbackText());

		therapistFeedback.setRating(dto.getTherapistFeedback().getRating());

		existing.setTherapistFeedback(therapistFeedback);

		// ================= SAVE =================

		PatientFeedback updated = repository.save(existing);

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedback updated successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(mapToDTO(updated));

		return response;
	}

	// ================= DELETE =================

	@Override
	public Response deleteFeedback(String id) {

		PatientFeedback feedback = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Feedback not found"));

		repository.delete(feedback);

		Response response = new Response();

		response.setSuccess(true);
		response.setMessage("Feedback deleted successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData("Deleted Successfully");

		return response;
	}

	// ================= MAP ENTITY TO DTO =================

	private PatientFeedbackDTO mapToDTO(PatientFeedback feedback) {

		if (feedback == null) {
			return null;
		}

		PatientFeedbackDTO dto = new PatientFeedbackDTO();

		dto.setId(feedback.getId());
		dto.setClinicId(feedback.getClinicId());
		dto.setBranchId(feedback.getBranchId());
		dto.setPatientId(feedback.getPatientId());
		dto.setPatientName(feedback.getPatientName());
		dto.setPatientPhone(feedback.getPatientPhone());
		dto.setDate(feedback.getDate());
		dto.setCreatedAt(feedback.getCreatedAt());
		dto.setUpdatedAt(feedback.getUpdatedAt());

		dto.setHospitalFeedback(mapHospitalToDTO(feedback.getHospitalFeedback()));

		dto.setDoctorFeedback(mapDoctorToDTO(feedback.getDoctorFeedback()));

		dto.setReceptionistFeedback(mapReceptionistToDTO(feedback.getReceptionistFeedback()));

		dto.setTherapistFeedback(mapTherapistToDTO(feedback.getTherapistFeedback()));

		return dto;
	}

	// ================= MAP DTO TO ENTITY =================

	private PatientFeedback mapToEntity(PatientFeedbackDTO dto) {

		if (dto == null) {
			return null;
		}

		PatientFeedback feedback = new PatientFeedback();

		feedback.setId(dto.getId());
		feedback.setClinicId(dto.getClinicId());
		feedback.setBranchId(dto.getBranchId());
		feedback.setPatientId(dto.getPatientId());
		feedback.setPatientName(dto.getPatientName());
		feedback.setPatientPhone(dto.getPatientPhone());
		feedback.setDate(dto.getDate());
		feedback.setCreatedAt(dto.getCreatedAt());
		feedback.setUpdatedAt(dto.getUpdatedAt());

		feedback.setHospitalFeedback(mapHospitalToEntity(dto.getHospitalFeedback()));

		feedback.setDoctorFeedback(mapDoctorToEntity(dto.getDoctorFeedback()));

		feedback.setReceptionistFeedback(mapReceptionistToEntity(dto.getReceptionistFeedback()));

		feedback.setTherapistFeedback(mapTherapistToEntity(dto.getTherapistFeedback()));

		return feedback;
	}

	// ================= UPDATE ENTITY =================

	private void updateEntity(PatientFeedback feedback, PatientFeedbackDTO dto) {

		feedback.setPatientId(dto.getPatientId());
		feedback.setPatientName(dto.getPatientName());
		feedback.setPatientPhone(dto.getPatientPhone());
		feedback.setDate(dto.getDate());
		feedback.setCreatedAt(dto.getCreatedAt());
		feedback.setUpdatedAt(dto.getUpdatedAt());

		feedback.setHospitalFeedback(mapHospitalToEntity(dto.getHospitalFeedback()));

		feedback.setDoctorFeedback(mapDoctorToEntity(dto.getDoctorFeedback()));

		feedback.setReceptionistFeedback(mapReceptionistToEntity(dto.getReceptionistFeedback()));

		feedback.setTherapistFeedback(mapTherapistToEntity(dto.getTherapistFeedback()));
	}

	// ================= HOSPITAL =================

	private HospitalFeedbackDTO mapHospitalToDTO(HospitalFeedback entity) {

		if (entity == null) {
			return null;
		}

		HospitalFeedbackDTO dto = new HospitalFeedbackDTO();

		dto.setFeedbackText(entity.getFeedbackText());
		dto.setRating(entity.getRating());

		return dto;
	}

	private HospitalFeedback mapHospitalToEntity(HospitalFeedbackDTO dto) {

		if (dto == null) {
			return null;
		}

		HospitalFeedback entity = new HospitalFeedback();

		entity.setFeedbackText(dto.getFeedbackText());
		entity.setRating(dto.getRating());

		return entity;
	}

	// ================= DOCTOR =================

	private DoctorFeedbackDTO mapDoctorToDTO(DoctorFeedback entity) {

		if (entity == null) {
			return null;
		}

		DoctorFeedbackDTO dto = new DoctorFeedbackDTO();

		dto.setTargetId(entity.getTargetId());
		dto.setFeedbackText(entity.getFeedbackText());
		dto.setRating(entity.getRating());

		return dto;
	}

	private DoctorFeedback mapDoctorToEntity(DoctorFeedbackDTO dto) {

		if (dto == null) {
			return null;
		}

		DoctorFeedback entity = new DoctorFeedback();

		entity.setTargetId(dto.getTargetId());
		entity.setFeedbackText(dto.getFeedbackText());
		entity.setRating(dto.getRating());

		return entity;
	}

	// ================= RECEPTIONIST =================

	private ReceptionistFeedbackDTO mapReceptionistToDTO(ReceptionistFeedback entity) {

		if (entity == null) {
			return null;
		}

		ReceptionistFeedbackDTO dto = new ReceptionistFeedbackDTO();

		dto.setTargetId(entity.getTargetId());
		dto.setFeedbackText(entity.getFeedbackText());
		dto.setRating(entity.getRating());

		return dto;
	}

	private ReceptionistFeedback mapReceptionistToEntity(ReceptionistFeedbackDTO dto) {

		if (dto == null) {
			return null;
		}

		ReceptionistFeedback entity = new ReceptionistFeedback();

		entity.setTargetId(dto.getTargetId());
		entity.setFeedbackText(dto.getFeedbackText());
		entity.setRating(dto.getRating());

		return entity;
	}

	// ================= THERAPIST =================

	private TherapistFeedbackDTO mapTherapistToDTO(TherapistFeedback entity) {

		if (entity == null) {
			return null;
		}

		TherapistFeedbackDTO dto = new TherapistFeedbackDTO();

		dto.setTargetId(entity.getTargetId());
		dto.setFeedbackText(entity.getFeedbackText());
		dto.setRating(entity.getRating());

		return dto;
	}

	private TherapistFeedback mapTherapistToEntity(TherapistFeedbackDTO dto) {

		if (dto == null) {
			return null;
		}

		TherapistFeedback entity = new TherapistFeedback();

		entity.setTargetId(dto.getTargetId());
		entity.setFeedbackText(dto.getFeedbackText());
		entity.setRating(dto.getRating());

		return entity;
	}

	@Override
	public Response getDoctorFeedbackSummary(String doctorId, String clinicId) {

		List<PatientFeedback> feedbacks = repository.findByClinicIdAndDoctorFeedbackTargetId(clinicId, doctorId);

		List<DoctorPatientFeedbackDTO> patients = feedbacks.stream().map(f -> {
			DoctorPatientFeedbackDTO p = new DoctorPatientFeedbackDTO();
			p.setPatientId(f.getPatientId());
			p.setPatientName(f.getPatientName());
			p.setMobileNumber(f.getPatientPhone());
			p.setRating(f.getDoctorFeedback().getRating());
			p.setWhatWentWell(f.getDoctorFeedback().getFeedbackText());
			return p;
		}).collect(Collectors.toList());

		double averageRating = patients.stream().mapToDouble(p -> {
			try {
				return Double.parseDouble(p.getRating());
			} catch (NumberFormatException e) {
				return 0.0;
			}
		}).average().orElse(0.0);

		averageRating = Math.round(averageRating * 10.0) / 10.0;

		DoctorFeedbackSummaryDTO summary = new DoctorFeedbackSummaryDTO();
		summary.setDoctorId(doctorId);
		summary.setClinicId(clinicId);
		summary.setTotalPatientsRated(patients.size());
		summary.setAverageRating(averageRating);
		summary.setPatients(patients);

		Response response = new Response();
		response.setSuccess(true);
		response.setMessage("Doctor feedback summary fetched successfully");
		response.setStatus(HttpStatus.OK.value());
		response.setData(summary);

		return response;
	}
}