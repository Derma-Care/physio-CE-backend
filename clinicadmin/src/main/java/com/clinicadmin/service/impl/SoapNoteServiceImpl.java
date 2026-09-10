package com.clinicadmin.service.impl;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.SoapNoteDTO;
import com.clinicadmin.entity.SoapNote;
import com.clinicadmin.entity.TreatmentSchedule;
import com.clinicadmin.repository.SoapNoteRepository;
import com.clinicadmin.repository.TreatmentScheduleRepository;
import com.clinicadmin.service.S3Service;
import com.clinicadmin.service.SoapNoteService;
import com.clinicadmin.service.TreatmentScheduleService;
import com.clinicadmin.utils.AudioCompressionUtil;
import com.clinicadmin.utils.FileSignatureUtil;
import com.clinicadmin.utils.ImageCompressionUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SoapNoteServiceImpl implements SoapNoteService {

	// SOAP note attachments/audio must be compressed to fit under this,
	// separate from S3Controller's generic per-field size limits which
	// govern the direct presigned-PUT flow used elsewhere.
	private static final long MAX_SIZE_BYTES = 250 * 1024;

	private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
	private static final List<String> AUDIO_EXTENSIONS = List.of("mp3", "wav", "ogg", "m4a", "aac");

	@Autowired
	private SoapNoteRepository soapNoteRepository;
	@Autowired
	private TreatmentScheduleService treatmentScheduleService; // ← NEW

	@Autowired
	private TreatmentScheduleRepository treatmentScheduleRepository;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private ImageCompressionUtil imageCompressionUtil;

	@Autowired
	private AudioCompressionUtil audioCompressionUtil;

	@Autowired
	private FileSignatureUtil fileSignatureUtil;

	// -------------------- CREATE --------------------
	@Override
	public Response createSoapNote(SoapNoteDTO dto) {
		log.info("Create SOAP note request received. patientId={}, doctorId={}", dto.getPatientId(), dto.getDoctorId());
		Response response = new Response();
		try {
			Optional<SoapNote> existingSoapNote = soapNoteRepository.findByClinicIdAndBranchIdAndBookingIdAndPatientId(
					dto.getClinicId(), dto.getBranchId(), dto.getBookingId(), dto.getPatientId());

			if (existingSoapNote.isPresent()) {
				log.warn("SOAP note already exists. bookingId={}, patientId={}", dto.getBookingId(),
						dto.getPatientId());

				response.setSuccess(false);
				response.setMessage("SOAP note already exists.");
				response.setStatus(HttpStatus.CONFLICT.value()); // 409

				return response;
			}
			SoapNote soapNote = mapDtoToEntity(dto, new SoapNote());
			soapNote.setCreatedAt(Instant.now());
			soapNote.setUpdatedAt(Instant.now());

			handleAttachmentUpload(dto, soapNote);
			handleAudioUpload(dto, soapNote);

			SoapNote saved = soapNoteRepository.save(soapNote);
			log.info("SOAP note created successfully. id={}", saved.getId());

			// Auto-create (or self-heal) the treatment schedule for this SOAP note.
			// This must never block/fail the SOAP note creation itself, so any error
			// here is logged and swallowed rather than propagated.
			try {
				Response scheduleResponse = treatmentScheduleService.createFromSoapNote(saved.getClinicId(),
						saved.getBranchId(), saved.getBookingId(), saved.getPatientId());
				if (scheduleResponse == null || !scheduleResponse.isSuccess()) {
					log.warn("Treatment schedule auto-creation did not succeed for soapNoteId={}. message={}",
							saved.getId(), scheduleResponse != null ? scheduleResponse.getMessage() : "null response");
				} else {
					log.info("Treatment schedule auto-created/synced for soapNoteId={}", saved.getId());
				}
			} catch (Exception scheduleEx) {
				log.error("Failed to auto-create treatment schedule for soapNoteId={}: {}", saved.getId(),
						scheduleEx.getMessage(), scheduleEx);
			}

			response.setSuccess(true);
			response.setData(mapEntityToDto(saved));
			response.setMessage("SOAP note created successfully");
			response.setStatus(HttpStatus.CREATED.value());

		} catch (IllegalArgumentException e) {
			log.warn("Validation error while creating SOAP note: {}", e.getMessage());
			response.setSuccess(false);
			response.setMessage(e.getMessage());
			response.setStatus(HttpStatus.BAD_REQUEST.value());

		} catch (Exception e) {
			log.error("Exception while creating SOAP note: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error creating SOAP note: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- UPDATE --------------------
	@Override
	public Response updateSoapNote(String id, SoapNoteDTO dto) {
		log.info("Update SOAP note request received. id={}", id);
		Response response = new Response();
		try {
			Optional<SoapNote> existingOpt = soapNoteRepository.findById(id);
			if (existingOpt.isEmpty()) {
				log.warn("SOAP note not found for update. id={}", id);
				response.setSuccess(false);
				response.setMessage("SOAP note not found with ID: " + id);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}

//			SoapNote existing = existingOpt.get();
//			mapDtoToEntity(dto, existing);
//			existing.setUpdatedAt(Instant.now());
			SoapNote existing = existingOpt.get();

			// NEW: block edits once treatment has actually started (payment made or
			// a real sitting booked). Prevents createFromSoapNote()'s full-overwrite
			// from wiping out live bookings after the fact.
			if (existing.isEditingDisabled()) {
				log.warn("Blocked SOAP note update — editing is disabled. id={}", id);
				response.setSuccess(false);
				response.setMessage("This SOAP note can no longer be edited — treatment has already started "
						+ "(payment made or a sitting booked). Contact support if this needs correcting.");
				response.setStatus(HttpStatus.CONFLICT.value());
				return response;
			}

			mapDtoToEntity(dto, existing);
			existing.setUpdatedAt(Instant.now());

			if (dto.getAttachmentFileKey() != null && !dto.getAttachmentFileKey().isBlank()) {
				String oldKey = existing.getAttachmentFileKey();
				handleAttachmentUpload(dto, existing);
				if (oldKey != null) {
					s3Service.deleteFile(oldKey);
					log.info("Old attachment deleted from S3. key={}", oldKey);
				}
			}

			if (dto.getAudioNoteKey() != null && !dto.getAudioNoteKey().isBlank()) {
				String oldKey = existing.getAudioNoteKey();
				handleAudioUpload(dto, existing);
				if (oldKey != null) {
					s3Service.deleteFile(oldKey);
					log.info("Old audio note deleted from S3. key={}", oldKey);
				}
			}

			SoapNote updated = soapNoteRepository.save(existing);
			log.info("SOAP note updated successfully. id={}", updated.getId());
			try {
				Response scheduleResponse = treatmentScheduleService.createFromSoapNote(updated.getClinicId(),
						updated.getBranchId(), updated.getBookingId(), updated.getPatientId());
				if (scheduleResponse == null || !scheduleResponse.isSuccess()) {
					log.warn("Treatment schedule sync did not succeed for soapNoteId={}. message={}", updated.getId(),
							scheduleResponse != null ? scheduleResponse.getMessage() : "null response");
				}
			} catch (Exception scheduleEx) {
				log.error("Failed to sync treatment schedule for soapNoteId={}: {}", updated.getId(),
						scheduleEx.getMessage(), scheduleEx);
			}
			response.setSuccess(true);
			response.setData(mapEntityToDto(updated));
			response.setMessage("SOAP note updated successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (IllegalArgumentException e) {
			log.warn("Validation error while updating SOAP note id={}: {}", id, e.getMessage());
			response.setSuccess(false);
			response.setMessage(e.getMessage());
			response.setStatus(HttpStatus.BAD_REQUEST.value());

		} catch (Exception e) {
			log.error("Exception while updating SOAP note id={}: {}", id, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error updating SOAP note: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY ID --------------------
	@Override
	public Response getSoapNoteById(String id) {
		log.info("Get SOAP note by id={}", id);
		Response response = new Response();
		try {
			Optional<SoapNote> soapNoteOpt = soapNoteRepository.findById(id);
			if (soapNoteOpt.isEmpty()) {
				log.warn("SOAP note not found. id={}", id);
				response.setSuccess(false);
				response.setMessage("SOAP note not found with ID: " + id);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}
			response.setSuccess(true);
			response.setData(mapEntityToDto(soapNoteOpt.get()));
			response.setMessage("SOAP note retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP note id={}: {}", id, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP note: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET ALL --------------------
	@Override
	public Response getAllSoapNotes() {
		log.info("Get all SOAP notes request received");
		Response response = new Response();
		try {
			List<SoapNote> all = soapNoteRepository.findAll();
			response.setSuccess(true);
			response.setData(all.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(all.isEmpty() ? "No SOAP notes found" : "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching all SOAP notes: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY PATIENT --------------------
	@Override
	public Response getSoapNotesByPatientId(String patientId) {
		log.info("Get SOAP notes by patientId={}", patientId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByPatientId(patientId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(
					notes.isEmpty() ? "No SOAP notes found for this patient" : "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for patientId={}: {}", patientId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY DOCTOR --------------------
	@Override
	public Response getSoapNotesByDoctorId(String doctorId) {
		log.info("Get SOAP notes by doctorId={}", doctorId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByDoctorId(doctorId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(
					notes.isEmpty() ? "No SOAP notes found for this doctor" : "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for doctorId={}: {}", doctorId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY BOOKING --------------------
	@Override
	public Response getSoapNotesByBooking(String bookingId) {
		log.info("Get SOAP notes by bookingId={}", bookingId);
		Response response = new Response();
		try {
			SoapNote notes = soapNoteRepository.findByBookingId(bookingId);
			if (notes != null) {
				response.setSuccess(true);
				response.setData(mapEntityToDto(notes));
				response.setMessage("SOAP notes retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(false);
				response.setMessage("SOAP notes not found");
				response.setStatus(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for bookingId={}: {}", bookingId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- DELETE --------------------
	@Override
	public Response deleteSoapNoteById(String id) {
		log.info("Delete SOAP note request received. id={}", id);
		Response response = new Response();
		try {
			Optional<SoapNote> existingOpt = soapNoteRepository.findById(id);
			if (existingOpt.isEmpty()) {
				log.warn("SOAP note not found for deletion. id={}", id);
				response.setSuccess(false);
				response.setMessage("SOAP note not found with ID: " + id);
				response.setStatus(HttpStatus.NOT_FOUND.value());
				return response;
			}
			SoapNote existing = existingOpt.get();

			if (existing.getAttachmentFileKey() != null) {
				s3Service.deleteFile(existing.getAttachmentFileKey());
				log.info("Attachment deleted from S3. key={}", existing.getAttachmentFileKey());
			}
			if (existing.getAudioNoteKey() != null) {
				s3Service.deleteFile(existing.getAudioNoteKey());
				log.info("Audio note deleted from S3. key={}", existing.getAudioNoteKey());
			}
			Optional<TreatmentSchedule> schedule = treatmentScheduleRepository.findBySoapNoteId(existing.getId());

			if (schedule.isPresent()) {
				treatmentScheduleRepository.delete(schedule.get());
				log.info("Treatment Schedule deleted for soapNoteId={}", existing.getId());
			}
			soapNoteRepository.deleteById(id);
			log.info("SOAP note deleted successfully. id={}", id);

			response.setSuccess(true);
			response.setMessage("SOAP note deleted successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while deleting SOAP note id={}: {}", id, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error deleting SOAP note: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC --------------------
	@Override
	public Response getSoapNotesByClinicId(String clinicId) {
		log.info("Get SOAP notes by clinicId={}", clinicId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByClinicId(clinicId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(
					notes.isEmpty() ? "No SOAP notes found for this clinic" : "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for clinicId={}: {}", clinicId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC + BRANCH --------------------
	@Override
	public Response getSoapNotesByClinicIdAndBranchId(String clinicId, String branchId) {
		log.info("Get SOAP notes by clinicId={}, branchId={}", clinicId, branchId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByClinicIdAndBranchId(clinicId, branchId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(notes.isEmpty() ? "No SOAP notes found for this clinic and branch"
					: "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for clinicId={}, branchId={}: {}", clinicId, branchId,
					e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC + BRANCH + DOCTOR --------------------
	@Override
	public Response getSoapNotesByClinicBranchAndDoctor(String clinicId, String branchId, String doctorId) {
		log.info("Get SOAP notes by clinicId={}, branchId={}, doctorId={}", clinicId, branchId, doctorId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByClinicIdAndBranchIdAndDoctorId(clinicId, branchId,
					doctorId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(notes.isEmpty() ? "No SOAP notes found for this clinic, branch and doctor"
					: "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for clinicId={}, branchId={}, doctorId={}: {}", clinicId,
					branchId, doctorId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC + BRANCH + PATIENT --------------------
	@Override
	public Response getSoapNotesByClinicBranchAndPatient(String clinicId, String branchId, String patientId) {
		log.info("Get SOAP notes by clinicId={}, branchId={}, patientId={}", clinicId, branchId, patientId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByClinicIdAndBranchIdAndPatientId(clinicId, branchId,
					patientId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(notes.isEmpty() ? "No SOAP notes found for this clinic, branch and patient"
					: "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for clinicId={}, branchId={}, patientId={}: {}", clinicId,
					branchId, patientId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC + BRANCH + BOOKING --------------------
	@Override
	public Response getSoapNotesByClinicBranchAndBooking(String clinicId, String branchId, String bookingId) {
		log.info("Get SOAP notes by clinicId={}, branchId={}, bookingId={}", clinicId, branchId, bookingId);
		Response response = new Response();
		try {
			List<SoapNote> notes = soapNoteRepository.findByClinicIdAndBranchIdAndBookingId(clinicId, branchId,
					bookingId);
			response.setSuccess(true);
			response.setData(notes.stream().map(this::mapEntityToDto).collect(Collectors.toList()));
			response.setMessage(notes.isEmpty() ? "No SOAP notes found for this clinic, branch and booking"
					: "SOAP notes retrieved successfully");
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.error("Exception while fetching SOAP notes for clinicId={}, branchId={}, bookingId={}: {}", clinicId,
					branchId, bookingId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP notes: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET BY CLINIC + BRANCH + BOOKING + PATIENT
	// --------------------
	@Override
	public Response getSoapNotesByClinicBranchAndPatient(String clinicId, String branchId, String bookingId,
			String patientId) {

		log.info("Get SOAP note by clinicId={}, branchId={}, bookingId={}, patientId={}", clinicId, branchId, bookingId,
				patientId);

		Response response = new Response();

		try {
			Optional<SoapNote> note = soapNoteRepository.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId,
					branchId, bookingId, patientId);

			if (note.isPresent()) {
				response.setSuccess(true);
				response.setData(mapEntityToDto(note.get()));
				response.setMessage("SOAP note retrieved successfully");
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setSuccess(false);
				response.setData(null);
				response.setMessage("SOAP note not found");
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}

		} catch (Exception e) {
			log.error("Exception while fetching SOAP note: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching SOAP note: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	// -------------------- HELPER: Attachment upload with compression
	// --------------------
	private void handleAttachmentUpload(SoapNoteDTO dto, SoapNote soapNote) {

		if (dto.getAttachmentFileKey() == null || dto.getAttachmentFileKey().isBlank()) {
			return;
		}

		// Save the S3 object key directly
		soapNote.setAttachmentFileKey(dto.getAttachmentFileKey().trim());

		log.info("Attachment file key saved successfully: {}", dto.getAttachmentFileKey());
	}

	// -------------------- HELPER: Audio upload with compression
	// --------------------
	private void handleAudioUpload(SoapNoteDTO dto, SoapNote soapNote) {

		if (dto.getAudioNoteKey() == null || dto.getAudioNoteKey().isBlank()) {
			return;
		}

		// Save the S3 object key directly
		soapNote.setAudioNoteKey(dto.getAudioNoteKey().trim());

		log.info("Audio file key saved successfully: {}", dto.getAudioNoteKey());
	}

	// -------------------- HELPER: base64 decode (tolerates data URI prefixes)
	// --------------------
	private byte[] decodeBase64(String raw) {
		String cleaned = raw.contains(",") ? raw.substring(raw.indexOf(',') + 1) : raw;
		return Base64.getDecoder().decode(cleaned);
	}

	// -------------------- MAPPERS --------------------
	private SoapNote mapDtoToEntity(SoapNoteDTO dto, SoapNote soapNote) {
		soapNote.setPatientId(dto.getPatientId());
		soapNote.setPatientName(dto.getPatientName());
		soapNote.setMobileNumber(dto.getMobileNumber());
		soapNote.setClinicId(dto.getClinicId());
		soapNote.setBranchId(dto.getBranchId());
		soapNote.setBookingId(dto.getBookingId());
		soapNote.setDoctorId(dto.getDoctorId());
		soapNote.setDoctorName(dto.getDoctorName());
		soapNote.setComplaints(dto.getComplaints());
		soapNote.setPackageType(dto.getPackageType());
		soapNote.setPackageId(dto.getPackageId());
		soapNote.setPackageName(dto.getPackageName());
		soapNote.setNumberOfSittings(dto.getNumberOfSittings());
		soapNote.setPackagePrice(dto.getPackagePrice());
		soapNote.setNoteText(dto.getNoteText());
		soapNote.setSessionStartDate(dto.getSessionStartDate());
		soapNote.setSlot(dto.getSlot()); // ← ADD: was never being persisted
		return soapNote;
	}

	private SoapNoteDTO mapEntityToDto(SoapNote soapNote) {
		SoapNoteDTO dto = new SoapNoteDTO();

		dto.setId(soapNote.getId());
		dto.setPatientId(soapNote.getPatientId());
		dto.setPatientName(soapNote.getPatientName());
		dto.setMobileNumber(soapNote.getMobileNumber());
		dto.setClinicId(soapNote.getClinicId());
		dto.setBranchId(soapNote.getBranchId());
		dto.setBookingId(soapNote.getBookingId());
		dto.setDoctorId(soapNote.getDoctorId());
		dto.setDoctorName(soapNote.getDoctorName());
		dto.setComplaints(soapNote.getComplaints());
		dto.setPackageType(soapNote.getPackageType());
		dto.setPackageId(soapNote.getPackageId());
		dto.setPackageName(soapNote.getPackageName());
		dto.setNumberOfSittings(soapNote.getNumberOfSittings());
		dto.setPackagePrice(soapNote.getPackagePrice());
		dto.setNoteText(soapNote.getNoteText());
		dto.setSessionStartDate(soapNote.getSessionStartDate());
		dto.setSlot(soapNote.getSlot());
		dto.setEditingDisabled(soapNote.isEditingDisabled());
		// Generate signed URLs from stored S3 keys
		dto.setAttachmentFileKey(
				soapNote.getAttachmentFileKey() != null ? s3Service.generateSignedUrl(soapNote.getAttachmentFileKey())
						: null);

		dto.setAudioNoteKey(
				soapNote.getAudioNoteKey() != null ? s3Service.generateSignedUrl(soapNote.getAudioNoteKey()) : null);

		dto.setCreatedAt(soapNote.getCreatedAt() != null ? soapNote.getCreatedAt().toString() : null);

		return dto;
	}

}