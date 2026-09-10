package com.clinicadmin.service.impl;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.clinicadmin.dto.*;
import com.clinicadmin.entity.*;
import com.clinicadmin.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.service.TreatmentScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class TreatmentScheduleServiceImpl implements TreatmentScheduleService {

	@Autowired
	private DoctorServiceImpl DoctorServiceImpl;

	@Autowired
	private BookingFeign bookingFeign;

	@Autowired
	private CustomerOnboardingRepository onboardingRepository;

	@Autowired
	private HomeVisitTrackingRepository homeVisitTrackingRepository;


	private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)).toFormatter(Locale.ENGLISH);

	@Autowired
	private TreatmentScheduleRepository treatmentScheduleRepository;

	@Autowired
	private SoapNoteRepository soapNoteRepository;

	@Autowired
	private DoctorsRepository doctorsRepository;

	// -------------------- CREATE (frontend sends
	// clinicId/branchId/bookingId/patientId; SoapNote looked up via these)

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// --------------------
	@Override
	public Response createFromSoapNote(String clinicId, String branchId, String bookingId, String patientId) {
		log.info("Creating treatment schedule. clinicId={}, branchId={}, bookingId={}, patientId={}", clinicId,
				branchId, bookingId, patientId);
		Response response = new Response();
		try {
			Optional<SoapNote> soapNoteOpt = soapNoteRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (soapNoteOpt.isEmpty()) {
				log.warn(
						"SOAP note not found while creating treatment schedule. clinicId={}, branchId={}, bookingId={}, patientId={}",
						clinicId, branchId, bookingId, patientId);
				response.setSuccess(false);
				response.setMessage("SOAP note not found for the given clinic, branch, booking and patient");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			SoapNote soapNote = soapNoteOpt.get();
			// Always resolve doctorName fresh from DoctorsRepository — names can change,
			// so we never trust a cached/stale value from SoapNote.
			String doctorName = resolveDoctorName(soapNote.getDoctorId());

			// Per-sitting rate, derived once at creation/overwrite time from
			// packagePrice / numberOfSittings. Used later by addSitting()/deleteSitting()
			// to keep packagePrice in sync as sittings are added/removed manually.
			double resolvedPricePerSitting = (soapNote.getNumberOfSittings() != null
					&& soapNote.getNumberOfSittings() > 0 && soapNote.getPackagePrice() != null)
							? soapNote.getPackagePrice() / soapNote.getNumberOfSittings()
							: (soapNote.getPackagePrice() != null ? soapNote.getPackagePrice() : 0.0);

			// If a schedule already exists for this SOAP note, overwrite it entirely
			// with fresh data — including a fully regenerated sittings list. We do
			// NOT preserve old sitting data (bookings on sitting #2+ are wiped).
			Optional<TreatmentSchedule> existingOpt = treatmentScheduleRepository.findBySoapNoteId(soapNote.getId());
			if (existingOpt.isPresent()) {
				TreatmentSchedule existing = existingOpt.get();

				existing.setPatientId(soapNote.getPatientId());
				existing.setPatientName(soapNote.getPatientName());
				existing.setMobileNumber(soapNote.getMobileNumber());
				existing.setClinicId(soapNote.getClinicId());
				existing.setBranchId(soapNote.getBranchId());
				existing.setBookingId(soapNote.getBookingId());
				existing.setDoctorId(soapNote.getDoctorId());
				existing.setDoctorName(doctorName);
				existing.setComplaints(soapNote.getComplaints());
				existing.setPackageType(soapNote.getPackageType());
				existing.setPackageId(soapNote.getPackageId());
				existing.setPackageName(soapNote.getPackageName());
				existing.setNumberOfSittings(soapNote.getNumberOfSittings());
				existing.setSessionStartDate(soapNote.getSessionStartDate());
				existing.setPackagePrice(soapNote.getPackagePrice());
				existing.setPricePerSitting(resolvedPricePerSitting);
				existing.setSittings(generateSittings(soapNote, doctorName)); // full regenerate
				existing.setUpdatedAt(Instant.now());

				TreatmentSchedule saved = treatmentScheduleRepository.save(existing);
				log.info("Treatment schedule overwritten with latest SOAP note data. scheduleId={}, soapNoteId={}",
						saved.getId(), soapNote.getId());

				// If sitting #1 starts today, reflect "In-progress" on the booking side too.
				if (saved.getSittings() != null && !saved.getSittings().isEmpty()) {
					Sittings firstSitting = saved.getSittings().get(0);
					if ("In-progress".equals(firstSitting.getTreatmentStatus())) {
						syncAppointmentStatusToBooking(firstSitting.getBookingId(), firstSitting.getTreatmentStatus());
					}
				}

				response.setSuccess(true);
				response.setData(mapEntityToDto(saved));
				response.setMessage("Treatment schedule updated successfully");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			// No existing schedule — create a new one
			TreatmentSchedule schedule = TreatmentSchedule.builder().soapNoteId(soapNote.getId())
					.patientId(soapNote.getPatientId()).patientName(soapNote.getPatientName())
					.mobileNumber(soapNote.getMobileNumber()).clinicId(soapNote.getClinicId())
					.branchId(soapNote.getBranchId()).bookingId(soapNote.getBookingId())
					.doctorId(soapNote.getDoctorId()).doctorName(doctorName).complaints(soapNote.getComplaints())
					.packageType(soapNote.getPackageType()).packageId(soapNote.getPackageId())
					.packageName(soapNote.getPackageName()).numberOfSittings(soapNote.getNumberOfSittings())
					.sessionStartDate(soapNote.getSessionStartDate()).packagePrice(soapNote.getPackagePrice())
					.pricePerSitting(resolvedPricePerSitting).sittings(generateSittings(soapNote, doctorName))
					.createdAt(Instant.now()).updatedAt(Instant.now()).build();

			TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
			log.info("Treatment schedule created successfully. scheduleId={}, soapNoteId={}", saved.getId(),
					soapNote.getId());
			// If sitting #1 starts today, its status is "In-progress" — reflect that on the
			// booking side too.
			if (saved.getSittings() != null && !saved.getSittings().isEmpty()) {
				Sittings firstSitting = saved.getSittings().get(0);
				if ("In-progress".equals(firstSitting.getTreatmentStatus())) {
					syncAppointmentStatusToBooking(firstSitting.getBookingId(), firstSitting.getTreatmentStatus());
				}
			}
			response.setSuccess(true);
			response.setData(mapEntityToDto(saved));
			response.setMessage("Treatment schedule created successfully");
			response.setStatus(HttpStatus.CREATED.value());

		} catch (Exception e) {
			log.error("Failed to create treatment schedule. clinicId={}, branchId={}, bookingId={}, patientId={}: {}",
					clinicId, branchId, bookingId, patientId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error creating treatment schedule: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET --------------------
	@Override
	public Response getTreatmentSchedule(String clinicId, String branchId, String bookingId, String patientId) {
		log.info("Get treatment schedule. clinicId={}, branchId={}, bookingId={}, patientId={}", clinicId, branchId,
				bookingId, patientId);
		Response response = new Response();
		try {
			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (scheduleOpt.isEmpty()) {
				log.warn("No treatment schedule found. clinicId={}, branchId={}, bookingId={}, patientId={}", clinicId,
						branchId, bookingId, patientId);
				response.setSuccess(false);
				response.setMessage("No treatment schedule found for the given clinic, branch, booking and patient");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();

			response.setSuccess(true);
			response.setData(mapEntityToDto(schedule));
			response.setMessage("Treatment schedule retrieved successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Exception while fetching treatment schedule: {}", e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error fetching treatment schedule: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- GET (duplicate route kept for compatibility; now returns
	// DTO consistently) --------------------
	@Override
	public Response getTreatmentScheduleUsingClinicIdBranchId(String clinicId, String branchId, String bookingId,
			String patientId) {
		Response response = new Response();
		try {
			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (scheduleOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("No treatment schedule found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();

			if (schedule.getSittings() != null) {
				schedule.getSittings().forEach(s -> {
					if (s.getDate() != null && !s.getDate().isBlank()) {
						s.setTreatmentStatus(computeStatus(s.getDate()));
					}
				});
			}

			response.setSuccess(true);
			response.setData(mapEntityToDto(schedule));
			response.setMessage("Treatment schedule retrieved successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Error fetching treatment schedule", e);
			response.setSuccess(false);
			response.setMessage("Error fetching treatment schedule: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- BOOK A SPECIFIC SITTING --------------------
	@Override
	public Response bookSitting(String scheduleId, String sittingsId, Integer sittingNumber, String doctorId,
			String patientId,String condition, String date, String slot, String bookingId, List<PhysioMaxTreatedDoctor> treatedBy,
			String treatmentPlan, String visitType) {
		log.info("Book sitting request. scheduleId={}, sittingsId={}, sittingNumber={}, doctorId={}, bookingId={}",
				scheduleId, sittingsId, sittingNumber, doctorId, bookingId);
		Response response = new Response();
		try {
			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository.findById(scheduleId);
			if (scheduleOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Treatment schedule not found with ID: " + scheduleId);
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();

			if (schedule.getPatientId() != null && !schedule.getPatientId().equals(patientId)) {
				log.warn("PatientId mismatch. scheduleId={}, expectedPatientId={}, requestPatientId={}", scheduleId,
						schedule.getPatientId(), patientId);
				response.setSuccess(false);
				response.setMessage("This treatment schedule does not belong to the given patient");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			if (schedule.getSittings() == null || schedule.getSittings().isEmpty()) {
				response.setSuccess(false);
				response.setMessage("No sittings exist for this treatment schedule");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			Optional<Sittings> targetOpt;
			if (sittingsId != null && !sittingsId.isBlank()) {
				targetOpt = schedule.getSittings().stream().filter(s -> sittingsId.equals(s.getSittingsId()))
						.findFirst();
			} else if (sittingNumber != null) {
				targetOpt = schedule.getSittings().stream().filter(s -> sittingNumber.equals(s.getSittingNumber()))
						.findFirst();
			} else {
				response.setSuccess(false);
				response.setMessage("Either sittingsId or sittingNumber must be provided");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			if (targetOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage(sittingsId != null ? "Sitting with ID " + sittingsId + " not found"
						: "Sitting number " + sittingNumber + " not found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			Sittings target = targetOpt.get();

			target.setDoctorId(doctorId);
			target.setDoctorName(resolveDoctorName(doctorId));
			target.setDate(date);
			target.setSlot(slot);
			target.setCondition(condition);
			target.setBookingId(bookingId);
			target.setTreatmentStatus(computeStatus(date));
			if (treatedBy != null) {
				target.setTreatedBy(treatedBy);
			}
			// NEW: set from frontend request
			if (treatmentPlan != null && !treatmentPlan.isBlank()) {
				target.setTreatmentPlan(treatmentPlan);
			}
			if (visitType != null && !visitType.isBlank()) {
				target.setVisitType(visitType);
			}

			schedule.setUpdatedAt(Instant.now());
			TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
			try {
				DoctorServiceImpl.makingFalseDoctorSlot(target.getDoctorId(), schedule.getBranchId(), target.getDate(),
						target.getSlot());
				DoctorServiceImpl.updateSlot(target.getDoctorId(), schedule.getBranchId(), date, slot);
			} catch (Exception e) {
			}
			log.info("Sitting booked successfully. scheduleId={}, sittingsId={}, sittingNumber={}, bookingId={}",
					scheduleId, target.getSittingsId(), target.getSittingNumber(), bookingId);
			syncBookingServiceForSitting(saved, target, patientId);
			syncEditingDisabledIfSittingBooked(saved, target);

			response.setSuccess(true);
			response.setData(mapEntityToDto(saved));
			response.setMessage("Sitting booked successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Exception while booking sitting. scheduleId={}: {}", scheduleId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error booking sitting: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}
//	@Override
//	public Response bookSitting(String scheduleId, String sittingsId, Integer sittingNumber, String doctorId,
//	                            String patientId, String date, String slot, String bookingId,
//	                            List<PhysioMaxTreatedDoctor> treatedBy) {
//	    log.info("Book sitting request. scheduleId={}, sittingsId={}, sittingNumber={}, doctorId={}, bookingId={}",
//	            scheduleId, sittingsId, sittingNumber, doctorId, bookingId);
//	    Response response = new Response();
//	    try {
//	        Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository.findById(scheduleId);
//	        if (scheduleOpt.isEmpty()) {
//	            response.setSuccess(false);
//	            response.setMessage("Treatment schedule not found with ID: " + scheduleId);
//	            response.setStatus(HttpStatus.OK.value());
//	            return response;
//	        }
//
//	        TreatmentSchedule schedule = scheduleOpt.get();
//
//	        if (schedule.getPatientId() != null && !schedule.getPatientId().equals(patientId)) {
//	            log.warn("PatientId mismatch. scheduleId={}, expectedPatientId={}, requestPatientId={}", scheduleId,
//	                    schedule.getPatientId(), patientId);
//	            response.setSuccess(false);
//	            response.setMessage("This treatment schedule does not belong to the given patient");
//	            response.setStatus(HttpStatus.BAD_REQUEST.value());
//	            return response;
//	        }
//
//	        if (schedule.getSittings() == null || schedule.getSittings().isEmpty()) {
//	            response.setSuccess(false);
//	            response.setMessage("No sittings exist for this treatment schedule");
//	            response.setStatus(HttpStatus.BAD_REQUEST.value());
//	            return response;
//	        }
//
//	        // Prefer sittingsId (stable UUID) when provided; fall back to sittingNumber
//	        // for backward compatibility with older callers that don't send it yet.
//	        Optional<Sittings> targetOpt;
//	        if (sittingsId != null && !sittingsId.isBlank()) {
//	            targetOpt = schedule.getSittings().stream()
//	                    .filter(s -> sittingsId.equals(s.getSittingsId())).findFirst();
//	        } else if (sittingNumber != null) {
//	            targetOpt = schedule.getSittings().stream()
//	                    .filter(s -> sittingNumber.equals(s.getSittingNumber())).findFirst();
//	        } else {
//	            response.setSuccess(false);
//	            response.setMessage("Either sittingsId or sittingNumber must be provided");
//	            response.setStatus(HttpStatus.BAD_REQUEST.value());
//	            return response;
//	        }
//
//	        if (targetOpt.isEmpty()) {
//	            response.setSuccess(false);
//	            response.setMessage(sittingsId != null
//	                    ? "Sitting with ID " + sittingsId + " not found"
//	                    : "Sitting number " + sittingNumber + " not found");
//	            response.setStatus(HttpStatus.OK.value());
//	            return response;
//	        }
//
//	        Sittings target = targetOpt.get();
//
//	        target.setDoctorId(doctorId);
//	        target.setDoctorName(resolveDoctorName(doctorId));
//	        target.setDate(date);
//	        target.setSlot(slot);
//	        target.setBookingId(bookingId);
//	        target.setTreatmentStatus(computeStatus(date));
//	        if (treatedBy != null) {
//	            target.setTreatedBy(treatedBy);
//	        }
//
//	        schedule.setUpdatedAt(Instant.now());
//	        TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
//	        try {
//	            DoctorServiceImpl.makingFalseDoctorSlot(target.getDoctorId(), schedule.getBranchId(), target.getDate(),
//	                    target.getSlot());
//	            DoctorServiceImpl.updateSlot(target.getDoctorId(), schedule.getBranchId(), date, slot);
//	        } catch (Exception e) {
//	        }
//	        log.info("Sitting booked successfully. scheduleId={}, sittingsId={}, sittingNumber={}, bookingId={}",
//	                scheduleId, target.getSittingsId(), target.getSittingNumber(), bookingId);
//	        syncBookingServiceForSitting(saved, target, patientId);
//	        syncEditingDisabledIfSittingBooked(saved, target);
//
//	        response.setSuccess(true);
//	        response.setData(mapEntityToDto(saved));
//	        response.setMessage("Sitting booked successfully");
//	        response.setStatus(HttpStatus.OK.value());
//
//	    } catch (Exception e) {
//	        log.error("Exception while booking sitting. scheduleId={}: {}", scheduleId, e.getMessage(), e);
//	        response.setSuccess(false);
//	        response.setMessage("Error booking sitting: " + e.getMessage());
//	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//	    }
//	    return response;
//	}

	// -------------------- NEW HELPER: lock SoapNote editing once a sitting
	// #2+ is fully booked (doctorId, doctorName, date, slot all present).
	// This mirrors PaymentServiceImpl's computeEditingDisabledFlag() logic
	// but is triggered from the booking side, since booking a sitting doesn't
	// necessarily go through a payment call. Sitting #1 is skipped — it
	// auto-carries the SOAP note's session start date/slot and isn't a
	// deliberate booking action. Only ever sets true, never resets to false
	// here (that reset, if ever needed, belongs to a separate "reopen" flow).
	// --------------------
	private void syncEditingDisabledIfSittingBooked(TreatmentSchedule schedule, Sittings bookedSitting) {
		try {
			if (bookedSitting.getSittingNumber() == null || bookedSitting.getSittingNumber() == 1) {
				return; // sitting #1 never triggers the lock on its own
			}
			boolean doctorIdSet = bookedSitting.getDoctorId() != null && !bookedSitting.getDoctorId().isBlank();
			boolean doctorNameSet = bookedSitting.getDoctorName() != null && !bookedSitting.getDoctorName().isBlank();
			boolean dateSet = bookedSitting.getDate() != null && !bookedSitting.getDate().isBlank();
			boolean slotSet = bookedSitting.getSlot() != null && !bookedSitting.getSlot().isBlank();

			if (!(doctorIdSet && doctorNameSet && dateSet && slotSet)) {
				return; // not fully booked yet
			}

			Optional<SoapNote> soapNoteOpt = soapNoteRepository.findByClinicIdAndBranchIdAndBookingIdAndPatientId(
					schedule.getClinicId(), schedule.getBranchId(), schedule.getBookingId(), schedule.getPatientId());
			if (soapNoteOpt.isPresent()) {
				SoapNote soapNote = soapNoteOpt.get();
				if (!soapNote.isEditingDisabled()) {
					soapNote.setEditingDisabled(true);
					soapNoteRepository.save(soapNote);
					log.info(
							"editingDisabled set true on SoapNote via sitting booking. soapNoteId={}, sittingNumber={}",
							soapNote.getId(), bookedSitting.getSittingNumber());
				}

			} else {
				log.warn(
						"Could not find matching SoapNote to sync editingDisabled. clinicId={}, branchId={}, "
								+ "bookingId={}, patientId={}",
						schedule.getClinicId(), schedule.getBranchId(), schedule.getBookingId(),
						schedule.getPatientId());
			}
		} catch (Exception e) {
			// best-effort only, must never fail the booking itself
			log.warn("Failed to sync editingDisabled after booking sitting: {}", e.getMessage());
		}
	}
	// -------------------- BOOK A SPECIFIC SITTING --------------------
//	@Override
//	public Response bookSitting(String scheduleId, Integer sittingNumber, String doctorId, String patientId,
//			String date, String slot, String bookingId) {
//		log.info("Book sitting request. scheduleId={}, sittingNumber={}, doctorId={}, bookingId={}", scheduleId,
//				sittingNumber, doctorId, bookingId);
//		Response response = new Response();
//		try {
//			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository.findById(scheduleId);
//			if (scheduleOpt.isEmpty()) {
//				response.setSuccess(false);
//				response.setMessage("Treatment schedule not found with ID: " + scheduleId);
//				response.setStatus(HttpStatus.OK.value());
//				return response;
//			}
//
//			TreatmentSchedule schedule = scheduleOpt.get();
//
//			if (schedule.getPatientId() != null && !schedule.getPatientId().equals(patientId)) {
//				log.warn("PatientId mismatch. scheduleId={}, expectedPatientId={}, requestPatientId={}", scheduleId,
//						schedule.getPatientId(), patientId);
//				response.setSuccess(false);
//				response.setMessage("This treatment schedule does not belong to the given patient");
//				response.setStatus(HttpStatus.BAD_REQUEST.value());
//				return response;
//			}
//
//			if (schedule.getSittings() == null || schedule.getSittings().isEmpty()) {
//				response.setSuccess(false);
//				response.setMessage("No sittings exist for this treatment schedule");
//				response.setStatus(HttpStatus.BAD_REQUEST.value());
//				return response;
//			}
//
//			Optional<Sittings> targetOpt = schedule.getSittings().stream()
//					.filter(s -> sittingNumber.equals(s.getSittingNumber())).findFirst();
//
//			if (targetOpt.isEmpty()) {
//				response.setSuccess(false);
//				response.setMessage("Sitting number " + sittingNumber + " not found");
//				response.setStatus(HttpStatus.OK.value());
//				return response;
//			}
//
//			Sittings target = targetOpt.get();
//
//			target.setDoctorId(doctorId);
//			target.setDoctorName(resolveDoctorName(doctorId));
//			target.setDate(date);
//			target.setSlot(slot);
//			target.setBookingId(bookingId);
//			target.setTreatmentStatus(computeStatus(date));
//
//			schedule.setUpdatedAt(Instant.now());
//			TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
//			try {
//				DoctorServiceImpl.makingFalseDoctorSlot(target.getDoctorId(), schedule.getBranchId(), target.getDate(),
//						target.getSlot());
//				DoctorServiceImpl.updateSlot(target.getDoctorId(), schedule.getBranchId(), date, slot);
//			} catch (Exception e) {
//			}
//			log.info("Sitting booked successfully. scheduleId={}, sittingNumber={}, bookingId={}", scheduleId,
//					sittingNumber, bookingId);
//			syncBookingServiceForSitting(saved, target, patientId);
//			response.setSuccess(true);
//			response.setData(mapEntityToDto(saved));
//			response.setMessage("Sitting booked successfully");
//			response.setStatus(HttpStatus.OK.value());
//
//		} catch (Exception e) {
//			log.error("Exception while booking sitting. scheduleId={}: {}", scheduleId, e.getMessage(), e);
//			response.setSuccess(false);
//			response.setMessage("Error booking sitting: " + e.getMessage());
//			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//		return response;
//	}

	// -------------------- ADD A NEW SITTING to an existing schedule
	// (manual "+" action — e.g. patient booked as a single sitting, comes
	// back later and wants another single visit added to the same record).
	// This does NOT touch existing sittings and does NOT go through the
	// SOAP note sync flow — it's a pure append. Also recomputes packagePrice
	// from pricePerSitting × new count, so per-session pricing scales up.
	// --------------------
	@Override
	public Response addSitting(String clinicId, String branchId, String bookingId, String patientId) {
		log.info("Add sitting request. clinicId={}, branchId={}, bookingId={}, patientId={}", clinicId, branchId,
				bookingId, patientId);
		Response response = new Response();
		try {
			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (scheduleOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("No treatment schedule found for the given clinic, branch, booking and patient");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();
			List<Sittings> sittings = schedule.getSittings();
			if (sittings == null) {
				sittings = new ArrayList<>();
			}

			int nextNumber = sittings.stream().map(Sittings::getSittingNumber).filter(Objects::nonNull)
					.max(Integer::compareTo).orElse(0) + 1;

			Sittings newSitting = Sittings.builder().sittingsId(UUID.randomUUID().toString()).sittingNumber(nextNumber)
					.condition(schedule.getComplaints()).bookingId(schedule.getBookingId()).doctorId(null)
					.doctorName(null).date(null).slot(null).treatmentStatus("Not Started").treatmentPlan(null).visitType(null).build();

			sittings.add(newSitting);
			schedule.setSittings(sittings);
			schedule.setNumberOfSittings(sittings.size());

			// Recompute packagePrice from the per-sitting rate so per-session pricing
			// scales with the new count.
			double rate = schedule.getPricePerSitting() != null ? schedule.getPricePerSitting() : 0.0;
			schedule.setPackagePrice(rate * sittings.size());

			schedule.setUpdatedAt(Instant.now());

			TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
			log.info("New sitting appended. scheduleId={}, sittingNumber={}, newPackagePrice={}", saved.getId(),
					nextNumber, saved.getPackagePrice());

			response.setSuccess(true);
			response.setData(mapEntityToDto(saved));
			response.setMessage("Sitting added successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Failed to add sitting. clinicId={}, branchId={}, bookingId={}, patientId={}: {}", clinicId,
					branchId, bookingId, patientId, e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error adding sitting: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- DELETE A SITTING (undo an accidental "+" click).
	// Keyed by sittingsId (the stable UUID assigned at creation) rather than
	// sittingNumber, since sittingNumber can shift after a delete/renumber.
	// Only allows deleting a sitting that hasn't been booked yet (no
	// doctorId/date/slot). Recomputes packagePrice from pricePerSitting ×
	// new count after removal, and renumbers remaining sittings to stay
	// contiguous (1, 2, 3...).
	// --------------------
	@Override
	public Response deleteSitting(String scheduleId, String sittingsId) {
		log.info("Delete sitting request. scheduleId={}, sittingsId={}", scheduleId, sittingsId);
		Response response = new Response();
		try {
			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository.findById(scheduleId);
			if (scheduleOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Treatment schedule not found with ID: " + scheduleId);
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();
			List<Sittings> sittings = schedule.getSittings();

			if (sittings == null || sittings.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("No sittings exist for this treatment schedule");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			if (sittings.size() <= 1) {
				response.setSuccess(false);
				response.setMessage("Cannot delete the only remaining sitting on this schedule");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			Optional<Sittings> targetOpt = sittings.stream().filter(s -> sittingsId.equals(s.getSittingsId()))
					.findFirst();

			if (targetOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Sitting with ID " + sittingsId + " not found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			Sittings target = targetOpt.get();
			boolean isBooked = (target.getDoctorId() != null && !target.getDoctorId().isBlank())
					|| (target.getDate() != null && !target.getDate().isBlank())
					|| (target.getSlot() != null && !target.getSlot().isBlank());

			if (isBooked) {
				response.setSuccess(false);
				response.setMessage("Cannot delete sitting " + target.getSittingNumber()
						+ " — it is already booked. Cancel the appointment first if this needs removing.");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return response;
			}

			Integer deletedNumber = target.getSittingNumber();
			sittings.removeIf(s -> sittingsId.equals(s.getSittingsId()));

			// Renumber remaining sittings so numbering stays contiguous (1,2,3...),
			// preserving original relative order.
			sittings.sort(Comparator.comparing(Sittings::getSittingNumber, Comparator.nullsLast(Integer::compareTo)));
			int seq = 1;
			for (Sittings s : sittings) {
				s.setSittingNumber(seq++);
			}

			schedule.setSittings(sittings);
			schedule.setNumberOfSittings(sittings.size());

			// Recompute packagePrice from the per-sitting rate after removal.
			double rate = schedule.getPricePerSitting() != null ? schedule.getPricePerSitting() : 0.0;
			schedule.setPackagePrice(rate * sittings.size());

			schedule.setUpdatedAt(Instant.now());

			TreatmentSchedule saved = treatmentScheduleRepository.save(schedule);
			log.info(
					"Sitting deleted successfully. scheduleId={}, sittingsId={}, oldSittingNumber={}, remainingCount={}, newPackagePrice={}",
					saved.getId(), sittingsId, deletedNumber, sittings.size(), saved.getPackagePrice());

			response.setSuccess(true);
			response.setData(mapEntityToDto(saved));
			response.setMessage("Sitting deleted successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Exception while deleting sitting. scheduleId={}, sittingsId={}: {}", scheduleId, sittingsId,
					e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Error deleting sitting: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	// -------------------- NEW HELPER: push sitting booking into booking service
	// --------------------
	private void syncBookingServiceForSitting(TreatmentSchedule schedule, Sittings target, String patientId) {
		try {
			BookingResponse syncReq = new BookingResponse();
			syncReq.setBookingId(target.getBookingId());
			syncReq.setDoctorId(target.getDoctorId());
			syncReq.setDoctorName(target.getDoctorName());
			syncReq.setPatientId(patientId);
			syncReq.setServiceDate(target.getDate());
			syncReq.setServicetime(target.getSlot());
			syncReq.setVisitType("sitting");

			if (schedule.getMobileNumber() != null) {
				syncReq.setMobileNumber(schedule.getMobileNumber());
			}
			if (schedule.getClinicId() != null) {
				syncReq.setClinicId(schedule.getClinicId());
			}
			if (schedule.getBranchId() != null) {
				syncReq.setBranchId(schedule.getBranchId());
			}
			if (schedule.getId() != null) {
				syncReq.setScheduleId(schedule.getId());
			}
			if (schedule.getPackageId() != null) {
				syncReq.setPacakgeId(schedule.getPackageId());
			}
			if (schedule.getPackageName() != null) {
				syncReq.setPackageName(schedule.getPackageName());
			}
			if (schedule.getPackageType() != null) {
				syncReq.setPackageType(schedule.getPackageType());
			}
			if (schedule.getNumberOfSittings() != null) {
				syncReq.setNoOfSittings(schedule.getNumberOfSittings().toString());
			}

			if (target.getSittingsId() != null) {
				syncReq.setSittingsId(target.getSittingsId());
			}

			if (target.getSittingNumber() != null) {
				syncReq.setSittingNumber(target.getSittingNumber());
			}

			bookingFeign.bookService(syncReq);
			log.info("Booking service synced with visitType=sitting. bookingId={}, sittingNumber={}",
					target.getBookingId(), target.getSittingNumber());
		} catch (Exception e) {
			log.warn("Failed to sync booking service (visitType=sitting) for bookingId={}: {}", target.getBookingId(),
					e.getMessage());
		}
	}

	// -------------------- HELPER: resolve doctor name from doctorId (always fresh,
	// never cached) --------------------
	private String resolveDoctorName(String doctorId) {
		if (doctorId == null || doctorId.isBlank())
			return null;
		try {
			Optional<Doctors> doctorOpt = doctorsRepository.findByDoctorId(doctorId);
			return doctorOpt.map(Doctors::getDoctorName).orElse(null);
		} catch (Exception e) {
			log.warn("Could not resolve doctorName for doctorId={}: {}", doctorId, e.getMessage());
			return null;
		}
	}

	// -------------------- HELPER: Generate sittings list --------------------
	private List<Sittings> generateSittings(SoapNote soapNote, String doctorName) {
		int totalSittings = (soapNote.getNumberOfSittings() != null && soapNote.getNumberOfSittings() > 0)
				? soapNote.getNumberOfSittings()
				: 1;

		List<Sittings> sittings = new ArrayList<>();

		for (int i = 1; i <= totalSittings; i++) {
			Sittings.SittingsBuilder builder = Sittings.builder().sittingsId(UUID.randomUUID().toString())
					.sittingNumber(i).condition(soapNote.getComplaints()).bookingId(soapNote.getBookingId());

			if (i == 1) {
				builder.doctorId(soapNote.getDoctorId()).doctorName(doctorName).slot(soapNote.getSlot())
						.date(soapNote.getSessionStartDate())
						.treatmentStatus(computeStatus(soapNote.getSessionStartDate()));
			} else {
				builder.doctorId(null).doctorName(null).date(null).slot(null).treatmentStatus("Not Started");
			}
			sittings.add(builder.build());
		}
		return sittings;
	}

	// -------------------- HELPER: date parsing --------------------
	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.isBlank())
			return null;
		try {
			return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}

	// -------------------- NEW HELPER: push status change back to booking service
	// --------------------
	private void syncAppointmentStatusToBooking(String bookingId, String status) {
		if (bookingId == null || bookingId.isBlank()) {
			return;
		}
		try {
			BookingResponse statusUpdate = new BookingResponse();
			statusUpdate.setBookingId(bookingId);
			statusUpdate.setStatus(status);

			bookingFeign.updateAppointmentBasedOnBookingId(statusUpdate);
			log.info("Booking status synced. bookingId={}, status={}", bookingId, status);
		} catch (Exception e) {
			log.warn("Failed to sync booking status for bookingId={}, status={}: {}", bookingId, status,
					e.getMessage());
		}
	}

	// -------------------- HELPER: status computation --------------------
	private String computeStatus(String dateStr) {
		LocalDate parsed = parseDate(dateStr);
		if (parsed == null) {
			return "Not Started";
		}
		LocalDate today = LocalDate.now(ZONE);
		if (parsed.isEqual(today)) {
			return "In-progress";
		}
		return "Not Started";
	}

	// -------------------- MAPPER: entity -> DTO --------------------
	private TreatmentScheduleDTO mapEntityToDto(TreatmentSchedule schedule) {
		TreatmentScheduleDTO dto = new TreatmentScheduleDTO();
		dto.setId(schedule.getId());
		dto.setSoapNoteId(schedule.getSoapNoteId());
		dto.setPatientId(schedule.getPatientId());
		dto.setPatientName(schedule.getPatientName());
		dto.setMobileNumber(schedule.getMobileNumber());
		dto.setClinicId(schedule.getClinicId());
		dto.setBranchId(schedule.getBranchId());
		dto.setBookingId(schedule.getBookingId());
		dto.setDoctorId(schedule.getDoctorId());
		dto.setDoctorName(schedule.getDoctorName());
		dto.setComplaints(schedule.getComplaints());
		dto.setPackageType(schedule.getPackageType());
		dto.setPackageId(schedule.getPackageId());
		dto.setPackageName(schedule.getPackageName());
		dto.setNumberOfSittings(schedule.getNumberOfSittings());
		dto.setSessionStartDate(schedule.getSessionStartDate());
		dto.setPackagePrice(schedule.getPackagePrice());
		dto.setSittings(schedule.getSittings());
		return dto;
	}

	@Override
	public Response completeSitting(String clinicId, String branchId, String sittingsId, Integer sittingNumber,
			String status) {

		Response response = new Response();

		try {

			Optional<TreatmentSchedule> scheduleOpt = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId, sittingsId);

			if (scheduleOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Treatment Schedule not found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			TreatmentSchedule schedule = scheduleOpt.get();

			Optional<Sittings> sittingOpt = schedule.getSittings().stream()
					.filter(s -> sittingsId.equals(s.getSittingsId()) && sittingNumber.equals(s.getSittingNumber()))
					.findFirst();

			if (sittingOpt.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Sitting not found");
				response.setStatus(HttpStatus.OK.value());
				return response;
			}

			Sittings sitting = sittingOpt.get();

			sitting.setTreatmentStatus(status);

			schedule.setUpdatedAt(Instant.now());

			treatmentScheduleRepository.save(schedule);

			response.setSuccess(true);
			response.setData(schedule);
			response.setMessage("Sitting status updated successfully");
			response.setStatus(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	public TreatmentScheduleDTO getByBookingId(String bookingId) {
		TreatmentSchedule schedule = treatmentScheduleRepository.findByBookingId(bookingId);

		if (schedule == null) {
			return null;
		}

		return new TreatmentScheduleDTO(schedule.getId(), schedule.getSoapNoteId(), schedule.getPatientId(),
				schedule.getPatientName(), schedule.getMobileNumber(), schedule.getClinicId(), schedule.getBranchId(),
				schedule.getBookingId(), schedule.getDoctorId(), schedule.getDoctorName(), schedule.getComplaints(),
				schedule.getPackageType(), schedule.getPackageId(), schedule.getPackageName(),
				schedule.getPackagePrice(), schedule.getNumberOfSittings(), schedule.getSessionStartDate(),
				schedule.getSittings());
	}

	public ResponseEntity<Response> getDoctorAnalytics(String clinicId, String branchId, String fromDateStr,
			String toDateStr) {
		try {
			LocalDate fromDate = LocalDate.parse(fromDateStr);
			LocalDate toDate = LocalDate.parse(toDateStr);

			List<TreatmentSchedule> schedules = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndSessionStartDateBetween(clinicId, branchId, fromDate.toString(),
							toDate.toString());

			Map<String, List<Sittings>> groupedByDoctor = new HashMap<>();

			for (TreatmentSchedule schedule : schedules) {
				if (schedule.getSittings() != null) {
					for (Sittings sitting : schedule.getSittings()) {
						if (sitting.getDate() != null) {
							LocalDate sittingDate = LocalDate.parse(sitting.getDate(),
									DateTimeFormatter.ofPattern("yyyy-MM-dd"));
							if (!sittingDate.isBefore(fromDate) && !sittingDate.isAfter(toDate)) {
								if (sitting.getDoctorId() != null) {
									groupedByDoctor.computeIfAbsent(sitting.getDoctorId(), k -> new ArrayList<>())
											.add(sitting);
								}
							}
						}
					}
				}
			}

			List<TherapistAnalyticsDTO> analyticsList = new ArrayList<>();

			for (Map.Entry<String, List<Sittings>> entry : groupedByDoctor.entrySet()) {
				String docId = entry.getKey();
				List<Sittings> doctorSittings = entry.getValue();

				String providerType = null;
				try {
					providerType = DoctorServiceImpl.getType(docId);
				} catch (Exception e) {
				}

				doctorSittings = doctorSittings.stream().filter(n -> n.getDoctorId() != null && n.getDate() != null)
						.toList();
				int total = doctorSittings.size();

				int completed = (int) doctorSittings.stream()
						.filter(s -> "Completed".equalsIgnoreCase(s.getTreatmentStatus())).count();
				int pending = total - completed;
				double ratio = total > 0 ? (double) completed / total : 0.0;

				analyticsList.add(new TherapistAnalyticsDTO(docId, doctorSittings.get(0).getDoctorName(),
						fromDate + " to " + toDate, total, completed, pending, ratio, providerType));
			}

			Response response = Response.builder().success(true).status(200)
					.message("Doctor analytics fetched successfully").hospitalId(clinicId).branchId(branchId)
					.data(analyticsList).build();

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Response errorResponse = Response.builder().success(false).status(500)
					.message("Error fetching doctor analytics: " + e.getMessage()).hospitalId(clinicId)
					.branchId(branchId).build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	public ResponseEntity<Response> getAnalytics(String clinicId, String branchId, int filter) {
		try {
			LocalDate today = LocalDate.now();
			LocalDate startDate;
			LocalDate endDate;
			int total = 0;

			switch (filter) {
			case 1:
				startDate = today.minusDays(1);
				endDate = today.plusDays(1);
				break;

			case 2:
				startDate = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
				endDate = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
				break;

			case 3:
				startDate = today.withDayOfMonth(1);
				endDate = today.withDayOfMonth(today.lengthOfMonth());
				break;

			case 4:
				startDate = today.withDayOfYear(1);
				endDate = today.withDayOfYear(today.lengthOfYear());
				break;

			default:
				return ResponseEntity.badRequest().body(Response.builder().success(false).status(400)
						.message("Invalid filter value").hospitalId(clinicId).branchId(branchId).build());
			}

			List<TreatmentSchedule> schedules = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndSessionStartDateBetween(clinicId, branchId, startDate.toString(),
							endDate.toString());

			Map<String, List<Sittings>> groupedByDoctor = new HashMap<>();

			for (TreatmentSchedule schedule : schedules) {
				if (schedule.getSittings() != null) {
					List<Sittings> FILTERED_ALL_DOCTORS_SITTINGS = schedule.getSittings().stream()
							.filter(n -> n.getDoctorId() != null && n.getDate() != null).toList();
					for (Sittings sitting : FILTERED_ALL_DOCTORS_SITTINGS) {
						if (sitting.getDate() != null) {
							LocalDate sittingDate = LocalDate.parse(sitting.getDate(),
									DateTimeFormatter.ofPattern("yyyy-MM-dd"));
							if (!sittingDate.isBefore(startDate) && !sittingDate.isAfter(endDate)) {
								if (sitting.getDoctorId() != null) {
									groupedByDoctor.computeIfAbsent(sitting.getDoctorId(), k -> new ArrayList<>())
											.add(sitting);
								}
							}
						}
					}
				}
			}

			List<TherapistAnalyticsDTO> analyticsList = new ArrayList<>();

			for (Map.Entry<String, List<Sittings>> entry : groupedByDoctor.entrySet()) {
				int totalDoctorSittings = 0;
				int completed = 0;
				String docId = entry.getKey();
				List<Sittings> doctorSittings = entry.getValue();
				String providerType = null;
				try {
					providerType = DoctorServiceImpl.getType(docId);
				} catch (Exception e) {
				}
				doctorSittings = doctorSittings.stream().filter(n -> n.getDoctorId() != null && n.getDate() != null)
						.toList();
				for (Sittings v : doctorSittings) {
					LocalDate sittingDate = LocalDate.parse(v.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					if (!sittingDate.isBefore(startDate) && !sittingDate.isAfter(endDate)) {
						totalDoctorSittings++;
					}
				}
				try {
					completed = (int) doctorSittings.stream()
							.filter(s -> "Completed".equalsIgnoreCase(s.getTreatmentStatus())).count();
				} catch (Exception e) {
				}
				int pending = totalDoctorSittings - completed;
				double ratio = totalDoctorSittings > 0 ? (double) completed / totalDoctorSittings : 0.0;

				analyticsList.add(new TherapistAnalyticsDTO(docId, doctorSittings.get(0).getDoctorName(),
						startDate + " to " + today, totalDoctorSittings, completed, pending, ratio, providerType));
			}

			Response response = Response.builder().success(true).status(200)
					.message("Therapist analytics fetched successfully").hospitalId(clinicId).branchId(branchId)
					.data(analyticsList).build();

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Response errorResponse = Response.builder().success(false).status(500)
					.message("Error fetching analytics: " + e.getMessage()).hospitalId(clinicId).branchId(branchId)
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	public ResponseEntity<Response> getDoctorSummary(String clinicId, String branchId, String doctorId, int filter) {
		try {
			List<TreatmentSchedule> schedules = treatmentScheduleRepository.findByClinicIdAndBranchId(clinicId,
					branchId);

			int total = 0;
			int completed = 0;
			int pending = 0;
			String doctorName = null;

			Map<String, PatientSummary> patientSummaryMap = new LinkedHashMap<>();

			LocalDate today = LocalDate.now();
			LocalDate startDate;
			LocalDate endDate;

			switch (filter) {
			case 1:
				startDate = today.minusDays(1);
				endDate = today.plusDays(1);
				break;
			case 2:
				startDate = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
				endDate = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
				break;
			case 3:
				startDate = today.withDayOfMonth(1);
				endDate = today.withDayOfMonth(today.lengthOfMonth());
				break;
			case 4:
				startDate = today.withDayOfYear(1);
				endDate = today.withDayOfYear(today.lengthOfYear());
				break;
			default:
				return ResponseEntity.badRequest().body(Response.builder().success(false).status(400)
						.message("Invalid filter value").hospitalId(clinicId).branchId(branchId).build());
			}

			for (TreatmentSchedule schedule : schedules) {
				if (schedule.getSittings() != null) {
					for (Sittings sitting : schedule.getSittings()) {
						if (sitting.getDoctorId() != null && sitting.getDate() != null
								&& sitting.getDoctorId().equalsIgnoreCase(doctorId)) {

							LocalDate sittingDate = LocalDate.parse(sitting.getDate(),
									DateTimeFormatter.ofPattern("yyyy-MM-dd"));

							if (!sittingDate.isBefore(startDate) && !sittingDate.isAfter(endDate)) {
								total++;
								doctorName = sitting.getDoctorName();

								if ("COMPLETED".equalsIgnoreCase(sitting.getTreatmentStatus())) {
									completed++;
								} else {
									pending++;
								}

								patientSummaryMap.compute(schedule.getPatientId(), (pid, summary) -> {
									if (summary == null) {
										return new PatientSummary(schedule.getPatientId(), schedule.getPatientName(),
												1);
									} else {
										summary.setTotalNumberOfSittings(summary.getTotalNumberOfSittings() + 1);
										// summary.setTreatedBy(sitting.getTreatedBy());
										return summary;
									}
								});
							}
						}
					}
				}
			}

			String providerType = null;
			try {
				providerType = DoctorServiceImpl.getType(doctorId);
			} catch (Exception e) {
			}

			DoctorSummaryDTO summary = new DoctorSummaryDTO(doctorId, doctorName, total, completed, pending,
					providerType, new ArrayList<>(patientSummaryMap.values()));

			Response response = Response.builder().success(true).status(200)
					.message("Doctor summary fetched successfully").hospitalId(clinicId).branchId(branchId)
					.data(summary).build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Response errorResponse = Response.builder().success(false).status(500)
					.message("Error fetching doctor summary: " + e.getMessage()).hospitalId(clinicId).branchId(branchId)
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	public ResponseEntity<Response> getPatientSummary(String clinicId, String branchId, String doctorId,
			String patientId) {

		try {
			List<TreatmentSchedule> schedules = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndPatientId(clinicId, branchId, patientId);

			String patientName = null;
			Map<String, List<DoctorSummary>> bookingSummaries = new LinkedHashMap<>();
			int total = 0;

			for (TreatmentSchedule schedule : schedules) {
				patientName = schedule.getPatientName();
				String packageType = schedule.getPackageType();
				String bookingId = schedule.getBookingId();

				if (schedule.getSittings() != null) {
					List<Sittings> doctorSittings = schedule.getSittings().stream()
							.filter(s -> s.getDoctorId() != null && s.getDate() != null)
							.filter(s -> s.getDoctorId().equalsIgnoreCase(doctorId)).toList();

					total += doctorSittings.size();

					List<DoctorSummary> summariesForBooking = new ArrayList<>();
					for (Sittings sitting : doctorSittings) {
						String status;
						if (sitting.getTreatmentStatus() != null) {
							String treatmentStatus = sitting.getTreatmentStatus().toLowerCase();

							if (treatmentStatus.equalsIgnoreCase("not started")) {
								status = "Not Started";
							} else if (treatmentStatus.equalsIgnoreCase("in-progress")) {
								status = "In-progress";
							} else if (treatmentStatus.equalsIgnoreCase("completed")) {
								status = "Completed";
							} else {
								status = "Unknown";
							}
						} else {
							status = "Unknown";
						}

						summariesForBooking.add(new DoctorSummary(sitting.getDoctorName(), sitting.getDoctorId(),
								sitting.getDate(), sitting.getSlot(), sitting.getSittingNumber(), status, packageType,
								sitting.getTreatedBy()));
					}

					if (!summariesForBooking.isEmpty()) {
						bookingSummaries.put(bookingId, summariesForBooking);
					}
				}
			}

			PatientSummaryDTO summary = new PatientSummaryDTO(patientId, patientName, total, bookingSummaries);

			Response response = Response.builder().success(true).status(200)
					.message("Patient summary fetched successfully").hospitalId(clinicId).branchId(branchId)
					.data(summary).build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Response errorResponse = Response.builder().success(false).status(500)
					.message("Error fetching patient summary: " + e.getMessage()).hospitalId(clinicId)
					.branchId(branchId).build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@Override
	public TreatmentScheduleDTO getFilteredSittings(String clinicId, String branchId, String bookingId,
			String startDate, String endDate) {
		try {
			LocalDate start = LocalDate.parse(startDate, formatter);
			LocalDate end = LocalDate.parse(endDate, formatter);
			/// System.out.println(start.toString());
			/// System.out.println(end.toString());
			/// System.out.println(bookingId);
			// Fetch the schedule for clinic + branch + booking
			TreatmentSchedule schedule = treatmentScheduleRepository.findByClinicIdAndBranchIdAndBookingId(clinicId,
					branchId, bookingId);
			/// System.out.println(schedule);
			if (schedule == null) {
				return null; // or throw a custom exception
			}

			List<Sittings> filteredSittings;
			try {
				// Filter sittings by date range
				filteredSittings = schedule.getSittings().stream()
						.filter(sitting -> sitting.getDate() != null && sitting.getSlot() != null).filter(sitting -> {
							LocalDate sittingDate = LocalDate.parse(sitting.getDate(), formatter);
							/// System.out.println(sittingDate);
							if ((!sittingDate.isBefore(start) && !sittingDate.isAfter(end))
									|| (sittingDate.equals(start) || sittingDate.equals(end))) {
								return true;
							} else {
								return false;
							}
						}).collect(Collectors.toList());
				/// System.out.println(filteredSittings);
			} catch (Exception dateEx) {
				//// System.out.println(dateEx.getMessage());
				log.warn("Error parsing sitting dates for bookingId={}: {}", bookingId, dateEx.getMessage());
				filteredSittings = Collections.emptyList();
			}

			// Replace sittings with filtered list
			schedule.setSittings(filteredSittings);

			// Configure ObjectMapper with JavaTimeModule
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			try {
				// Convert entity to DTO
				return mapper.convertValue(schedule, TreatmentScheduleDTO.class);
			} catch (Exception convertEx) {
				/// System.out.println(convertEx.getMessage());
				log.error("Error converting schedule to DTO for bookingId={}: {}", bookingId, convertEx.getMessage(),
						convertEx);
				return null;
			}

		} catch (Exception e) {
			//// System.out.println(e.getMessage());
			log.error("Error fetching filtered sittings for clinicId={}, branchId={}, bookingId={}: {}", clinicId,
					branchId, bookingId, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public List<TreatmentScheduleDTO> getSittingsBasedOnDates(String clinicId, String branchId, String startDate,
			String endDate, List<String> ids) {
		List<TreatmentScheduleDTO> result = new ArrayList<>();

		try {
			LocalDate start = LocalDate.parse(startDate, formatter);
			LocalDate end = LocalDate.parse(endDate, formatter);
			/// System.out.println(ids);
			/// System.out.println(start.toString());
			//// System.out.println(end.toString());
			// Fetch all schedules for clinic + branch
			List<TreatmentSchedule> schedules = treatmentScheduleRepository.findByClinicIdAndBranchId(clinicId,
					branchId);
			//// System.out.println(schedules);
			for (TreatmentSchedule schedule : schedules) {
				if (!ids.isEmpty()) {
					if (!ids.contains(schedule.getBookingId())) {
						try {
							List<Sittings> filteredSittings = schedule.getSittings().stream()
									.filter(sitting -> sitting.getDate() != null).filter(sitting -> {
										LocalDate sittingDate = LocalDate.parse(sitting.getDate(), formatter);
										/// System.out.println(sittingDate);
										if ((!sittingDate.isBefore(start) && !sittingDate.isAfter(end))
												|| (sittingDate.equals(start) || sittingDate.equals(end))) {
											return true;
										} else {
											return false;
										}
									}).collect(Collectors.toList());

							schedule.setSittings(filteredSittings);

							ObjectMapper mapper = new ObjectMapper();
							mapper.registerModule(new JavaTimeModule());
							mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

							TreatmentScheduleDTO dto = mapper.convertValue(schedule, TreatmentScheduleDTO.class);
							result.add(dto);
							//// System.out.println(dto);
						} catch (Exception innerEx) {
							/// System.out.println(innerEx.getMessage());
							// Handle per-schedule errors without breaking the whole loop
							log.warn("Error processing schedule with id={}: {}", schedule.getId(),
									innerEx.getMessage());
						}
					}
				} else {
					try {
						List<Sittings> filteredSittings = schedule.getSittings().stream()
								.filter(sitting -> sitting.getDate() != null).filter(sitting -> {
									LocalDate sittingDate = LocalDate.parse(sitting.getDate(), formatter);
									boolean check = false;
									if (start.equals(end)) {
										check = sittingDate.equals(start);
									} else {
										if ((!sittingDate.isBefore(start) && !sittingDate.isAfter(end))
												|| (sittingDate.equals(start) || sittingDate.equals(end))) {
											check = true;
										}
									}
									return check;
								}).collect(Collectors.toList());

						schedule.setSittings(filteredSittings);

						ObjectMapper mapper = new ObjectMapper();
						mapper.registerModule(new JavaTimeModule());
						mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

						TreatmentScheduleDTO dto = mapper.convertValue(schedule, TreatmentScheduleDTO.class);
						result.add(dto);
						/// System.out.println(dto);
					} catch (Exception innerEx) {
						/// System.out.println(innerEx.getMessage());
						// Handle per-schedule errors without breaking the whole loop
						log.warn("Error processing schedule with id={}: {}", schedule.getId(), innerEx.getMessage());
					}
				}
			}
		} catch (Exception e) {
			// Handle top-level errors
			/// System.out.println(e.getMessage());
			log.error("Error fetching sittings for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage(),
					e);
		}

		return result;
	}


	public ResponseEntity<Response> getHomeSittings(
			String clinicId,
			String branchId,
			Integer number) {

		List<TreatmentSchedule> schedules =
				treatmentScheduleRepository.findByClinicIdAndBranchId(clinicId, branchId);

		LocalDate today = LocalDate.now();

		List<HomeResponse> response = new ArrayList<>();

		List<HomeResponse> requiredObjects = new ArrayList<>();

		for (TreatmentSchedule schedule : schedules) {

			if (schedule.getSittings() == null) {
				continue;
			}

			for (Sittings sitting : schedule.getSittings()) {

				CustomerOnbording onboard =
						onboardingRepository.findByPatientId(schedule.getPatientId());

				boolean match = false;

				// 1 = Today + Home
				if (number == 1) {

					match = "Home".equalsIgnoreCase(sitting.getVisitType())
							&& sitting.getDate() != null
							&& LocalDate.parse(sitting.getDate()).isEqual(today);
				}

				// 2 = After Today + Home
				else if (number == 2) {

					match = "Home".equalsIgnoreCase(sitting.getVisitType())
							&& sitting.getDate() != null
							&& LocalDate.parse(sitting.getDate()).isAfter(today);
				}

				// 3 = Completed
				else if (number == 3) {

					match = "Completed".equalsIgnoreCase(
							sitting.getTreatmentStatus());
				}

				if (match) {

				Optional<Doctors> doctors = doctorsRepository.findByDoctorId(sitting.getDoctorId());
				String doctorMobileNumber = new String();
				if(doctors.isPresent()){
					doctorMobileNumber = doctors.get().getDoctorMobileNumber();
				}
					HomeResponse homeResponse = HomeResponse.builder()
							.sitting(sitting).customerId(onboard.getCustomerId())
							.doctorMobileNumber(doctorMobileNumber)
							.patientId(schedule.getPatientId())
							.patientName(schedule.getPatientName())
							.mobileNumber(schedule.getMobileNumber())
							.clinicId(schedule.getClinicId())
							.branchId(schedule.getBranchId())
							.gender(onboard != null ? onboard.getGender() : null)
							.age(onboard != null ? onboard.getAge() : null)
							.address(onboard != null ? onboard.getAddress() : null)
							.bookingId(schedule.getBookingId())
							.size(schedules.size())
							.build();

					response.add(homeResponse);

					if(number == 3) {
						Optional<HomeVisitTracking> home = homeVisitTrackingRepository.
								findBySittingsId(homeResponse.getSitting().getSittingsId());
						if(home.isPresent()){
							ObjectMapper objectMapper = new ObjectMapper();

							objectMapper.registerModule(
									new JavaTimeModule()
							);

							objectMapper.disable(
									SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
							);
							homeResponse.setHomeVisitTrackingRequest(objectMapper.convertValue(home.get(),HomeVisitTrackingRequest.class));
						}

					}}
				}
			}

		if(response.size() > 10){

			for(int i =0; i<=10; i++){
				requiredObjects.add(response.get(i));
			}
		}

		Response apiResponse = new Response();
		if(requiredObjects != null && !requiredObjects.isEmpty()){

		apiResponse = Response.builder()
				.success(true)
				.data(requiredObjects)
				.message("Home sittings fetched successfully")
				.status(HttpStatus.OK.value())
				.build();
		}else{

			apiResponse = Response.builder()
					.success(true)
					.data(response)
					.message("Home sittings fetched successfully")
					.status(HttpStatus.OK.value())
					.build();
		}

		return ResponseEntity.ok(apiResponse);
	}


	@Override
	public ResponseEntity<Response> getHomeSittingsByDoctor(
			String clinicId,
			String branchId,
			String doctorId,
			Integer number) {

		try {

			/*
			 * =====================================================
			 * 1. VALIDATION
			 * =====================================================
			 */

			if (clinicId == null || clinicId.isBlank()) {

				return badRequest("ClinicId is required");
			}

			if (branchId == null || branchId.isBlank()) {

				return badRequest("BranchId is required");
			}

			if (doctorId == null || doctorId.isBlank()) {

				return badRequest("DoctorId is required");
			}

			if (number == null) {

				return badRequest("Number is required");
			}

			if (number < 1 || number > 3) {

				return badRequest(
						"Number must be 1, 2 or 3"
				);
			}


			/*
			 * =====================================================
			 * 2. FETCH TREATMENT SCHEDULES
			 * =====================================================
			 */

			List<TreatmentSchedule> schedules =
					treatmentScheduleRepository
							.findByClinicIdAndBranchIdAndDoctorId(
									clinicId,
									branchId,
									doctorId
							);


			/*
			 * =====================================================
			 * 3. NO SCHEDULES
			 * =====================================================
			 */

			if (schedules == null || schedules.isEmpty()) {

				return ResponseEntity
						.ok(
								Response.builder()
										.success(true)
										.data(Collections.emptyList())
										.message(
												"No doctor home sittings found"
										)
										.status(
												HttpStatus.OK.value()
										)
										.build()
						);
			}


			/*
			 * =====================================================
			 * 4. CURRENT DATE
			 * =====================================================
			 */

			LocalDate today = LocalDate.now();


			/*
			 * =====================================================
			 * 5. RESULT
			 * =====================================================
			 */

			List<HomeResponse> response =
					new ArrayList<>();


			/*
			 * =====================================================
			 * 6. LOOP THROUGH SCHEDULES
			 * =====================================================
			 */

			for (TreatmentSchedule schedule : schedules) {

				if (schedule.getSittings() == null
						|| schedule.getSittings().isEmpty()) {

					continue;
				}


				/*
				 * =================================================
				 * PATIENT ONBOARDING
				 * =================================================
				 */

				CustomerOnbording onboard =
						onboardingRepository.findByPatientId(
								schedule.getPatientId()
						);


				/*
				 * =================================================
				 * LOOP THROUGH SITTINGS
				 * =================================================
				 */

				for (Sittings sitting : schedule.getSittings()) {


					/*
					 * =================================================
					 * IMPORTANT:
					 * FILTER SITTING BY DOCTOR ID
					 * =================================================
					 *
					 * A TreatmentSchedule may contain sittings
					 * belonging to different doctors.
					 *
					 * Therefore checking only:
					 *
					 * schedule.getDoctorId()
					 *
					 * is NOT enough.
					 */

					if (sitting.getDoctorId() == null
							|| !doctorId.equals(
							sitting.getDoctorId()
					)) {

						continue;
					}


					/*
					 * =================================================
					 * 1 = TODAY + HOME
					 * =================================================
					 */

					boolean match = false;

					if (number == 1) {

						if ("Home".equalsIgnoreCase(
								sitting.getVisitType()
						)
								&& sitting.getDate() != null
								&& !sitting.getDate().isBlank()) {

							try {

								LocalDate sittingDate =
										LocalDate.parse(
												sitting.getDate()
										);

								match =
										sittingDate.isEqual(today);

							} catch (Exception e) {

								/*
								 * Invalid date format.
								 *
								 * Expected:
								 * yyyy-MM-dd
								 */

								continue;
							}
						}
					}


					/*
					 * =================================================
					 * 2 = AFTER TODAY + HOME
					 * =================================================
					 */

					else if (number == 2) {

						if ("Home".equalsIgnoreCase(
								sitting.getVisitType()
						)
								&& sitting.getDate() != null
								&& !sitting.getDate().isBlank()) {

							try {

								LocalDate sittingDate =
										LocalDate.parse(
												sitting.getDate()
										);

								match =
										sittingDate.isAfter(today);

							} catch (Exception e) {

								continue;
							}
						}
					}


					/*
					 * =================================================
					 * 3 = COMPLETED
					 * =================================================
					 */

					else if (number == 3) {

						match =
								"Completed".equalsIgnoreCase(
										sitting.getTreatmentStatus()
								);
					}


					/*
					 * =================================================
					 * NOT MATCHED
					 * =================================================
					 */

					if (!match) {

						continue;
					}


					/*
					 * =================================================
					 * FETCH DOCTOR DETAILS
					 * =================================================
					 */

					String doctorMobileNumber = "";

					Optional<Doctors> doctors =
							doctorsRepository.findByDoctorId(
									sitting.getDoctorId()
							);

					if (doctors.isPresent()) {

						doctorMobileNumber =
								doctors.get()
										.getDoctorMobileNumber();
					}


					/*
					 * =================================================
					 * CREATE HOME RESPONSE
					 * =================================================
					 */

					HomeResponse homeResponse =
							HomeResponse.builder()
									.sitting(sitting)
									.customerId(
											onboard != null
													? onboard.getCustomerId()
													: null
									)
									.doctorMobileNumber(
											doctorMobileNumber
									)
									.patientId(
											schedule.getPatientId()
									)
									.patientName(
											schedule.getPatientName()
									)
									.mobileNumber(
											schedule.getMobileNumber()
									)
									.clinicId(
											schedule.getClinicId()
									)
									.branchId(
											schedule.getBranchId()
									)
									.gender(
											onboard != null
													? onboard.getGender()
													: null
									)
									.age(
											onboard != null
													? onboard.getAge()
													: null
									)
									.address(
											onboard != null
													? onboard.getAddress()
													: null
									)
									.bookingId(
											schedule.getBookingId()
									)
									.size(
											schedules.size()
									)
									.build();


					/*
					 * =================================================
					 * FETCH HOME VISIT TRACKING
					 * =================================================
					 */

					try {

						if (sitting.getSittingsId() != null
								&& !sitting.getSittingsId().isBlank()) {

							Optional<HomeVisitTracking> home =
									homeVisitTrackingRepository
											.findBySittingsId(
													sitting.getSittingsId()
											);


							if (home.isPresent()) {

								ObjectMapper objectMapper =
										new ObjectMapper();

								objectMapper.registerModule(
										new JavaTimeModule()
								);

								objectMapper.disable(
										SerializationFeature
												.WRITE_DATES_AS_TIMESTAMPS
								);


								HomeVisitTrackingRequest trackingRequest =
										objectMapper.convertValue(
												home.get(),
												HomeVisitTrackingRequest.class
										);


								homeResponse
										.setHomeVisitTrackingRequest(
												trackingRequest
										);
							}
						}

					} catch (Exception e) {

						/*
						 * Tracking failure should not prevent
						 * the main sitting record from returning.
						 */

						// log.error(
						//     "Failed to fetch tracking for sittingId: {}",
						//     sitting.getSittingsId(),
						//     e
						// );
					}


					/*
					 * =================================================
					 * ADD MATCHING RECORD
					 * =================================================
					 */

					response.add(homeResponse);


					/*
					 * =================================================
					 * MAXIMUM 10 RECORDS
					 * =================================================
					 */

					if (response.size() >= 10) {

						break;
					}
				}


				/*
				 * Stop once 10 records are collected.
				 */

				if (response.size() >= 10) {

					break;
				}
			}


			/*
			 * =====================================================
			 * 7. RETURN RESPONSE
			 * =====================================================
			 */

			return ResponseEntity
					.ok(
							Response.builder()
									.success(true)
									.data(response)
									.message(
											"Doctor home sittings fetched successfully"
									)
									.status(
											HttpStatus.OK.value()
									)
									.build()
					);


		} catch (Exception e) {

			return ResponseEntity
					.status(
							HttpStatus.INTERNAL_SERVER_ERROR
					)
					.body(
							Response.builder()
									.success(false)
									.data(null)
									.message(
											"Failed to fetch doctor home sittings: "
													+ e.getMessage()
									)
									.status(
											HttpStatus.INTERNAL_SERVER_ERROR
													.value()
									)
									.build()
					);
		}
	}


	@Override
	public ResponseEntity<Response> updateHomeVisitStatus(String clinicId, String branchId,
			String sittingsId,
			HomeVisitTrackingRequest request) {

		try {

			Optional<HomeVisitTracking> optionalTracking =
					homeVisitTrackingRepository.findBySittingsId(sittingsId);

			/*
			 * FIRST REQUEST
			 *
			 * No record exists yet.
			 * Automatically create the record with ASSIGNED status.
			 */
			if (optionalTracking.isEmpty()) {

				HomeVisitTracking tracking = new HomeVisitTracking();

				tracking.setSittingsId(sittingsId);
				tracking.setAssignmentStatus("ASSIGNED");
				tracking.setAssignedAt(OffsetDateTime.from(Instant.now()));

				JourneyToPatient journeyToPatient =
						new JourneyToPatient();

				journeyToPatient.setStatus("ASSIGNED");

				tracking.setJourneyToPatient(journeyToPatient);

				tracking.setCreatedAt(Instant.now());
				tracking.setUpdatedAt(Instant.now());

				HomeVisitTracking saved =
						homeVisitTrackingRepository.save(tracking);

				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Response.builder()
								.success(true)
								.data(saved)
								.message("Home visit assigned successfully")
								.status(HttpStatus.CREATED.value())
								.build());
			}

			/*
			 * EXISTING RECORD
			 *
			 * From this point onwards, status comes from
			 * the appropriate nested request object.
			 */
			HomeVisitTracking tracking = optionalTracking.get();

			String newStatus = extractStatus(request);

			if (newStatus == null || newStatus.isBlank()) {

				return badRequest(
						"Status is required in the appropriate request object"
				);
			}

			String currentStatus = tracking.getAssignmentStatus();

			/*
			 * Process the new status
			 */
			if ("JOURNEY_TO_PATIENT".equals(newStatus)) {

				updateJourneyToPatientStarted(clinicId, branchId,
						sittingsId,
						tracking,
						request.getJourneyToPatient()
				);

			} else if ("PATIENT_REACHED".equals(newStatus)) {

				updatePatientReached(clinicId,branchId,
						sittingsId,
						tracking,
						request.getJourneyToPatient()
				);

			} else if ("TREATMENT_IN_PROGRESS".equals(newStatus)) {

				updateTreatmentStarted(clinicId,branchId,
						sittingsId,
						tracking,
						request.getTreatment()
				);

			} else if ("TREATMENT_COMPLETED".equals(newStatus)) {

				updateTreatmentCompleted(clinicId,branchId,
						sittingsId,
						tracking,
						request.getTreatment()
				);

			} else if ("JOURNEY_TO_CLINIC".equals(newStatus)) {

				updateJourneyToClinicStarted(clinicId,branchId,
						sittingsId,
						tracking,
						request.getJourneyToClinic()
				);

			} else if ("CLINIC_REACHED".equals(newStatus)) {

				updateClinicReached(clinicId,branchId,
						sittingsId,
						tracking,
						request.getJourneyToClinic()
				);

			} else if ("COMPLETED".equals(newStatus)) {

				updateCompletion(clinicId,branchId,
						sittingsId,
						tracking,
						request.getCompletion()
				);

			} else {

				return badRequest(
						"Invalid status: " + newStatus
				);
			}

			/*
			 * Update root assignment status
			 */
			tracking.setAssignmentStatus(newStatus);
			tracking.setUpdatedAt(Instant.now());

			HomeVisitTracking saved =
					homeVisitTrackingRepository.save(tracking);

			return ResponseEntity.ok(
					Response.builder()
							.success(true)
							.data(saved)
							.message(
									"Home visit status updated to "
											+ newStatus
							)
							.status(HttpStatus.OK.value())
							.build()
			);

		} catch (IllegalArgumentException e) {

			return badRequest(e.getMessage());

		} catch (Exception e) {

			return ResponseEntity.status(
							HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Response.builder()
							.success(false)
							.message(
									"Failed to update home visit: "
											+ e.getMessage()
							)
							.status(
									HttpStatus.INTERNAL_SERVER_ERROR.value()
							)
							.build());
		}
	}

	private void updateJourneyToPatientStarted(String clinicId, String branchId,
	                                           String sittingsId,
			HomeVisitTracking tracking,
			JourneyToPatientDTO request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Journey to patient details are required"
			);
		}

		if (request.getStartedLatitude() == null
				|| request.getStartedLongitude() == null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		JourneyToPatient journey = tracking.getJourneyToPatient();

		if (journey == null) {
			journey = new JourneyToPatient();
		}

		OffsetDateTime now =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		journey.setStatus("JOURNEY_TO_PATIENT");
		tracking.setAssignmentStatus("JOURNEY_TO_PATIENT");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("JOURNEY_TO_PATIENT");
				};
			});
		}
		journey.setIsJourneyToPatientStarted(true);
		journey.setStartedAt(now);
		journey.setStartedLatitude(request.getStartedLatitude());
		journey.setStartedLongitude(request.getStartedLongitude());

		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getStartedLatitude() + "," + request.getStartedLatitude();

		journey.setStartedAddressUrl(address);

		journey.setStartedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));

		tracking.setJourneyToPatient(journey);
	}


	private void updatePatientReached(String clinicId, String branchId,
	                                  String sittingsId,
			HomeVisitTracking tracking,
			JourneyToPatientDTO request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Journey to patient details are required"
			);
		}

		if (request.getReachedLatitude() == null
				|| request.getReachedLongitude() == null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		JourneyToPatient journey =
				tracking.getJourneyToPatient();

		if (journey == null
				|| journey.getStartedAt() == null) {

			throw new IllegalArgumentException(
					"Journey to patient has not been started"
			);
		}

		OffsetDateTime reachedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		journey.setStatus("PATIENT_REACHED");
		tracking.setAssignmentStatus("PATIENT_REACHED");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("PATIENT_REACHED");
				};
			});
		}
		journey.setIsJourneyToPatientEnded(true);
		journey.setReachedAt(reachedAt);
		journey.setReachedLatitude(request.getReachedLatitude());
		journey.setReachedLongitude(request.getReachedLongitude());


		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getReachedLatitude()+ "," +request.getReachedLongitude();

		journey.setReachedAddressUrl(address);

		journey.setReachedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));


		long duration = Duration.between(
				journey.getStartedAt(),
				reachedAt
		).toMinutes();

		journey.setDurationMinutes(duration);

		Double distance = calculateDistance(
				journey.getStartedLatitude(),
				journey.getStartedLongitude(),
				journey.getReachedLatitude(),
				journey.getReachedLongitude()
		);

		journey.setDistanceKm(distance);

		tracking.setJourneyToPatient(journey);
	}


	private void updateTreatmentStarted(String clinicId, String branchId,
	                                    String sittingsId,
			HomeVisitTracking tracking,
			TreatmentResponse request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Treatment details are required"
			);
		}

		if (request.getStartedLatitude() == null
				|| request.getStartedLongitude()== null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		TreatmentEntity treatment =
				tracking.getTreatment();

		if (treatment == null) {
			treatment = new TreatmentEntity();
		}

		OffsetDateTime startedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		treatment.setStatus("TREATMENT_IN_PROGRESS");
		tracking.setAssignmentStatus("TREATMENT_IN_PROGRESS");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("TREATMENT_IN_PROGRESS");
				};
			});
		}
		treatment.setIsTreatmentStarted(true);
		treatment.setStartedAt(startedAt);
		treatment.setStartedLatitude(request.getStartedLatitude());
		treatment.setStartedLongitude(request.getEndedLongitude());

		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getStartedLatitude()+ "," +request.getStartedLongitude();

		treatment.setReachedAddressUrl(address);

		treatment.setStartedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));


		tracking.setTreatment(treatment);
	}


	private void updateTreatmentCompleted(String clinicId, String branchId,
	                                      String sittingsId,
			HomeVisitTracking tracking,
			TreatmentResponse request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Treatment details are required"
			);
		}

		if (request.getEndedLatitude() == null
				|| request.getEndedLongitude() == null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		TreatmentEntity treatment =
				tracking.getTreatment();

		if (treatment == null
				|| treatment.getStartedAt() == null) {

			throw new IllegalArgumentException(
					"Treatment has not been started"
			);
		}

		OffsetDateTime endedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		treatment.setStatus("TREATMENT_COMPLETED");
		tracking.setAssignmentStatus("TREATMENT_COMPLETED");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("TREATMENT_COMPLETED");
				};
			});
		}
		treatment.setIsTreatmentEnded(true);
		treatment.setEndedAt(endedAt);
		treatment.setEndedLatitude(request.getEndedLatitude());
		treatment.setEndedLongitude(request.getEndedLongitude());

		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getEndedLatitude()+ "," +request.getEndedLongitude();

		treatment.setReachedAddressUrl(address);

		treatment.setReachedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));


		long duration = Duration.between(
				treatment.getStartedAt(),
				endedAt
		).toMinutes();

		treatment.setDurationMinutes(duration);

		tracking.setTreatment(treatment);
	}


	private void updateJourneyToClinicStarted(String clinicId, String branchId,
	                                          String sittingsId,
			HomeVisitTracking tracking,
			JourneyToClinicDTO request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Journey to clinic details are required"
			);
		}

		if (request.getStartedLatitude() == null
				|| request.getStartedLongitude() == null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		JourneyToClinic journey =
				tracking.getJourneyToClinic();

		if (journey == null) {
			journey = new JourneyToClinic();
		}

		OffsetDateTime startedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		journey.setStatus("JOURNEY_TO_CLINIC");
		tracking.setAssignmentStatus("JOURNEY_TO_CLINIC");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("JOURNEY_TO_CLINIC");
				};
			});
		}
		journey.setIsJourneyToClinicStarted(true);
		journey.setStartedAt(startedAt);
		journey.setStartedLatitude(request.getStartedLatitude());
		journey.setStartedLongitude(request.getStartedLongitude());

		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getStartedLatitude()+ "," +request.getStartedLongitude();

		journey.setStartedAddressUrl(address);

		journey.setStartedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));


		tracking.setJourneyToClinic(journey);
	}


	private void updateClinicReached(String clinicId, String branchId,
	                                 String sittingsId,
			HomeVisitTracking tracking,
			JourneyToClinicDTO request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Journey to clinic details are required"
			);
		}

		if (request.getReachedClinicLatitude() == null
				|| request.getReachedClinicLongitude() == null) {

			throw new IllegalArgumentException(
					"Latitude and longitude are required"
			);
		}

		JourneyToClinic journey =
				tracking.getJourneyToClinic();

		if (journey == null
				|| journey.getStartedAt() == null) {

			throw new IllegalArgumentException(
					"Journey to clinic has not been started"
			);
		}

		OffsetDateTime reachedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		journey.setStatus("CLINIC_REACHED");
		tracking.setAssignmentStatus("CLINIC_REACHED");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("CLINIC_REACHED");
				};
			});
		}
		journey.setIsJourneyToClinicEnded(true);
		journey.setReachedClinicAt(reachedAt);
		journey.setReachedClinicLatitude(
				request.getReachedClinicLatitude()
		);
		journey.setReachedClinicLongitude(
				request.getReachedClinicLongitude()
		);

		String address = "https://www.google.com/maps/search/?api=1&query="
				+request.getReachedClinicLatitude()+ "," +request.getReachedClinicLongitude();

		journey.setReachedAddressUrl(address);

		journey.setReachedAddress(getAddressFromCoordinates(request.getStartedLatitude(), request.getStartedLatitude()));


		long duration = Duration.between(
				journey.getStartedAt(),
				reachedAt
		).toMinutes();

		journey.setDurationMinutes(duration);

		Double distance = calculateDistance(
				journey.getStartedLatitude(),
				journey.getStartedLongitude(),
				journey.getReachedClinicLatitude(),
				journey.getReachedClinicLongitude()
		);

		journey.setDistanceKm(distance);

		tracking.setJourneyToClinic(journey);
	}

	private void updateCompletion(String clinicId, String branchId,
	                              String sittingsId,
			HomeVisitTracking tracking,
			CompletionDTO request) {

		if (request == null) {
			throw new IllegalArgumentException(
					"Completion details are required"
			);
		}

		if (request.getCompletedBy() == null
				|| request.getCompletedBy().isBlank()) {

			throw new IllegalArgumentException(
					"CompletedBy is required"
			);
		}

		Completion completion =
				tracking.getCompletion();

		if (completion == null) {
			completion = new Completion();
		}

		OffsetDateTime completedAt =
				OffsetDateTime.now(ZoneOffset.of("+05:30"));

		completion.setStatus("COMPLETED");
		tracking.setAssignmentStatus("COMPLETED");
		Optional<TreatmentSchedule> res = treatmentScheduleRepository.findByClinicIdAndBranchIdAndSittingsId(clinicId, branchId,
				sittingsId);
		if(res.isPresent()){
			res.get().getSittings().stream().forEach(n->{
				if(n.getSittingsId().equalsIgnoreCase(sittingsId)){
					n.setTreatmentStatus("COMPLETED");
				};
			});
		}
		completion.setIsCompleted(true);
		completion.setCompletedAt(completedAt);
		completion.setCompletedBy(request.getCompletedBy());

		tracking.setCompletion(completion);
	}

	private Double calculateDistance(
			Double startLatitude,
			Double startLongitude,
			Double endLatitude,
			Double endLongitude) {

		if (startLatitude == null
				|| startLongitude == null
				|| endLatitude == null
				|| endLongitude == null) {

			return null;
		}

		final double EARTH_RADIUS_KM = 6371.0;

		double lat1 = Math.toRadians(startLatitude);
		double lat2 = Math.toRadians(endLatitude);

		double deltaLat =
				Math.toRadians(endLatitude - startLatitude);

		double deltaLon =
				Math.toRadians(endLongitude - startLongitude);

		double a =
				Math.sin(deltaLat / 2)
						* Math.sin(deltaLat / 2)
						+ Math.cos(lat1)
						* Math.cos(lat2)
						* Math.sin(deltaLon / 2)
						* Math.sin(deltaLon / 2);

		double c =
				2 * Math.atan2(
						Math.sqrt(a),
						Math.sqrt(1 - a)
				);

		return Math.round(
				EARTH_RADIUS_KM * c * 100.0
		) / 100.0;
	}

	private ResponseEntity<Response> badRequest(String message) {

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(
						Response.builder()
								.success(false)
								.data(null)
								.message(message)
								.status(HttpStatus.BAD_REQUEST.value())
								.build()
				);
	}


	private String extractStatus(HomeVisitTrackingRequest request) {

		if (request == null) {
			return null;
		}

		if (request.getJourneyToPatient() != null
				&& request.getJourneyToPatient().getStatus() != null
				&& !request.getJourneyToPatient().getStatus().isBlank()) {

			return request.getJourneyToPatient().getStatus();
		}

		if (request.getTreatment() != null
				&& request.getTreatment().getStatus() != null
				&& !request.getTreatment().getStatus().isBlank()) {

			return request.getTreatment().getStatus();
		}

		if (request.getJourneyToClinic() != null
				&& request.getJourneyToClinic().getStatus() != null
				&& !request.getJourneyToClinic().getStatus().isBlank()) {

			return request.getJourneyToClinic().getStatus();
		}

		if (request.getCompletion() != null
				&& request.getCompletion().getStatus() != null
				&& !request.getCompletion().getStatus().isBlank()) {

			return request.getCompletion().getStatus();
		}

		return null;
	}


	@Override
	public ResponseEntity<Response> getHomeVisit(
			String clinicId,
			String branchId,
			String sittingsId,
			Integer number) {

		try {

			/*
			 * =====================================================
			 * 1. VALIDATE INPUT
			 * =====================================================
			 */

			if (clinicId == null || clinicId.isBlank()) {

				return badRequest(
						"ClinicId is required"
				);
			}

			if (branchId == null || branchId.isBlank()) {

				return badRequest(
						"BranchId is required"
				);
			}

			if (number == null) {

				return badRequest(
						"Number is required"
				);
			}

			if (number < 1 || number > 3) {

				return badRequest(
						"Number must be 1, 2 or 3"
				);
			}


			/*
			 * =====================================================
			 * 2. FETCH TREATMENT SCHEDULES
			 * =====================================================
			 */

			List<TreatmentSchedule> schedules =
					treatmentScheduleRepository
							.findByClinicIdAndBranchId(
									clinicId,
									branchId
							);


			if (schedules == null || schedules.isEmpty()) {

				return ResponseEntity
						.status(HttpStatus.OK)
						.body(
								Response.builder()
										.success(false)
										.data(Collections.emptyList())
										.message(
												"No treatment schedules found"
										)
										.status(
												HttpStatus.OK.value()
										)
										.build()
						);
			}


			/*
			 * =====================================================
			 * 3. CURRENT DATE
			 * =====================================================
			 *
			 * Sittings.date format:
			 *
			 * yyyy-MM-dd
			 *
			 * Example:
			 * 2026-09-10
			 */

			LocalDate today = LocalDate.now();


			/*
			 * =====================================================
			 * 4. RESULT LIST
			 * =====================================================
			 */

			List<HomeResponse> homeResponses =
					new ArrayList<>();


			/*
			 * =====================================================
			 * 5. LOOP THROUGH TREATMENT SCHEDULES
			 * =====================================================
			 */

			for (TreatmentSchedule schedule : schedules) {

				if (schedule.getSittings() == null
						|| schedule.getSittings().isEmpty()) {

					continue;
				}


				/*
				 * =================================================
				 * FETCH DOCTOR MOBILE NUMBER
				 * =================================================
				 */

				String doctorMobileNumber = "";

				/*
				 * =================================================
				 * FETCH PATIENT ONBOARDING
				 * =================================================
				 */

				CustomerOnbording onboard =
						onboardingRepository.findByPatientId(
								schedule.getPatientId()
						);


				/*
				 * =================================================
				 * LOOP THROUGH SITTINGS
				 * =================================================
				 */

				for (Sittings sitting : schedule.getSittings()) {

					Optional<Doctors> doctors =
							doctorsRepository.findByDoctorId(
									sitting.getDoctorId()
							);

					if (doctors.isPresent()) {

						doctorMobileNumber =
								doctors.get()
										.getDoctorMobileNumber();
					}
					/*
					 * =================================================
					 * OPTIONAL SITTING ID FILTER
					 * =================================================
					 *
					 * If sittingsId is provided, return only that
					 * particular sitting.
					 *
					 * If sittingsId is null/blank, process all
					 * sittings.
					 */

					if (sittingsId != null
							&& !sittingsId.isBlank()
							&& !sittingsId.equals(
							sitting.getSittingsId()
					)) {

						continue;
					}


					/*
					 * =================================================
					 * NUMBER = 1
					 *
					 * TODAY'S HOME VISITS
					 * =================================================
					 */

					if (number == 1) {

						/*
						 * Visit type must be HOME
						 */

						if (!"home".equalsIgnoreCase(
								sitting.getVisitType()
						)) {

							continue;
						}


						/*
						 * Date must be available
						 */

						if (sitting.getDate() == null
								|| sitting.getDate().isBlank()) {

							continue;
						}


						LocalDate sittingDate;

						try {

							/*
							 * yyyy-MM-dd
							 *
							 * Example:
							 * 2026-09-10
							 */

							sittingDate =
									LocalDate.parse(
											sitting.getDate()
									);

						} catch (Exception e) {

							/*
							 * Invalid date format.
							 */

							continue;
						}


						/*
						 * Only today's records
						 */

						if (!sittingDate.equals(today)) {

							continue;
						}
					}


					/*
					 * =================================================
					 * NUMBER = 2
					 *
					 * FUTURE HOME VISITS
					 * =================================================
					 */

					if (number == 2) {

						/*
						 * Visit type must be HOME
						 */

						if (!"home".equalsIgnoreCase(
								sitting.getVisitType()
						)) {

							continue;
						}


						/*
						 * Date must be available
						 */

						if (sitting.getDate() == null
								|| sitting.getDate().isBlank()) {

							continue;
						}


						LocalDate sittingDate;

						try {

							/*
							 * yyyy-MM-dd
							 */

							sittingDate =
									LocalDate.parse(
											sitting.getDate()
									);

						} catch (Exception e) {

							continue;
						}


						/*
						 * Only dates AFTER today.
						 *
						 * Today itself is excluded.
						 */

						if (!sittingDate.isAfter(today)) {

							continue;
						}
					}


					/*
					 * =================================================
					 * NUMBER = 3
					 *
					 * COMPLETED RECORDS
					 * =================================================
					 */

					if (number == 3) {

						if (!"Completed".equalsIgnoreCase(
								sitting.getTreatmentStatus()
						)) {

							continue;
						}
					}


					/*
					 * =================================================
					 * 6. CREATE HOME RESPONSE
					 * =================================================
					 */

					HomeResponse homeResponse =
							new HomeResponse();


					homeResponse.setPatientId(
							schedule.getPatientId()
					);

					homeResponse.setDoctorMobileNumber(
							doctorMobileNumber
					);

					homeResponse.setPatientName(
							schedule.getPatientName()
					);

					homeResponse.setMobileNumber(
							schedule.getMobileNumber()
					);

					homeResponse.setClinicId(
							schedule.getClinicId()
					);

					homeResponse.setBranchId(
							schedule.getBranchId()
					);

					homeResponse.setBookingId(
							schedule.getBookingId()
					);

					homeResponse.setSitting(
							sitting
					);


					/*
					 * =================================================
					 * 7. PATIENT INFORMATION
					 * =================================================
					 */

					if (onboard != null) {

						homeResponse.setGender(
								onboard.getGender()
						);

						homeResponse.setAge(
								onboard.getAge()
						);

						homeResponse.setAddress(
								onboard.getAddress()
						);
						homeResponse.setCustomerId(
								onboard.getCustomerId()
						);

						/*
						 * Do NOT overwrite clinicId here.
						 *
						 * Previously you had:
						 *
						 * homeResponse.setClinicId(
						 *     onboard.getCustomerId()
						 * );
						 *
						 * That overwrites the actual clinicId.
						 */
					}


					/*
					 * =================================================
					 * 8. FETCH HOME VISIT TRACKING
					 * =================================================
					 */

					if (sitting.getSittingsId() != null
							&& !sitting.getSittingsId().isBlank()) {

						Optional<HomeVisitTracking> optionalTracking =
								homeVisitTrackingRepository
										.findBySittingsId(
												sitting.getSittingsId()
										);


						if (optionalTracking.isPresent()) {

							HomeVisitTracking tracking =
									optionalTracking.get();


							/*
							 * Convert entity → DTO
							 */

							ObjectMapper objectMapper =
									new ObjectMapper();

							objectMapper.registerModule(
									new JavaTimeModule()
							);

							objectMapper.disable(
									SerializationFeature
											.WRITE_DATES_AS_TIMESTAMPS
							);


							HomeVisitTrackingRequest trackingRequest =
									objectMapper.convertValue(
											tracking,
											HomeVisitTrackingRequest.class
									);


							homeResponse
									.setHomeVisitTrackingRequest(
											trackingRequest
									);
						}
					}


					/*
					 * =================================================
					 * 9. ADD RESULT
					 * =================================================
					 */

					homeResponses.add(
							homeResponse
					);


					/*
					 * =================================================
					 * MAXIMUM 10 RECORDS
					 * =================================================
					 */

					if (homeResponses.size() >= 10) {

						break;
					}
				}


				/*
				 * Stop processing schedules once we have
				 * 10 records.
				 */

				if (homeResponses.size() >= 10) {

					break;
				}
			}


			/*
			 * =====================================================
			 * 10. NO MATCHING RECORDS
			 * =====================================================
			 */

			if (homeResponses.isEmpty()) {

				String message;

				if (number == 1) {

					message =
							"No home visits found for today";

				} else if (number == 2) {

					message =
							"No upcoming home visits found";

				} else {

					message =
							"No completed records found";
				}


				return ResponseEntity
						.status(HttpStatus.OK)
						.body(
								Response.builder()
										.success(false)
										.data(
												Collections.emptyList()
										)
										.message(message)
										.status(
												HttpStatus.OK.value()
										)
										.build()
						);
			}


			/*
			 * =====================================================
			 * 11. RETURN RESPONSE
			 * =====================================================
			 */

			return ResponseEntity
					.status(HttpStatus.OK)
					.body(
							Response.builder()
									.success(true)
									.data(homeResponses)
									.message(
											"Home visits fetched successfully"
									)
									.status(
											HttpStatus.OK.value()
									)
									.build()
					);


		} catch (Exception e) {

			return ResponseEntity
					.status(
							HttpStatus.INTERNAL_SERVER_ERROR
					)
					.body(
							Response.builder()
									.success(false)
									.message(
											"Failed to fetch home visits: "
													+ e.getMessage()
									)
									.status(
											HttpStatus.INTERNAL_SERVER_ERROR
													.value()
									)
									.build()
					);
		}
	}

	@Override
	public ResponseEntity<Response> updateSittingTreatment(
			String clinicId,
			String branchId,
			String sittingId,
			UpdateSittingTreatmentRequest request) {

		TreatmentSchedule treatmentSchedule =
				treatmentScheduleRepository
						.findByClinicIdAndBranchIdAndSittings_SittingsId(
								clinicId,
								branchId,
								sittingId
						)
						.orElse(null);

		if (treatmentSchedule == null) {

			Response response = Response.builder()
					.success(false)
					.status(HttpStatus.OK.value())
					.message("Treatment schedule or sitting not found")
					.build();

			return ResponseEntity
					.status(HttpStatus.OK
					)
					.body(response);
		}

		Sittings targetSitting = treatmentSchedule.getSittings()
				.stream()
				.filter(sitting ->
						sittingId.equals(sitting.getSittingsId()))
				.findFirst()
				.orElse(null);

		if (targetSitting == null) {

			Response response = Response.builder()
					.success(false)
					.status(HttpStatus.OK.value())
					.message("Sitting not found")
					.build();

			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(response);
		}

		// Update sitting fields
		targetSitting.setCondition(request.getCondition());
		targetSitting.setTreatmentPlan(request.getTreatmentPlan());
		targetSitting.setTreatedBy(request.getTreatedBy());

		// Update parent document timestamp
		treatmentSchedule.setUpdatedAt(Instant.now());

		treatmentScheduleRepository.save(treatmentSchedule);

		Response response = Response.builder()
				.success(true)
				.status(HttpStatus.OK.value())
				.message("Sitting treatment details updated successfully")
				.data(targetSitting)
				.build();

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(response);
	}


	private String getAddressFromCoordinates(
			Double latitude,
			Double longitude) {

		String url =
				"https://nominatim.openstreetmap.org/reverse"
						+ "?format=jsonv2"
						+ "&lat=" + latitude
						+ "&lon=" + longitude
						+ "&addressdetails=1";

		HttpHeaders headers = new HttpHeaders();

		// Nominatim requires an identifying User-Agent
		headers.set("User-Agent", "ClinicAdmin/1.0");

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<JsonNode> response =
				restTemplate.exchange(
						url,
						HttpMethod.GET,
						entity,
						JsonNode.class
				);

		JsonNode body = response.getBody();

		if (body != null && body.has("display_name")) {
			return body.get("display_name").asText();
		}

		return null;
	}

}
