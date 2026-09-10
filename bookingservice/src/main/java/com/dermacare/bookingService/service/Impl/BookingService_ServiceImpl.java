package com.dermacare.bookingService.service.Impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.dermacare.bookingService.dto.*;
import com.dermacare.bookingService.feign.AdminServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dermacare.bookingService.entity.Booking;
import com.dermacare.bookingService.entity.ConsultationFees;
import com.dermacare.bookingService.entity.FollowupBooking;
import com.dermacare.bookingService.entity.Reports;
import com.dermacare.bookingService.entity.ReportsList;
import com.dermacare.bookingService.entity.Status;
import com.dermacare.bookingService.entity.TheraphyAnswersEntity;
import com.dermacare.bookingService.feign.ClinicAdminFeign;
import com.dermacare.bookingService.feign.NotificationFeign;
import com.dermacare.bookingService.feign.PhysioDoctorFeign;
import com.dermacare.bookingService.repository.BookingServiceRepository;
import com.dermacare.bookingService.service.BookingService_Service;
import com.dermacare.bookingService.service.S3Service;
import com.dermacare.bookingService.util.Response;
import com.dermacare.bookingService.util.ResponseStructure;
import com.dermacare.bookingService.util.geneateIds;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingService_ServiceImpl implements BookingService_Service {

	@Autowired
	private BookingServiceRepository repository;

	@Autowired
	private PhysioDoctorFeign physioDoctorFeign;

	@Autowired
	private ClinicAdminFeign clinnicfeign;

    @Autowired
    private AdminServiceClient adminServiceClient;

	@Autowired
	private NotificationFeign notificationFeign;

	@Autowired
	private ClinicAdminFeign clinicAdminFeign;

	@Autowired
	private geneateIds sequenceGeneratorService;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private WhatsAppService whatsAppService;
	
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	
	// Cap for unbounded list endpoints (see CHANGE 3 / getAllBookedServices)
	private static final int MAX_UNPAGED_RESULTS = 500;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * ✅ FIX 2: single null-safe source of truth for "which mobile number do we
	 * show". Replaces several call sites that did
	 * `!n.getPatientMobileNumber().isEmpty() ? ... : ...` with no null check,
	 * which threw NPEs the moment patientMobileNumber was null.
	 */

	@Override
	public ResponseEntity<Response> addService(BookingResponse request) {
		Response response = new Response();

		try {
			Booking updatedBooking = updateForFollowup(request);

			if (updatedBooking == null) {
				log.warn("No follow-up bookings found for request with bookingId={}", request.getBookingId());
				response.setSuccess(false);
				response.setMessage("No follow-up bookings found");
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			Map<String, Object> map = new LinkedHashMap<>();

			// ✅ Update doctor slot
			boolean slotUpdate = false;
			try {
				slotUpdate = clinnicfeign.updateDoctorSlotWhileBooking(
						updatedBooking.getDoctorId(),
						updatedBooking.getBranchId(),
						updatedBooking.getServiceDate(),
						updatedBooking.getServicetime()
				);
			} catch (Exception e) {
				log.error("Slot update failed for bookingId {}: {}", updatedBooking.getBookingId(), e.getMessage());
			}

			if (!slotUpdate) {
				response.setMessage("Appointment booking failed: slot not blocked");
				response.setSuccess(false);
				response.setStatus(HttpStatus.CONFLICT.value()); // or BAD_REQUEST if preferred
				return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
			}
			// ✅ Slot successfully blocked
			response.setMessage("Appointment Booked Successfully");

			if(updatedBooking.getFreeFollowUps() != 0){
				updatedBooking.setFreeFollowUpsLeft(updatedBooking.getFreeFollowUps()-1);
				repository.save(updatedBooking);}

			// ✅ Fetch branch details
			BranchDTO branch = null;
			try {
				ResponseEntity<ResponseStructure<BranchDTO>> branchResponse =
						adminServiceClient.getBranchById(updatedBooking.getBranchId());

				if (branchResponse != null
						&& branchResponse.getBody() != null
						&& branchResponse.getBody().getData() != null) {
					branch = branchResponse.getBody().getData();
				}
			} catch (Exception e) {
				log.error("Branch fetch failed for branchId {}: {}", updatedBooking.getBranchId(), e.getMessage());
			}

			// ✅ Populate booking details
			map.put("Doctor", updatedBooking.getDoctorName());
			map.put("Date", updatedBooking.getServiceDate());
			map.put("Time", updatedBooking.getServicetime());
			map.put("Booking ID", updatedBooking.getBookingId());
			map.put("Branch", updatedBooking.getBranchname());

			if (branch != null) {
				if (branch.getEmail() != null) {
					map.put("Email", branch.getEmail());
				}
				if (branch.getLocation() != null) {
					map.put("Location", branch.getLocation());
				} else {
					String locationUrl = "https://www.google.com/maps/search/?api=1&query="
							+ branch.getLatitude() + "," + branch.getLongitude();
					map.put("Location", locationUrl);
				}
				if (branch.getContactNumber() != null) {
					map.put("mobilenumber", branch.getContactNumber());
				}
			}

			map.put("patientmobilenumber", updatedBooking.getPatientMobileNumber());
			map.put("patientId", updatedBooking.getPatientId());
			map.put("patientname", updatedBooking.getName());

			response.setSuccess(true);
			response.setStatus(HttpStatus.OK.value());
			response.setData(map);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Exception occurred while processing addService for bookingId {}: {}", request.getBookingId(), e.getMessage(), e);
			response.setSuccess(false);
			response.setMessage("Internal error: " + e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}


	/**
	 * Clears large or sensitive fields from a Booking entity before returning it in API responses.
	 * This helps reduce payload size and avoids exposing unnecessary data.
	 */
	private void nullifyLargeFields(Booking booking) {
	    if (booking == null) {
	        log.warn("Attempted to nullify fields on a null Booking entity");
	        return;
	    }

	    try {
	        // ✅ Clear heavy collections
	        if (booking.getReports() != null) {
	            booking.setReports(null);
	            log.debug("Reports cleared for bookingId={}", booking.getBookingId());
	        }

	        if (booking.getAttachments() != null) {
	            booking.setAttachments(null);
	            log.debug("Attachments cleared for bookingId={}", booking.getBookingId());
	        }

	        // ✅ Clear large binary/pdf fields
	        if (booking.getConsentFormPdf() != null) {
	            booking.setConsentFormPdf(null);
	            log.debug("ConsentFormPdf cleared for bookingId={}", booking.getBookingId());
	        }

	        if (booking.getPrescriptionPdf() != null) {
	            booking.setPrescriptionPdf(null);
	            log.debug("PrescriptionPdf cleared for bookingId={}", booking.getBookingId());
	        }
	    } catch (Exception e) {
	        log.error("Error nullifying large fields for bookingId={}: {}", booking.getBookingId(), e.getMessage(), e);
	    }
	}


	private Booking toEntity(BookingRequset request) {
	    Booking entity = null;
	    try {
	        entity = mapper.convertValue(request, Booking.class);

	        // ✅ Default values
	        entity.setFollowupStatus("pending");
	        entity.setConsultationType("First-Time");

	        // ✅ Resolve customerId/patientId if missing
	        if ((request.getCustomerId() == null || request.getCustomerId().isEmpty()) ||
	            (request.getPatientId() == null || request.getPatientId().isEmpty())) {
	            try {
	                Map<String, String> res = clinnicfeign.getCustomerByMobilenumberAndName(
	                        request.getMobileNumber(), request.getName());
	                if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
	                    entity.setCustomerId(res.get("customerId"));
	                }
	                if (request.getPatientId() == null || request.getPatientId().isEmpty()) {
	                    entity.setPatientId(res.get("patientId"));
	                }
	            } catch (Exception e) {
	                log.warn("Failed to fetch customer/patient info: {}", e.getMessage());
	            }
	        }

	        // ✅ Follow-up status logic
	        entity.setFreeFollowUpsLeft(request.getFreeFollowUps());
	        if (request.getFreeFollowUps() != null && request.getFreeFollowUps() == 0) {
	            entity.setIsFollowupStatus(true);
	        } else {
	            entity.setIsFollowupStatus(false);
	        }

	        try {
	            if (request.getConsultationExpiration() != null) {
	                int days = Integer.parseInt(request.getConsultationExpiration().replaceAll("[^0-9]", ""));
	                LocalDate serviceDate = LocalDate.parse(request.getServiceDate());
	                LocalDate expiryDate = serviceDate.plusDays(days);
	                LocalDate today = LocalDate.now();

	                if (!today.isAfter(expiryDate) && request.getFreeFollowUps() != null && request.getFreeFollowUps() == 0) {
	                    entity.setIsFollowupStatus(true);
	                } else if (today.isAfter(expiryDate)) {
	                    entity.setIsFollowupStatus(true);
	                }
	            }
	        } catch (Exception e) {
	            log.warn("Consultation expiration parsing failed: {}", e.getMessage());
	            entity.setIsFollowupStatus(false);
	        }

	        // ✅ Generate custom booking ID
	        String bookingId = sequenceGeneratorService.generateBookingId(
	                request.getClinicName().substring(0, 3),
	                request.getBranchname().substring(0, 3));
	        entity.setBookingId(bookingId);

	        // ✅ Payment & status logic
	        if (request.getFoc() != null && request.getPaymentType() != null) {
	            if ("paid".equalsIgnoreCase(request.getFoc()) && "not paid".equalsIgnoreCase(request.getPaymentType())) {
	                entity.setStatus("pending");
	            } else if ("foc".equalsIgnoreCase(request.getFoc()) && "not paid".equalsIgnoreCase(request.getPaymentType())) {
	                entity.setStatus("confirmed");
	            } else if ("paid".equalsIgnoreCase(request.getFoc()) && !request.getPaymentType().isEmpty()) {
	                entity.setStatus("confirmed");
	            }
	        }

	        // ✅ Current status tracking
	        List<Status> statusList = new LinkedList<>();
	        Status s = new Status();
	        s.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
	        s.setStatus(entity.getStatus());
	        statusList.add(s);
	        entity.setCurrentStatus(statusList);

	        // ✅ Consultation fee tracking
	        if (request.getConsultationFee() != null ) {
	            ConsultationFees fee = new ConsultationFees();
	            fee.setConsulationFee(request.getConsultationFee());
	            fee.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
	            entity.setListOfConsultationFee(Collections.singletonList(fee));
	        }

	        // ✅ Follow-up bookings initialization
	        if (entity.getFollwupBookings() == null) {
	            FollowupBooking followup = new FollowupBooking();
	            followup.setDoctorId(entity.getDoctorId());
	            followup.setDoctorName(entity.getDoctorName());
	            followup.setServiceDate(entity.getServiceDate());
	            followup.setServicetime(entity.getServicetime());
	            followup.setStatus(entity.getStatus());
	            followup.setVisitType(entity.getVisitType());
	            entity.setFollwupBookings(Collections.singletonList(followup));
	        }

	    } catch (Exception e) {
	        log.error("Error converting BookingRequest to Booking entity: {}", e.getMessage(), e);
	        throw new RuntimeException("Failed to convert request to Booking entity", e);
	    }
	    return entity;
	}


	private BookingResponse toResponse(Booking entity) {
	    BookingResponse response = mapper.convertValue(entity, BookingResponse.class);

	    // ✅ Follow-up status
	    response.setIsFollowupStatus(entity.getIsFollowupStatus());

	    // ✅ Consultation fee check
	    if (entity.getListOfConsultationFee() != null && !entity.getListOfConsultationFee().isEmpty()) {
	        response.setConsultationFee(entity.getListOfConsultationFee().get(0).getConsulationFee());
	    }

	    // ✅ Prescription PDF
	    try {
	        String dto = getPrescriptionpdf(response.getBookingId());
	        if (dto != null) {
	            response.setPrescriptionPdf(Collections.singletonList(dto));
	        }
	    } catch (Exception e) {
	        log.warn("Prescription PDF error for bookingId={}: {}", response.getBookingId(), e.getMessage());
	    }

	    response.setBookingId(String.valueOf(entity.getBookingId()));

	    // ✅ S3 signed URLs
	 
	    try {
	        if (entity.getConsentFormPdf() != null && !entity.getConsentFormPdf().isEmpty()) {
	            response.setConsentFormPdf(s3Service.generateSignedUrl(entity.getConsentFormPdf()));
	        }
	    } catch (Exception e) {
	        log.warn("ConsentFormPdf URL error for bookingId={}: {}", entity.getBookingId(), e.getMessage());
	    }

	    try {
	        if (entity.getAttachments() != null && !entity.getAttachments().isEmpty()) {
	            List<String> signedUrls = entity.getAttachments().stream().map(key -> {
	                try {
	                    return s3Service.generateSignedUrl(key);
	                } catch (Exception ex) {
	                    log.warn("Attachment signing failed for key={} bookingId={}", key, entity.getBookingId());
	                    return key;
	                }
	            }).collect(Collectors.toList());
	            response.setAttachments(signedUrls);
	        }
	    } catch (Exception e) {
	        log.warn("Attachments URL error for bookingId={}: {}", entity.getBookingId(), e.getMessage());
	    }

	    // ✅ Reports signing via Clinic Admin
	    try {
	        if (response.getReports() != null) {
	            for (ReportsDtoList reportsDtoList : response.getReports()) {
	                if (reportsDtoList.getReportsList() == null) continue;
	                for (ReportsDTO report : reportsDtoList.getReportsList()) {
	                    if (report.getReportFile() == null || report.getReportFile().isEmpty()) continue;
	                    List<String> signedUrls = report.getReportFile().stream()
	                            .filter(key -> key != null && !key.isBlank())
	                            .map(key -> {
	                                try {
	                                    return clinicAdminFeign.getSignedUrl(key);
	                                } catch (Exception ex) {
	                                    log.warn("Report signing failed for key={} bookingId={}", key, entity.getBookingId());
	                                    return key;
	                                }
	                            }).collect(Collectors.toList());
	                    report.setReportFile(signedUrls);
	                }
	            }
	        }
	    } catch (Exception e) {
	        log.warn("Reports URL signing error for bookingId={}: {}", entity.getBookingId(), e.getMessage());
	    }

	    return response;
	}

	/**
	 * Retrieves the prescription PDF for a given bookingId and generates a signed S3 URL.
	 * Returns null if no prescription is found or if signing fails.
	 */
	private String getPrescriptionpdf(String bookingId) {
	    if (bookingId == null || bookingId.trim().isEmpty()) {
	        log.warn("getPrescriptionpdf called with null/empty bookingId");
	        return null;
	    }

	    try {
//	        String res = physioDoctorFeign.getByBookingId(bookingId);
//
//	        if (res != null && !res.isBlank()) {
//	            try {
//	                String signedUrl = s3Service.generateSignedUrl(res);
//	                log.debug("Prescription PDF signed successfully for bookingId={}", bookingId);
//	                return signedUrl;
//	            } catch (Exception ex) {
//	                log.error("Failed to sign prescription PDF for bookingId={} : {}", bookingId, ex.getMessage(), ex);
//	                return res; // fallback to raw key if signing fails
//	            }
//	        } else {
//	            log.info("No prescription PDF found for bookingId={}", bookingId);
//	            return null;
//	        }

	    } catch (Exception e) {
	        log.error("Error fetching prescription PDF for bookingId={} : {}", bookingId, e.getMessage(), e);
	        return null;
	    }
		return null;
	}


	private static String randomNumber() {
		Random random = new Random();
		int sixDigitNumber = 100000 + random.nextInt(900000);
		return String.valueOf(sixDigitNumber);
	}

	
	private List<BookingResponse> toResponses(List<Booking> bookings) {
	    if (bookings == null || bookings.isEmpty()) {
	        return new ArrayList<>();
	    }

	    List<BookingResponse> responses;
	    try {
	        responses = new ArrayList<>(mapper.convertValue(bookings, new TypeReference<List<BookingResponse>>() {}));
	    } catch (Exception e) {
	        log.error("Failed to map bookings to BookingResponse: {}", e.getMessage(), e);
	        throw new RuntimeException("Failed to convert bookings list", e);
	    }

	    for (BookingResponse bres : responses) {
	      
	        // ✅ Consent Form PDF
	        try {
	            if (bres.getConsentFormPdf() != null && !bres.getConsentFormPdf().isEmpty()) {
	                bres.setConsentFormPdf(s3Service.generateSignedUrl(bres.getConsentFormPdf()));
	            }
	        } catch (Exception e) {
	            log.warn("ConsentFormPdf URL error for bookingId={}: {}", bres.getBookingId(), e.getMessage());
	        }

	        // ✅ Attachments
	        try {
	            if (bres.getAttachments() != null && !bres.getAttachments().isEmpty()) {
	                List<String> signedUrls = bres.getAttachments().stream().map(key -> {
	                    try {
	                        return s3Service.generateSignedUrl(key);
	                    } catch (Exception ex) {
	                        log.warn("Attachment signing failed for key={} bookingId={}", key, bres.getBookingId());
	                        return key;
	                    }
	                }).collect(Collectors.toList());
	                bres.setAttachments(signedUrls);
	            }
	        } catch (Exception e) {
	            log.warn("Attachments URL error for bookingId={}: {}", bres.getBookingId(), e.getMessage());
	        }

	        // ✅ Reports signing via Clinic Admin
	        try {
	            if (bres.getReports() != null) {
	                for (ReportsDtoList reportsDtoList : bres.getReports()) {
	                    if (reportsDtoList.getReportsList() == null) continue;
	                    for (ReportsDTO report : reportsDtoList.getReportsList()) {
	                        if (report.getReportFile() == null || report.getReportFile().isEmpty()) continue;
	                        List<String> signedUrls = report.getReportFile().stream()
	                                .filter(key -> key != null && !key.isBlank())
	                                .map(key -> {
	                                    try {
	                                        return clinicAdminFeign.getSignedUrl(key);
	                                    } catch (Exception ex) {
	                                        log.warn("Report signing failed for key={} bookingId={}", key, bres.getBookingId());
	                                        return key;
	                                    }
	                                }).collect(Collectors.toList());
	                        report.setReportFile(signedUrls);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            log.warn("Reports URL signing error for bookingId={}: {}", bres.getBookingId(), e.getMessage());
	        }

	        // ✅ Prescription PDF
	        try {
	            String dto = getPrescriptionpdf(bres.getBookingId());
	            if (dto != null) {
	                bres.setPrescriptionPdf(Collections.singletonList(dto));
	            }
	        } catch (Exception e) {
	            log.warn("PrescriptionPdf error for bookingId={}: {}", bres.getBookingId(), e.getMessage());
	        }
	    }

	    return responses;
	}



	@Override
	public ResponseEntity<?> physioAppointment(BookingRequset request) {
	    Response res = new Response();

	    try {
	        // =========================
	        // VALIDATIONS
	        // =========================
	        if (request.getFreeFollowUps() == null) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Free FollowUps is mandatory");
	        }
	        if (request.getClinicId() == null || request.getClinicId().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clinic Id is mandatory");
	        }
	        if (request.getBranchId() == null || request.getBranchId().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch Id is mandatory");
	        }
	        if (request.getDoctorId() == null || request.getDoctorId().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor Id is mandatory");
	        }
	        if (request.getServiceDate() == null || request.getServiceDate().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service Date is mandatory");
	        }
	        if (request.getServicetime() == null || request.getServicetime().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service Time is mandatory");
	        }
	        if (request.getConsultationExpiration() == null || request.getConsultationExpiration().trim().isEmpty()) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consultation Expiration is mandatory");
	        }
	        boolean hasPatientMobile = request.getPatientMobileNumber() != null && !request.getPatientMobileNumber().trim().isEmpty();
	        boolean hasMobile = request.getMobileNumber() != null && !request.getMobileNumber().trim().isEmpty();
	        if (!hasPatientMobile && !hasMobile) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient Mobile Number or Mobile Number is mandatory");
	        }

	        // =========================
	        // SAVE BOOKING
	        // =========================
	        Booking entity = toEntity(request);
	        Booking updatedBooking = repository.save(entity);
	        if (updatedBooking == null) {
	            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save appointment");
	        }

	        // =========================
	        // Notification Service
	        // =========================
	        int notificationStatus = 0;
	        try {
	            Response notificationResponse = notificationService
	                    .createNotification(mapper.convertValue(updatedBooking, BookingResponse.class))
	                    .getBody();
	            if (notificationResponse != null) {
	                notificationStatus = notificationResponse.getStatus();
	            }
	        } catch (Exception e) {
	            log.warn("Notification service failed for booking {} : {}", updatedBooking.getBookingId(), e.getMessage());
	        }

	        // =========================
	        // WhatsApp Notification
	        // =========================
	        // ⛔ Disabled: not in use currently. Left commented (not removed)
	        // so it can be re-enabled without rewiring WhatsAppService again.
	        /*
	        try {
	            request.setBookingId(updatedBooking.getBookingId());
	            request.setClinicId(updatedBooking.getClinicId());
	            request.setBranchId(updatedBooking.getBranchId());

	            whatsAppService.sendBookingConfirmation(request);
	            log.info("WhatsApp sent successfully for booking {}", updatedBooking.getBookingId());
	        } catch (Exception e) {
	            log.warn("WhatsApp notification failed for booking {} : {}", updatedBooking.getBookingId(), e.getMessage());
	        }
	        */

	        // =========================
	        // SUCCESS RESPONSE
	        // =========================
	        res.setStatus(200);
	        res.setSuccess(true);
            Map<String,String> map = new LinkedHashMap<>();
	        try {
	         boolean slotupdate = 
	        		 clinnicfeign.updateDoctorSlotWhileBooking(         
	        				 request.getDoctorId(),
	        				 request.getBranchId(),
	        				 request.getServiceDate(),
	        				 request.getServicetime()
		            );
	         if(slotupdate) {
	        	  res.setMessage("Appointment Booked Successfully and slot blocked and");
                 map.put("slotStatus","200");
	         }else {
                 map.put("slotStatus","500");
	        	  res.setMessage("Appointment Booked Successfully and slot not blocked and");
	         }
                if (notificationStatus == 200 ) {
                    res.setMessage(res.getMessage()+" notification sent");
                } else {
                    res.setMessage(res.getMessage()+" Notification not sent");
                }}catch(Exception e) {}
                BranchDTO branch = null;
                try {
                    ResponseEntity<ResponseStructure<BranchDTO>> response =
                            adminServiceClient.getBranchById(updatedBooking.getBranchId());

                    if (response != null
                            && response.getBody() != null
                            && response.getBody().getData() != null) {
                        branch = response.getBody().getData();
                    }

                } catch (Exception e) {
                    log.error("Branch fetch failed: {}", e.getMessage(), e);
                }
            map.put("Doctor",updatedBooking.getDoctorName());
            map.put("Date",updatedBooking.getServiceDate());
            map.put("Time",updatedBooking.getServicetime());
            map.put("Booking ID",updatedBooking.getBookingId());
            map.put("Branch",updatedBooking.getBranchname());
            if(branch != null && branch.getEmail()!=null ){
                map.put("Email",branch.getEmail());}
            if(branch != null && branch.getLocation()!= null){
                map.put("Location",branch.getLocation());}
            else{
                if(branch != null){
                    String locationUrl =
                            "https://www.google.com/maps/search/?api=1&query="
                                    + branch.getLatitude()
                                    + ","
                                    + branch.getLongitude();
                    map.put("Location",locationUrl);}
            }
            if(branch != null && branch.getContactNumber()!=null){
                map.put("mobilenumber",branch.getContactNumber());}
            map.put("patientmobilenumber",updatedBooking.getPatientMobileNumber());
            map.put("patientId",updatedBooking.getPatientId());
            map.put("patientname",updatedBooking.getName());
                res.setData(map);

	        return ResponseEntity.ok(res);

	    } catch (ResponseStatusException e) {
	        log.error("Validation failed: {}", e.getReason());
	        res.setStatus(e.getStatusCode().value());
	        res.setSuccess(false);
	        res.setMessage(e.getReason());
	        return ResponseEntity.status(e.getStatusCode()).body(res);
	    } catch (Exception e) {
	        log.error("Appointment booking failed : {}", e.getMessage(), e);
	        res.setStatus(500);
	        res.setSuccess(false);
	        res.setMessage("Internal error: " + e.getMessage());
	        return ResponseEntity.status(500).body(res);
	    }
	}


	@Override
	public ResponseEntity<?> getAppointsByPatientId(String patientId) {
	    ResponseStructure<List<Map<String, Object>>> res = new ResponseStructure<>();
	    List<Map<String, Object>> list = new ArrayList<>();

	    try {
	        // ✅ Case-insensitive repository method
	        List<Booking> existingBookings = repository.findByPatientIdIgnoreCase(patientId);

	        if (existingBookings == null || existingBookings.isEmpty()) {
	            log.warn("No appointments found for patientId={}", patientId);
	            res.setStatusCode(200);
	            res.setMessage("Appointments Are Not Found");
	            res.setData(Collections.emptyList());
	            return ResponseEntity.ok(res);
	        }

	        log.info("Repository returned {} bookings for patientId={}", existingBookings.size(), patientId);

	        List<BookingResponse> responses = mapper.convertValue(
	            existingBookings,
	            new TypeReference<List<BookingResponse>>() {}
	        );

	        responses.forEach(n -> {
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("bookingId", n.getBookingId());
	            map.put("serviceDate", n.getServiceDate());
	            map.put("servicetime", n.getServicetime());
	            map.put("name", n.getName());
	            map.put("mobileNumber", resolveMobileNumber(n));
	            map.put("doctorId", n.getDoctorId());
	            map.put("doctorName", n.getDoctorName());
	            map.put("paymentType", n.getPaymentType());
	            map.put("visitType", n.getVisitType());
	            map.put("status", n.getStatus());
	            map.put("followupStatus", n.getFollowupStatus());
	            map.put("patientId", n.getPatientId());
	            map.put("clinicId", n.getClinicId());
	            map.put("customerId", n.getCustomerId());
	            map.put("branchId", n.getBranchId());
	            map.put("age", n.getAge());
	            map.put("gender", n.getGender());
	            map.put("branchName", n.getBranchname());
	            map.put("session", n.getSession());
	            map.put("problem", n.getProblem());
	            map.put("consultationfee", n.getConsultationFee());
	            map.put("freeFollowUps", n.getFreeFollowUps());
	            map.put("address", n.getPatientAddress());
	            map.put("freeFollowupsLeft", n.getFreeFollowUpsLeft());

	            try {
	                Response response = clinicAdminFeign.getSoapNotesByBooking(n.getBookingId()).getBody();
	                SoapNoteDTO notes = response != null
	                        ? mapper.convertValue(response.getData(), SoapNoteDTO.class)
	                        : null;

	                Map<String, Double> info = clinicAdminFeign.getAllPayments(n.getBookingId());
	                if (info != null) {
	                    if (info.containsKey("finalAmount")) map.put("finalAmount", info.get("finalAmount"));
	                    if (info.containsKey("paidAmount")) map.put("paidAmount", info.get("paidAmount"));
	                    if (info.containsKey("dueAmount")) map.put("dueAmount", info.get("dueAmount"));
	                    if (info.containsKey("discount")) map.put("discount", info.get("discount"));
	                    if (info.containsKey("actualAccount")) map.put("actualAmount", info.get("actualAccount"));
	                }
	                if (notes != null) {
	                    map.put("sitting", n.getSittingNumber());
	                    map.put("packageType", notes.getPackageType());
	                    map.put("packageId", notes.getPackageId());
	                    map.put("packageName", notes.getPackageName());
	                    map.put("noOfSittings", notes.getNumberOfSittings());
	                }

	            } catch (Exception e) {
	                log.error("Error fetching SOAP notes or payments for bookingId={}: {}", n.getBookingId(), e.getMessage());
	            }

	            list.add(map);
	        });

	        res.setStatusCode(200);
	        res.setMessage("Appointments Are Found");
	        res.setData(list);
	        log.info("Returning {} appointments for patientId={}", list.size(), patientId);

	        return ResponseEntity.ok(res);

	    } catch (Exception e) {
	        log.error("Error fetching appointments for patientId={}: {}", patientId, e.getMessage(), e);
	        res.setStatusCode(500);
	        res.setMessage("Internal error: " + e.getMessage());
	        res.setData(Collections.emptyList());
	        return ResponseEntity.status(500).body(res);
	    }
	}


	@Override
	public ResponseEntity<?> getAppointsByInput(String input) {
	    ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();

	    try {
	        // ✅ Uses patientId and bookingId indexes; name regex is slower
	        List<Booking> existingBookings = repository.findByNameIgnoreCaseOrBookingIdOrPatientId(input);

	        if (existingBookings == null || existingBookings.isEmpty()) {
	            log.warn("No appointments found for input={}", input);
	            res.setStatusCode(200);
	            res.setMessage("Appointments Are Not Found");
	            res.setData(Collections.emptyList());
	            return ResponseEntity.ok(res);
	        }

	        List<BookingResponse> responses = mapper.convertValue(existingBookings, new TypeReference<List<BookingResponse>>() {});

	        res.setStatusCode(200);
	        res.setMessage("Appointments Are Found");
	        res.setData(responses);
	        log.info("Found {} appointments for input={}", responses.size(), input);

	        return ResponseEntity.ok(res);

	    } catch (Exception e) {
	        log.error("Error fetching appointments for input={}: {}", input, e.getMessage(), e);
	        res.setStatusCode(500);
	        res.setMessage("Internal error: " + e.getMessage());
	        res.setData(Collections.emptyList());
	        return ResponseEntity.status(500).body(res);
	    }
	}

	@Override
	public ResponseEntity<?> getTodayDoctorAppointmentsByDoctorId(String clinicId, String doctorId) {
	    ResponseStructure<List<Map<String, Object>>> res = new ResponseStructure<>();
	    List<Map<String, Object>> list = new ArrayList<>();

	    try {
	        // ✅ Uses compound index (clinicId, doctorId, serviceDate)
	        List<Booking> existingBookings = repository.findByClinicIdAndDoctorId(clinicId, doctorId);

	        if (existingBookings == null || existingBookings.isEmpty()) {
	            log.warn("No appointments found for clinicId={} and doctorId={} on today", clinicId, doctorId);
	            res.setStatusCode(200);
	            res.setMessage("Appointments Are Not Found");
	            res.setData(Collections.emptyList());
	            return ResponseEntity.ok(res);
	        }

	        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        LocalDate currentDate = LocalDate.now();

	        List<BookingResponse> responseList = new ArrayList<>();

	        for (Booking b : existingBookings) {
	            try {
	                if (b.getServiceDate() != null && b.getStatus() != null) {
	                    LocalDate bookingDate = LocalDate.parse(b.getServiceDate(), dateFormatter);

	                    if (bookingDate.equals(currentDate) &&
	                        (b.getStatus().equalsIgnoreCase("Confirmed") || b.getStatus().equalsIgnoreCase("pending"))) {
	                        BookingResponse temp = toResponse(b);
	                        responseList.add(temp);
	                    }
	                }
	            } catch (Exception e) {
	                log.error("Error parsing serviceDate for bookingId={}: {}", b.getBookingId(), e.getMessage());
	            }
	        }

	        if (responseList.isEmpty()) {
	            log.info("No confirmed/pending appointments found for clinicId={} and doctorId={} today", clinicId, doctorId);
	            res.setStatusCode(200);
	            res.setMessage("Appointments Are Not Found");
	            res.setData(Collections.emptyList());
	            return ResponseEntity.ok(res);
	        }

	        // ✅ Map BookingResponse → simplified map for API response
	        responseList.forEach(n -> {
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("bookingId", n.getBookingId());
	            map.put("serviceDate", n.getServiceDate());
	            map.put("servicetime", n.getServicetime());
	            map.put("name", n.getName());
	            map.put("mobileNumber", resolveMobileNumber(n));
	            map.put("doctorId", n.getDoctorId());
	            map.put("doctorName", n.getDoctorName());
	            map.put("paymentType", n.getPaymentType());
	            map.put("visitType", n.getVisitType());
	            map.put("status", n.getStatus());
	            map.put("followupStatus", n.getFollowupStatus());
	            map.put("patientId", n.getPatientId());
	            map.put("clinicId", n.getClinicId());
	            map.put("customerId", n.getCustomerId());
	            map.put("branchId", n.getBranchId());
	            map.put("age", n.getAge());
	            map.put("gender", n.getGender());
	            map.put("branchName", n.getBranchname());
	            map.put("problem", n.getProblem());

	            // Extra fields
	            map.put("consultationFee", n.getConsultationFee());
	            map.put("freeFollowUpsLeft", n.getFreeFollowUpsLeft());
	            map.put("freeFollowUps", n.getFreeFollowUps());

	            list.add(map);
	        });

	        res.setStatusCode(200);
	        res.setMessage("Appointments Are Found");
	        res.setData(list);
	        log.info("Found {} appointments for clinicId={} and doctorId={} today", list.size(), clinicId, doctorId);

	        return ResponseEntity.ok(res);

	    } catch (Exception e) {
	        log.error("Error fetching today's appointments for clinicId={} and doctorId={}: {}", clinicId, doctorId, e.getMessage(), e);
	        res.setStatusCode(500);
	        res.setMessage("Internal error: " + e.getMessage());
	        res.setData(Collections.emptyList());
	        return ResponseEntity.status(500).body(res);
	    }
	}


	@Override
	public ResponseEntity<?> filterDoctorAppointmentsByDoctorId(String hospitalId, String doctorId, String number) {

		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		List<BookingResponse> responses = new ArrayList<>();

		try {

			List<Booking> bookings = repository.findByClinicIdAndDoctorId(hospitalId, doctorId);

			if (bookings == null || bookings.isEmpty()) {
				res.setStatusCode(200);
				res.setData(responses);
				res.setMessage("Appointments Are Not Found");
				return ResponseEntity.ok(res);
			}

			LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

			for (Booking booking : bookings) {

				if (booking.getServiceDate() == null) {
					continue;
				}

				LocalDate appointmentDate = LocalDate.parse(booking.getServiceDate());

				boolean add = false;

				switch (number) {

				// Upcoming
				case "1":
					add = "Confirmed".equalsIgnoreCase(booking.getStatus()) && appointmentDate.isAfter(today);
					break;

				// Upcoming Online
				case "2":
					add = "Online Consultation".equalsIgnoreCase(booking.getConsultationType())
							&& "Confirmed".equalsIgnoreCase(booking.getStatus()) && appointmentDate.isAfter(today);
					break;

				// Completed
				case "3":
					add = "Completed".equalsIgnoreCase(booking.getStatus());
					break;

				// In Progress
				case "4":
					add = "In-Progress".equalsIgnoreCase(booking.getStatus());
					break;
				}

				if (add) {
					responses.add(toResponse(booking));
				}
			}

			res.setStatusCode(200);
			res.setData(responses);
			res.setMessage(responses.isEmpty() ? "Appointments Are Not Found" : "Appointments Are Found");

		} catch (Exception e) {

			res.setStatusCode(500);
			res.setData(null);
			res.setMessage(e.getMessage());
		}

		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	public ResponseEntity<?> getCompletedApntsByDoctorId(String hospitalId, String doctorId) {
		Map<String, Object> m = new LinkedHashMap<>();
		try {
			List<Booking> existingBooking = repository.findByClinicIdAndDoctorId(hospitalId, doctorId);
			List<BookingResponse> res = new ArrayList<>();
			if (existingBooking != null) {
				for (Booking b : existingBooking) {
					if (b.getStatus() != null && b.getStatus().equalsIgnoreCase("Completed")) {
						res.add(toResponse(b));
					}
				}
				m.put("completedAppointmentsCount", res.size());
				m.put("status", 200);
				return ResponseEntity.status(200).body(m);
			} else {
				m.put("Message", "No Appointsments Found");
				m.put("status", 200);
				return ResponseEntity.status(200).body(m);
			}
		} catch (Exception e) {
			m.put("Message", e.getMessage());
			m.put("status", 500);
			return ResponseEntity.status(500).body(m);
		}
	}

	public ResponseEntity<?> getSizeOfConsultationTypesByDoctorId(String hospitalId, String doctorId) {
		Map<String, Object> m = new LinkedHashMap<>();
		try {
			List<Booking> existingBooking = repository.findByClinicIdAndDoctorId(hospitalId, doctorId);
			List<BookingResponse> servicesAndConsul = new ArrayList<>();
			List<BookingResponse> inClinic = new ArrayList<>();
			List<BookingResponse> online = new ArrayList<>();
			if (existingBooking != null) {
				for (Booking b : existingBooking) {
					if (b.getStatus() != null && b.getStatus().equalsIgnoreCase("Completed")
							&& b.getConsultationType() != null) {
						if (b.getConsultationType().equalsIgnoreCase("Services & Treatments")) {
							servicesAndConsul.add(toResponse(b));
						}
						if (b.getConsultationType().equalsIgnoreCase("In-Clinic Consultation")) {
							inClinic.add(toResponse(b));
						}
						if (b.getConsultationType().equalsIgnoreCase("Online Consultation")) {
							online.add(toResponse(b));
						}
					}
				}
				m.put("services & Treatments", servicesAndConsul.size());
				m.put("in-Clinic Consultation", inClinic.size());
				m.put("online Consultation", online.size());
				m.put("status", 200);
				return ResponseEntity.status(200).body(m);
			} else {
				m.put("Message", "No Appointsments Found");
				m.put("status", 200);
				return ResponseEntity.status(200).body(m);
			}
		} catch (Exception e) {
			m.put("Message", e.getMessage());
			m.put("status", 500);
			return ResponseEntity.status(500).body(m);
		}
	}

	public Map<String, Object> getBookedService(String bookingId) {
		Map<String, Object> map = new LinkedHashMap<>();
		try {
			Optional<Booking> optionalEntity = repository.findByBookingId(bookingId);
			if (optionalEntity.isPresent()) {
				Booking entity = optionalEntity.get();
				BookingResponse n = toResponse(entity);
				
				map.put("email", n.getEmail());
				map.put("age", n.getAge());
				map.put("gender", n.getGender());
				map.put("bookingId", n.getBookingId());
				map.put("serviceDate", n.getServiceDate());
				map.put("servicetime", n.getServicetime());
				map.put("name", n.getName());
				map.put("address", n.getPatientAddress());
				map.put("mobileNumber", resolveMobileNumber(n));
				map.put("doctorId", n.getDoctorId());
				map.put("doctorName", n.getDoctorName());
				map.put("paymentType", n.getPaymentType());
				map.put("visitType", n.getVisitType());
				map.put("status", n.getStatus());
				map.put("followupStatus", n.getFollowupStatus());
				map.put("patientId", n.getPatientId());
				map.put("clinicId", n.getClinicId());
				map.put("customerId", n.getCustomerId());
				map.put("branchId", n.getBranchId());
				map.put("session", n.getSession());
				map.put("problem", n.getProblem());
				map.put("consultationfee", n.getConsultationFee());
				map.put("freeFollowUps", n.getFreeFollowUps());
				map.put("freeFollowupsLeft", n.getFreeFollowUpsLeft());
			    try {
			        if (entity.getConsentFormPdf() != null && !entity.getConsentFormPdf().isEmpty()) {
			            n.setConsentFormPdf(s3Service.generateSignedUrl(entity.getConsentFormPdf()));
			        }
			    } catch (Exception e) {
			        log.warn("ConsentFormPdf URL error for bookingId={}: {}", entity.getBookingId(), e.getMessage());
			    }
			    map.put("consentFormPdf", n.getConsentFormPdf());
				

				try {
					Response response = clinicAdminFeign.getSoapNotesByBooking(n.getBookingId()).getBody();
					SoapNoteDTO notes = response != null
							? mapper.convertValue(response.getData(), SoapNoteDTO.class)
							: null;

					Map<String, Double> info = clinicAdminFeign.getAllPayments(n.getBookingId());
					if (info != null) {
						if (info.containsKey("finalAmount")) map.put("finalAmount", info.get("finalAmount"));
						if (info.containsKey("paidAmount")) map.put("paidAmount", info.get("paidAmount"));
						if (info.containsKey("dueAmount")) map.put("dueAmount", info.get("dueAmount"));
						if (info.containsKey("discount")) map.put("discount", info.get("discount"));
						if (info.containsKey("actualAccount")) map.put("actualAmount", info.get("actualAccount"));
					}
					if (notes != null) {
						map.put("sitting", n.getSittingNumber());
						map.put("packageType", notes.getPackageType());
						map.put("packageId", notes.getPackageId());
						map.put("packageName", notes.getPackageName());
						map.put("noOfSittings", notes.getNumberOfSittings());
					}
				} catch (Exception e) {
					log.warn("Error fetching SOAP notes or payments for bookingId={}: {}", n.getBookingId(), e.getMessage());
				}

				return map;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Error in getBookedService for bookingId={}: {}", bookingId, e.getMessage(), e);
			return null;
		}
	}

	public void deleteBookedServiceReports(String bookingId, String index) {
		try {
			Optional<Booking> optionalEntity = repository.findByBookingId(bookingId);
			if (optionalEntity.isEmpty()) {
				return;
			}
			Booking entity = optionalEntity.get();
			if (entity.getReports() == null) {
				return;
			}
			if (index != null && index.equalsIgnoreCase("null")) {
				try {
					entity.getReports().clear();
					repository.save(entity);
				} catch (Exception e) {
					log.warn("Failed to clear reports for bookingId={}: {}", bookingId, e.getMessage());
				}
			} else if (index != null) {
				try {
					entity.getReports().remove(Integer.valueOf(index).intValue());
					repository.save(entity);
				} catch (Exception e) {
					log.warn("Failed to remove report at index={} for bookingId={}: {}", index, bookingId, e.getMessage());
				}
			}
		} catch (Exception e) {
			log.error("Error in deleteBookedServiceReports for bookingId={}: {}", bookingId, e.getMessage(), e);
		}
	}

    @Override
    public BookingResponse deleteService(String id) {
        try {
            Booking entity = repository.findByBookingId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Booking Id. Please provide a valid Id"));

            repository.deleteById(id);

                try {
                    clinnicfeign.makingFalseDoctorSlot(
                            entity.getDoctorId(),
                            entity.getBranchId(),
                            entity.getServiceDate(),
                            entity.getServicetime()
                    );
                } catch (Exception e) {
                    // ✅ Don't fail the whole status update just because the downstream slot-release call had trouble
                    log.warn("Failed to release doctor slot for bookingId={}: {}", entity.getBookingId(), e.getMessage(), e);
                }

            return toResponse(entity);

        } catch (IllegalArgumentException e) {
            // Specific exception for invalid booking id
            log.error("Delete failed: {}", e.getMessage(), e);
            throw e; // rethrow so caller knows it's a bad request

        } catch (Exception e) {
            // Catch-all for unexpected errors
            log.error("Unexpected error while deleting booking with id={}", id, e);
            throw new RuntimeException("Error occurred while deleting booking: " + e.getMessage());
        }
    }


    @Override
	public List<BookingResponse> getBookedServices(String mobileNumber) {
		List<Booking> bookings = repository.findByMobileNumber(mobileNumber);
		if (bookings == null || bookings.isEmpty()) {
			return null;
		}
		List<Booking> reversedBookings = new ArrayList<>();
		for (int i = bookings.size() - 1; i >= 0; i--) {
			reversedBookings.add(bookings.get(i));
		}
		return toResponses(reversedBookings);
	}

	
	@Override
	public List<BookingResponse> getAllBookedServices() {
		List<Booking> bookings = repository.findAll(PageRequest.of(0, MAX_UNPAGED_RESULTS)).getContent();
		if (bookings == null || bookings.isEmpty()) {
			return null;
		}
		List<Booking> reversedBookings = new ArrayList<>();
		for (int i = bookings.size() - 1; i >= 0; i--) {
			reversedBookings.add(bookings.get(i));
		}
		return toResponses(reversedBookings);
	}

	@Override
	public List<BookingResponse> bookingByDoctorId(String doctorId) {
		List<Booking> bookings = repository.findByDoctorId(doctorId);
		if (bookings == null || bookings.isEmpty()) {
			return null;
		}
		List<Booking> reversedBookings = new ArrayList<>();
		for (int i = bookings.size() - 1; i >= 0; i--) {
			reversedBookings.add(bookings.get(i));
		}
		return toResponses(reversedBookings);
	}

	@Override
	public List<Map<String, Object>> bookingByCustomerId(String customerId) {

		List<Booking> bookings = repository.findByCustomerId(customerId);

		if (bookings == null || bookings.isEmpty()) {
			return Collections.emptyList();
		}

		bookings = bookings.stream()
				.filter(booking -> !"COMPLETED".equalsIgnoreCase(booking.getStatus()))
				.toList();

		List<BookingResponse> reversedBookings = toResponses(bookings);

		List<Map<String, Object>> list = new ArrayList<>();

		reversedBookings.forEach(n -> {
			Map<String, Object> map = new LinkedHashMap<>();

			map.put("bookingId", n.getBookingId());
			map.put("serviceDate", n.getServiceDate());
			map.put("servicetime", n.getServicetime());
			map.put("name", n.getName());
			map.put("mobileNumber", resolveMobileNumber(n));
			map.put("doctorId", n.getDoctorId());
			map.put("doctorName", n.getDoctorName());
			map.put("paymentType", n.getPaymentType());
			map.put("visitType", n.getVisitType());
			map.put("status", n.getStatus());
			map.put("followupStatus", n.getFollowupStatus());
			map.put("patientId", n.getPatientId());
			map.put("clinicId", n.getClinicId());
			map.put("customerId", n.getCustomerId());
			map.put("branchId", n.getBranchId());
			map.put("age", n.getAge());
			map.put("gender", n.getGender());
			map.put("branchName", n.getBranchname());
			map.put("problem", n.getProblem());

			list.add(map);
		});

		return list;
	}

	@Override
	public List<Map<String, Object>> CompletedbookingByCustomerId(String customerId) {

		List<Booking> bookings = repository.findByCustomerId(customerId);

		if (bookings == null || bookings.isEmpty()) {
			return Collections.emptyList();
		}

		bookings = bookings.stream()
				.filter(booking -> "COMPLETED".equalsIgnoreCase(booking.getStatus()))
				.toList();

		List<BookingResponse> reversedBookings = toResponses(bookings);

		List<Map<String, Object>> list = new ArrayList<>();

		reversedBookings.forEach(n -> {
			Map<String, Object> map = new LinkedHashMap<>();

			map.put("bookingId", n.getBookingId());
			map.put("serviceDate", n.getServiceDate());
			map.put("servicetime", n.getServicetime());
			map.put("name", n.getName());
			map.put("mobileNumber", resolveMobileNumber(n));
			map.put("doctorId", n.getDoctorId());
			map.put("doctorName", n.getDoctorName());
			map.put("paymentType", n.getPaymentType());
			map.put("visitType", n.getVisitType());
			map.put("status", n.getStatus());
			map.put("followupStatus", n.getFollowupStatus());
			map.put("patientId", n.getPatientId());
			map.put("clinicId", n.getClinicId());
			map.put("customerId", n.getCustomerId());
			map.put("branchId", n.getBranchId());
			map.put("age", n.getAge());
			map.put("gender", n.getGender());
			map.put("branchName", n.getBranchname());
			map.put("problem", n.getProblem());

			list.add(map);
		});

		return list;
	}

	@Override
	public ResponseEntity<?> bookingByPatientId(String patientId) {
	    ResponseStructure<List<Map<String, Object>>> response = new ResponseStructure<>();
	    List<Map<String, Object>> resultList = new LinkedList<>();

	    try {
	        // ✅ Case-insensitive repository method
	        List<Booking> bookings = repository.findByPatientIdIgnoreCase(patientId);

	        if (bookings == null || bookings.isEmpty()) {
	            log.warn("No appointments found for patientId={}", patientId);
	            response.setStatusCode(200);
	            response.setMessage("Appointments Are Not Found");
	            response.setData(Collections.emptyList());
	            return ResponseEntity.ok(response);
	        }

	        log.info("Repository returned {} bookings for patientId={}", bookings.size(), patientId);

	        List<BookingResponse> res = toResponses(bookings);

	        res.forEach(n -> {
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("bookingId", n.getBookingId());
	            map.put("serviceDate", n.getServiceDate());
	            map.put("servicetime", n.getServicetime());
	            map.put("name", n.getName());
	            map.put("mobileNumber", resolveMobileNumber(n));
	            map.put("doctorId", n.getDoctorId());
	            map.put("doctorName", n.getDoctorName());
	            map.put("paymentType", n.getPaymentType());
	            map.put("visitType", n.getVisitType());
	            map.put("status", n.getStatus());
	            map.put("followupStatus", n.getFollowupStatus());
	            map.put("patientId", n.getPatientId());
	            map.put("clinicId", n.getClinicId());
	            map.put("customerId", n.getCustomerId());
	            map.put("branchId", n.getBranchId());
	            map.put("age", n.getAge());
	            map.put("gender", n.getGender());
	            map.put("branchName", n.getBranchname());
	            map.put("session", n.getSession());
	            map.put("problem", n.getProblem());
	            map.put("address", n.getPatientAddress());
	            map.put("consultationfee", n.getConsultationFee());
	            map.put("freeFollowUps", n.getFreeFollowUps());
	            map.put("freeFollowupsLeft", n.getFreeFollowUpsLeft());

	            try {
	                Response soapResponse = clinicAdminFeign.getSoapNotesByBooking(n.getBookingId()).getBody();
	                SoapNoteDTO notes = soapResponse != null
	                        ? mapper.convertValue(soapResponse.getData(), SoapNoteDTO.class)
	                        : null;

	                Map<String, Double> info = clinicAdminFeign.getAllPayments(n.getBookingId());
	                if (info != null) {
	                    if (info.containsKey("finalAmount")) map.put("finalAmount", info.get("finalAmount"));
	                    if (info.containsKey("paidAmount")) map.put("paidAmount", info.get("paidAmount"));
	                    if (info.containsKey("dueAmount")) map.put("dueAmount", info.get("dueAmount"));
	                    if (info.containsKey("discount")) map.put("discount", info.get("discount"));
	                    if (info.containsKey("actualAccount")) map.put("actualAmount", info.get("actualAccount"));
	                }
	                if (notes != null) {
	                    map.put("sitting", n.getSittingNumber());
	                    map.put("packageType", notes.getPackageType());
	                    map.put("packageId", notes.getPackageId());
	                    map.put("packageName", notes.getPackageName());
	                    map.put("noOfSittings", notes.getNumberOfSittings());
	                }

	            } catch (Exception e) {
	                log.error("Error fetching SOAP notes or payments for bookingId={}: {}", n.getBookingId(), e.getMessage());
	            }

	            resultList.add(map);
	        });

	        response.setStatusCode(200);
	        response.setMessage("Appointments Are Found");
	        response.setData(resultList);
	        log.info("Returning {} appointments for patientId={}", resultList.size(), patientId);

	        return ResponseEntity.ok(response);

	    } catch (Exception ex) {
	        log.error("Error fetching appointments for patientId={}: {}", patientId, ex.getMessage(), ex);
	        response.setStatusCode(500);
	        response.setMessage("Internal error: " + ex.getMessage());
	        response.setData(Collections.emptyList());
	        return ResponseEntity.status(500).body(response);
	    }
	}


	@Override
	public List<BookingResponse> bookingByPatientIdAndBookingId(String patientId, String bookingId) {
	    List<Booking> bookings = repository.findByPatientIdAndBookingId(patientId, bookingId);

	    if (bookings == null || bookings.isEmpty()) {
	        log.info("No bookings found for patientId={} and bookingId={}", patientId, bookingId);
	        return Collections.emptyList();
	    }

	    List<Booking> reversedBookings = new ArrayList<>();
	    for (int i = bookings.size() - 1; i >= 0; i--) {
	        if ("In-Progress".equalsIgnoreCase(bookings.get(i).getStatus())) {
	            reversedBookings.add(bookings.get(i));
	        }
	    }

	    return toResponses(reversedBookings);
	}


	@Override
	public List<ReportsDTO> getReportsByPatientId(String patientId) {
	    if (patientId == null || patientId.trim().isEmpty()) {
	        log.warn("getReportsByPatientId called with null/empty patientId");
	        return Collections.emptyList();
	    }

	    List<Booking> bookings = repository.findByPatientId(patientId);
	    if (bookings == null || bookings.isEmpty()) {
	        log.info("No bookings found for patientId={}", patientId);
	        return Collections.emptyList();
	    }

	    List<ReportsDTO> responseList = new ArrayList<>();
	    for (Booking booking : bookings) {
	        if (booking.getReports() == null || booking.getReports().isEmpty()) {
	            continue;
	        }
	        for (ReportsList reportList : booking.getReports()) {
	            if (reportList.getReportsList() == null || reportList.getReportsList().isEmpty()) {
	                continue;
	            }
	            for (Reports reportEntity : reportList.getReportsList()) {
	                try {
	                    ReportsDTO dto = mapper.convertValue(reportEntity, ReportsDTO.class);
	                    responseList.add(dto);
	                } catch (Exception e) {
	                    log.warn("Failed to convert reportEntity for bookingId={} : {}", 
	                             booking.getBookingId(), e.getMessage());
	                }
	            }
	        }
	    }

	    return responseList;
	}


	private boolean isValidMobileNumber(String input) {
		if (input == null) {
			return false;
		}
		String regex = "^[6-9]\\d{9}$";
		return input.matches(regex);
	}

	@Override
	public List<BookingResponse> bookingByClinicId(String clinicId) {
		List<Booking> bookings = repository.findByClinicId(clinicId);
		if (bookings == null || bookings.isEmpty()) {
			return null;
		}
		return toResponses(bookings);
	}

	// ✅ CHANGE 2: rewritten to avoid findAll() + nested N+1 query/save loops.
	// The previous version pulled the entire booking collection into heap every
	// hour, then re-queried and re-saved patients inside a triple-nested loop —
	// this was the single biggest recurring memory spike in the service. The
	// counting now happens inside MongoDB via aggregation, and the JVM only
	// ever holds the small (patientId -> count) result set, followed by a
	// single bulk updateMulti per patient.
	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void autoCalculatePatientCompletedAppointments() {
		try {
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(Criteria.where("status").regex("^completed$", "i")),
					Aggregation.group("patientId").count().as("visitCount")
			);

			AggregationResults<Map> results = mongoTemplate.aggregate(agg, "booking", Map.class);

			for (Map<String, Object> r : results.getMappedResults()) {
				Object patientId = r.get("_id");
				Object visitCount = r.get("visitCount");

				if (patientId == null || visitCount == null) {
					continue;
				}

				mongoTemplate.updateMulti(
						Query.query(Criteria.where("patientId").is(patientId)),
						Update.update("visitCount", visitCount),
						Booking.class
				);
			}

			log.info("autoCalculatePatientCompletedAppointments: updated visitCount for {} patients",
					results.getMappedResults().size());

		} catch (Exception e) {
			log.error("autoCalculatePatientCompletedAppointments failed: {}", e.getMessage(), e);
		}
	}

	// ---------------------------to get patientdetails by
	// bookingId,pateintId,mobileNumber---------------------------
	@Override
	public Response getPatientDetailsForConsetForm(String bookingId, String patientId, String mobileNumber) {
		try {
			Optional<Booking> optionalBooking = repository.findByBookingIdAndPatientIdAndMobileNumber(bookingId,
					patientId, mobileNumber);
			if (optionalBooking.isPresent()) {
				Booking booking = optionalBooking.get();
				if (booking.getStatus() != null && (booking.getStatus().equalsIgnoreCase("Confirmed")
						|| booking.getStatus().equalsIgnoreCase("Completed"))) {
					BookingResponse response = mapper.convertValue(booking, BookingResponse.class);
					return Response.builder().success(true).status(200).message("Booking details fetched successfully.")
							.data(response).build();
				} else {
					return Response.builder().success(false).status(404)
							.message("No booking found with the given details.").build();
				}
			} else {
				return Response.builder().success(false).status(404).message("No booking found with the given details.")
						.build();
			}
		} catch (Exception e) {
			log.error("Error in getPatientDetailsForConsetForm for bookingId={}: {}", bookingId, e.getMessage(), e);
			return Response.builder().success(false).status(500).message(e.getMessage()).build();
		}
	}

	public ResponseEntity<?> getInProgressAppointments(String number) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<List<BookingResponse>>();
		try {
			List<Booking> booked = repository.findByMobileNumber(number);
			List<BookingResponse> response = new ArrayList<>();
			if (booked != null && !booked.isEmpty()) {
				for (Booking b : booked) {
					if (b.getStatus() != null && b.getStatus().equalsIgnoreCase("In-Progress")) {
						response.add(toResponse(b));
					}
				}
			}
			res.setStatusCode(200);
			res.setHttpStatus(HttpStatus.OK);
			res.setData(response);
			res.setMessage(response.isEmpty() ? "In-Progress appointments not found" : "In-Progress appointments found");
		} catch (Exception e) {
			log.error("Error in getInProgressAppointments for number={}: {}", number, e.getMessage(), e);
			res.setStatusCode(500);
			res.setMessage(e.getMessage());
		}
		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	@Override
	public ResponseEntity<?> getInProgressAppointmentsByCustomerId(String customerId) {

		try {

			List<Booking> bookings = repository.findByCustomerIdAndStatusIgnoreCase(customerId, "In-Progress");

			if (bookings == null || bookings.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseStructure.buildResponse(null,
						"No in-progress bookings found", HttpStatus.NOT_FOUND, 404));
			}

			List<BookingResponse> bookingResponses = bookings.stream().peek(this::nullifyLargeFields).map(booking -> {
				BookingResponse response = mapper.convertValue(booking, BookingResponse.class);

				String pdf = getPrescriptionpdf(response.getBookingId());

				if (pdf != null) {
					response.setPrescriptionPdf(Collections.singletonList(pdf));
				}

				return response;
			}).toList();

			return ResponseEntity.ok(ResponseStructure.buildResponse(bookingResponses, "In-Progress appointments found",
					HttpStatus.OK, HttpStatus.OK.value()));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseStructure.buildResponse(null,
					"Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, 500));
		}
	}

	@Override
	public ResponseEntity<?> getInProgressAppointmentsByPatientId(String patientId, String clinicId) {

		try {

			List<Booking> bookings = repository.findByPatientIdAndClinicId(patientId, clinicId);

			if (bookings == null || bookings.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseStructure.buildResponse(null,
						"No bookings found for this patient", HttpStatus.NOT_FOUND, 404));
			}

			List<BookingResponse> bookingResponses = bookings.stream()
					.filter(booking -> "In-Progress".equalsIgnoreCase(booking.getStatus()))
					.peek(this::nullifyLargeFields).map(booking -> {

						BookingResponse response = mapper.convertValue(booking, BookingResponse.class);

						String pdf = getPrescriptionpdf(booking.getBookingId());

						if (pdf != null) {
							response.setPrescriptionPdf(Collections.singletonList(pdf));
						}

						return response;
					}).toList();

			if (bookingResponses.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseStructure.buildResponse(null,
						"No In-Progress appointments found for this patient", HttpStatus.NOT_FOUND, 404));
			}

			return ResponseEntity.ok(ResponseStructure.buildResponse(bookingResponses, "In-Progress appointments found",
					HttpStatus.OK, HttpStatus.OK.value()));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseStructure.buildResponse(null,
					"Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, 500));
		}
	}

	/**
	 * ✅ Utility: Parse both yyyy-MM-dd and dd-MM-yyyy formats
	 */
	private LocalDate parseDate(String dateStr) {
		if (dateStr == null)
			return null;
		List<DateTimeFormatter> formatters = Arrays.asList(DateTimeFormatter.ofPattern("yyyy-MM-dd"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		for (DateTimeFormatter fmt : formatters) {
			try {
				return LocalDate.parse(dateStr, fmt);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public List<BookingResponse> inprogressAppointmentsByConsultationExpiration(LocalDate exp, Booking booking,
			DoctorSaveDetailsDTO saveDetails) {
		List<BookingResponse> finalList = new ArrayList<>();
		try {
			LocalDate today = LocalDate.now();
			LocalDate sixthDate = today.plusDays(6);
			DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			if (saveDetails.getFollowUp() != null && saveDetails.getFollowUp().getNextFollowUpDate() != null) {
				try {
					int days = 0;
					if (exp == null) {
						days = Integer.parseInt(booking.getConsultationExpiration().replaceAll("\\D+", ""));
						LocalDate serviceDate = LocalDate.parse(booking.getServiceDate(), isoFormatter);
						exp = serviceDate.plusDays(days);
					}
					LocalDate followDate = LocalDate.parse(saveDetails.getFollowUp().getNextFollowUpDate(),
							DateTimeFormatter.ISO_LOCAL_DATE_TIME);
					if (!followDate.isBefore(today) && !followDate.isAfter(sixthDate) && !followDate.isAfter(exp)) {
						Booking bkng = new Booking(booking);
						bkng.setFollowupDate(followDate.format(isoFormatter));
						bkng.setStatus("In-Progress");
						finalList.add(toResponse(bkng));
					}
				} catch (Exception e) {
					log.warn("Error computing followup date for bookingId={}: {}", booking.getBookingId(), e.getMessage());
				}
			} else {
				// ✅ Consultation expiration fallback
				if (booking.getConsultationExpiration() != null) {
					try {
						int days = 0;
						if (exp == null) {
							days = Integer.parseInt(booking.getConsultationExpiration().replaceAll("\\D+", ""));
							LocalDate serviceDate = LocalDate.parse(booking.getServiceDate(), isoFormatter);
							exp = serviceDate.plusDays(days);
						}
						for (int i = 0; i <= 6; i++) {
							LocalDate date = today.plusDays(i);
							if ((!date.isAfter(sixthDate)) && (date.isBefore(exp) || date.equals(exp))) {
								Booking bkng = new Booking(booking);
								bkng.setFollowupDate(date.format(isoFormatter));
								bkng.setStatus("In-Progress");
								finalList.add(toResponse(bkng));
							}
						}
					} catch (Exception e) {
						log.warn("Error computing expiration fallback for bookingId={}: {}", booking.getBookingId(), e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			log.error("Error in inprogressAppointmentsByConsultationExpiration: {}", e.getMessage(), e);
			return null;
		}
		return finalList;
	}

	public ResponseEntity<?> getDoctorFutureAppointments(String doctorId) {
		ResponseStructure<List<Map<String, Object>>> res = new ResponseStructure<List<Map<String, Object>>>();
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			List<Booking> booked = repository.findByDoctorId(doctorId);
			List<BookingResponse> response = new ArrayList<>();
			if (booked != null && !booked.isEmpty()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate currentDate = LocalDate.now();
				LocalDate plus = currentDate.plusDays(15);

				for (Booking b : booked) {
					if (b.getServiceDate() == null) {
						continue;
					}
					try {
						LocalDate serviceDate = LocalDate.parse(b.getServiceDate(), formatter);
						if (!serviceDate.isBefore(currentDate) && !serviceDate.isAfter(plus)) {
							response.add(toResponse(b));
						}
					} catch (Exception e) {
						log.warn("Error parsing serviceDate for bookingId={}: {}", b.getBookingId(), e.getMessage());
					}
				}

				response.forEach(n -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("bookingId", n.getBookingId());
					map.put("serviceDate", n.getServiceDate());
					map.put("servicetime", n.getServicetime());
					map.put("name", n.getName());
					// ✅ FIX 2: was `!n.getPatientMobileNumber().isEmpty()` with no null check
					map.put("mobileNumber", resolveMobileNumber(n));
					map.put("doctorId", n.getDoctorId());
					map.put("doctorName", n.getDoctorName());
					map.put("paymentType", n.getPaymentType());
					map.put("visitType", n.getVisitType());
					map.put("status", n.getStatus());
					map.put("followupStatus", n.getFollowupStatus());
					map.put("patientId", n.getPatientId());
					map.put("clinicId", n.getClinicId());
					map.put("customerId", n.getCustomerId());
					map.put("branchId", n.getBranchId());
					map.put("age", n.getAge());
					map.put("gender", n.getGender());
					map.put("branchName", n.getBranchname());
					map.put("problem", n.getProblem());
					list.add(map);
				});
			}

			res.setStatusCode(200);
			res.setHttpStatus(HttpStatus.OK);
			res.setData(list);
			res.setMessage(list.isEmpty() ? "appointments not found" : "appointments found");
		} catch (Exception e) {
			log.error("Error in getDoctorFutureAppointments for doctorId={}: {}", doctorId, e.getMessage(), e);
			res.setStatusCode(500);
			res.setMessage(e.getMessage());
		}
		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	@Override
	public List<BookingResponse> bookingByBranchId(String branchId) {
		List<Booking> bookings = repository.findByBranchId(branchId);
		if (bookings == null || bookings.isEmpty()) {
			return null;
		}
		List<Booking> reversedBookings = new ArrayList<>();
		for (int i = bookings.size() - 1; i >= 0; i--) {
			reversedBookings.add(bookings.get(i));
		}
		return toResponses(reversedBookings);
	}

    @Override
    public List<Map<String, Object>> getBookedServicesByClinicIdWithBranchId(String clinicId, String branchId) {

        try {
            List<Booking> bookings = repository.findByClinicIdAndBranchId(clinicId, branchId);
            //System.out.println("Bookings found: " + bookings.size());

            List<BookingResponse> responses = toResponses(bookings);
           // System.out.println("Responses mapped: " + responses.size());

            List<Map<String, Object>> list = new ArrayList<>();

            for (BookingResponse n : responses) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("bookingId", n.getBookingId());
                map.put("serviceDate", n.getServiceDate());
                map.put("servicetime", n.getServicetime());
                map.put("name", n.getName());
                map.put("mobileNumber", resolveMobileNumber(n));
                map.put("doctorId", n.getDoctorId());
                map.put("doctorName", n.getDoctorName());
                map.put("paymentType", n.getPaymentType());
                map.put("visitType", n.getVisitType());
                map.put("status", n.getStatus());
                map.put("followupStatus", n.getFollowupStatus());
                map.put("patientId", n.getPatientId());
                map.put("clinicId", n.getClinicId());
                map.put("customerId", n.getCustomerId());
                map.put("branchId", n.getBranchId());
                map.put("age", n.getAge());
                map.put("gender", n.getGender());
                map.put("branchName", n.getBranchname()); // check getter spelling!
                map.put("problem", n.getProblem());
                map.put("session", n.getSession());
                map.put("referredDoctorId", n.getReferredDoctorId());
                map.put("referredByType", n.getReferredByType());
                map.put("referredByName", n.getReferredByName());
                map.put("doctorRefCode", n.getDoctorRefCode());
                map.put("consultationFee", n.getConsultationFee());
                map.put("totalFee", n.getTotalFee());

                list.add(map);
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList(); // safer than returning null
        }
    }


    @Override
	public ResponseEntity<?> getBookedServicesByClinicIdWithBranchIdAnddoctorIdAndStatus(String clinicId,
			String branchId, String doctorId, String status) {
		try {
			List<Map<String, Object>> list = new ArrayList<>();
			List<BookingResponse> reversedBookings = new ArrayList<>();
			LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
			if (!branchId.equalsIgnoreCase("all")) {
				if (status.equalsIgnoreCase("pending")) {
					String requiredStatus = "confirmed";
					List<Booking> bookings = repository.findByClinicIdAndBranchIdAndDoctorIdAndStatusIgnoreCase(
							clinicId, branchId, doctorId, requiredStatus);
					reversedBookings = toResponses(bookings);
					reversedBookings = reversedBookings.stream().filter(b -> {
						if (b.getServiceDate() == null) return false;
						LocalDate bookingDate = LocalDate.parse(b.getServiceDate());
						return bookingDate.isBefore(currentDate);
					}).toList();
				} else if (status.equalsIgnoreCase("confirmed")) {
					List<Booking> bookings = repository.findByClinicIdAndBranchIdAndDoctorIdAndStatusIgnoreCase(
							clinicId, branchId, doctorId, status);

					reversedBookings = toResponses(bookings);

					reversedBookings = reversedBookings.stream().filter(b -> {
						if (b.getServiceDate() == null) return false;
						LocalDate bookingDate = LocalDate.parse(b.getServiceDate());
						return bookingDate.isAfter(currentDate);
					}).toList();
				} else {
					List<Booking> bookings = repository.findByClinicIdAndBranchIdAndDoctorIdAndStatusIgnoreCase(
							clinicId, branchId, doctorId, status);
					if (!bookings.isEmpty()) {
						reversedBookings = toResponses(bookings);
					} else {
						List<Booking> bkings = repository
								.findByClinicIdAndBranchIdAndDoctorIdAndFollowupStatusIgnoreCase(clinicId, branchId,
										doctorId, status);
						reversedBookings = toResponses(bkings);
					}
				}
				if (reversedBookings != null && !reversedBookings.isEmpty()) {

					reversedBookings.forEach(n -> {

						Map<String, Object> map = new LinkedHashMap<>();

						map.put("bookingId", n.getBookingId());
						map.put("serviceDate", n.getServiceDate());
						map.put("servicetime", n.getServicetime());
						map.put("name", n.getName());
						// ✅ FIX 2: was `!n.getPatientMobileNumber().isEmpty()` with no null check
						map.put("mobileNumber", resolveMobileNumber(n));

						map.put("doctorId", n.getDoctorId());
						map.put("doctorName", n.getDoctorName());
						map.put("paymentType", n.getPaymentType());
						map.put("visitType", n.getVisitType());
						map.put("status", n.getStatus());
						map.put("followupStatus", n.getFollowupStatus());
						map.put("patientId", n.getPatientId());
						map.put("clinicId", n.getClinicId());
						map.put("customerId", n.getCustomerId());
						map.put("branchId", n.getBranchId());
						map.put("age", n.getAge());
						map.put("gender", n.getGender());
						map.put("branchName", n.getBranchname());
						map.put("problem", n.getProblem());
						map.put("session", n.getSession());

						list.add(map);
					});
				}
			} else {
				if (status.equalsIgnoreCase("pending")) {
					String requiredStatus = "confirmed";
					List<Booking> bookings = repository.findByClinicIdAndDoctorIdAndStatusIgnoreCase(clinicId, doctorId,
							requiredStatus);
					reversedBookings = toResponses(bookings);
					reversedBookings = reversedBookings.stream().filter(b -> {
						if (b.getServiceDate() == null) return false;
						LocalDate bookingDate = LocalDate.parse(b.getServiceDate());
						return bookingDate.isBefore(currentDate);
					}).toList();
				} else if (status.equalsIgnoreCase("confirmed")) {
					List<Booking> bookings = repository.findByClinicIdAndDoctorIdAndStatusIgnoreCase(clinicId, doctorId,
							status);

					reversedBookings = toResponses(bookings);

					reversedBookings = reversedBookings.stream().filter(b -> {
						if (b.getServiceDate() == null) return false;
						LocalDate bookingDate = LocalDate.parse(b.getServiceDate());
						return bookingDate.isAfter(currentDate);
					}).toList();
				} else {
					List<Booking> bookings = repository.findByClinicIdAndDoctorIdAndStatusIgnoreCase(clinicId, doctorId,
							status);
					if (!bookings.isEmpty()) {
						reversedBookings = toResponses(bookings);
					} else {
						List<Booking> bkings = repository
								.findByClinicIdAndBranchIdAndDoctorIdAndFollowupStatusIgnoreCase(clinicId, branchId,
										doctorId, status);
						reversedBookings = toResponses(bkings);
					}
				}
				if (reversedBookings != null && !reversedBookings.isEmpty()) {

					reversedBookings.forEach(n -> {

						Map<String, Object> map = new LinkedHashMap<>();

						map.put("bookingId", n.getBookingId());
						map.put("serviceDate", n.getServiceDate());
						map.put("servicetime", n.getServicetime());
						map.put("name", n.getName());
						// ✅ FIX 2: was `!n.getPatientMobileNumber().isEmpty()` with no null check
						map.put("mobileNumber", resolveMobileNumber(n));

						map.put("doctorId", n.getDoctorId());
						map.put("doctorName", n.getDoctorName());
						map.put("paymentType", n.getPaymentType());
						map.put("visitType", n.getVisitType());
						map.put("status", n.getStatus());
						map.put("followupStatus", n.getFollowupStatus());
						map.put("patientId", n.getPatientId());
						map.put("clinicId", n.getClinicId());
						map.put("customerId", n.getCustomerId());
						map.put("branchId", n.getBranchId());
						map.put("age", n.getAge());
						map.put("gender", n.getGender());
						map.put("branchName", n.getBranchname());
						map.put("problem", n.getProblem());
						map.put("session", n.getSession());

						list.add(map);
					});
				}
			}
			if (!list.isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new Response(true, list, null, "appointments are found", 200, null, null));
			} else {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new Response(true, null, null, "appointments are not found", 200, null, null));
			}
		} catch (Exception e) {
			log.error("Error in getBookedServicesByClinicIdWithBranchIdAnddoctorIdAndStatus: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(false, null, null, e.getMessage(), 500, null, null));
		}
	}

	@Override
	public ResponseEntity<?> retrieveOneWeekAppointments(String clinicId, String branchId) {

		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<>();
		List<BookingResponse> finalList = new ArrayList<>();

		try {

			List<Booking> bookings = repository.findByClinicIdAndBranchId(clinicId, branchId);

			LocalDate today = LocalDate.now();
			LocalDate weekEndDate = today.plusDays(6);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			for (Booking booking : bookings) {

				String status = booking.getStatus();

				if (!"Confirmed".equalsIgnoreCase(status) && !"In-Progress".equalsIgnoreCase(status)) {
					continue;
				}

				if (booking.getServiceDate() == null) {
					continue;
				}

				try {
					LocalDate serviceDate = LocalDate.parse(booking.getServiceDate(), formatter);

					if (!serviceDate.isBefore(today) && !serviceDate.isAfter(weekEndDate)) {
						finalList.add(toResponse(booking));
					}
				} catch (Exception e) {
					log.warn("Error parsing serviceDate for bookingId={}: {}", booking.getBookingId(), e.getMessage());
				}
			}

			res.setStatusCode(200);
			res.setHttpStatus(HttpStatus.OK);
			res.setMessage("Weekly appointments retrieved successfully");
			res.setData(finalList);

		} catch (Exception e) {

			log.error("Error in retrieveOneWeekAppointments: {}", e.getMessage(), e);
			res.setStatusCode(500);
			res.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			res.setMessage("Error: " + e.getMessage());
			res.setData(Collections.emptyList());
		}

		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	public ResponseEntity<?> retrieveAppointments(String cinicId, String branchId, String date) {
		ResponseStructure<List<BookingResponse>> res = new ResponseStructure<List<BookingResponse>>();
		try {
			List<Booking> bookings = repository.findByClinicIdAndBranchIdAndServiceDateOrderByServicetimeAsc(cinicId,
					branchId, date);
			bookings = bookings.stream()
					.filter(n -> n.getStatus() != null && n.getStatus().equalsIgnoreCase("In-Progress"))
					.toList();
			List<BookingResponse> todayBookingsDto = toResponses(bookings);
			if (todayBookingsDto != null && !todayBookingsDto.isEmpty()) {
				res.setStatusCode(200);
				res.setHttpStatus(HttpStatus.OK);
				res.setData(todayBookingsDto);
				res.setMessage("appointments found");
			} else {
				res.setStatusCode(404);
				res.setHttpStatus(HttpStatus.NOT_FOUND);
				res.setMessage("appointments Not found with date");
			}
		} catch (Exception e) {
			log.error("Error in retrieveAppointments for clinicId={}, branchId={}, date={}: {}", cinicId, branchId, date, e.getMessage(), e);
			res.setStatusCode(500);
			res.setMessage(e.getMessage());
		}
		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	public ResponseEntity<ResponseStructure<BookingResponse>> updateAppointmentBasedOnBookingId(BookingResponse dto) {

		Booking updated = null;

		try {

			Booking entity = repository.findByBookingId(dto.getBookingId())
					.orElseThrow(() -> new RuntimeException("Invalid Booking Id"));

			// -------- BASIC --------

			if (dto.getBookingFor() != null && !dto.getBookingFor().isEmpty())
				entity.setBookingFor(dto.getBookingFor());

			if (dto.getName() != null && !dto.getName().isEmpty())
				entity.setName(dto.getName());

			if (dto.getReports() != null && !dto.getReports().isEmpty()) {
				entity.setReports(mapper.convertValue(dto.getReports(), new TypeReference<List<ReportsList>>() {
				}));
			}

			if(dto.getPaymentType() != null && dto.getPaymentType().equalsIgnoreCase("not paid")) {
				entity.setStatus("pending");
			}
			
			if (dto.getPatientMobileNumber() != null && !dto.getPatientMobileNumber().isEmpty())
				entity.setPatientMobileNumber(dto.getPatientMobileNumber());

			if (dto.getPatientId() != null && !dto.getPatientId().isEmpty())
				entity.setPatientId(dto.getPatientId());

			if (dto.getVisitType() != null && !dto.getVisitType().isEmpty())
				entity.setVisitType(dto.getVisitType());

			if (dto.getPatientAddress() != null && !dto.getPatientAddress().isEmpty())
				entity.setPatientAddress(dto.getPatientAddress());

			if (dto.getAge() != null && !dto.getAge().isEmpty())
				entity.setAge(dto.getAge());

			if (dto.getGender() != null && !dto.getGender().isEmpty())
				entity.setGender(dto.getGender());

			if (dto.getMobileNumber() != null && !dto.getMobileNumber().isEmpty())
				entity.setMobileNumber(dto.getMobileNumber());

			if (dto.getCustomerId() != null && !dto.getCustomerId().isEmpty())
				entity.setCustomerId(dto.getCustomerId());

		
			// -------- FOLLOWUPS --------

			if (dto.getFreeFollowUpsLeft() != null)
				entity.setFreeFollowUpsLeft(dto.getFreeFollowUpsLeft());

			if (dto.getFreeFollowUps() != null)
				entity.setFreeFollowUps(dto.getFreeFollowUps());

			if (dto.getFollowupDate() != null && !dto.getFollowupDate().isEmpty())
				entity.setFollowupDate(dto.getFollowupDate());

			if (dto.getFollowupStatus() != null) {
				if(dto.getFollowupStatus().equalsIgnoreCase("Completed")) {
					entity.setStatus("Completed");
				}
				entity.setFollowupStatus(dto.getFollowupStatus());}

			// -------- PROBLEM --------

			if (dto.getProblem() != null && !dto.getProblem().isEmpty())
				entity.setProblem(dto.getProblem());

			if (dto.getSymptomsDuration() != null && !dto.getSymptomsDuration().isEmpty())
				entity.setSymptomsDuration(dto.getSymptomsDuration());

			// -------- CLINIC --------

			if (dto.getClinicId() != null && !dto.getClinicId().isEmpty())
				entity.setClinicId(dto.getClinicId());

			if (dto.getClinicName() != null && !dto.getClinicName().isEmpty())
				entity.setClinicName(dto.getClinicName());

			
			if (dto.getBranchId() != null && !dto.getBranchId().isEmpty())
				entity.setBranchId(dto.getBranchId());

			if (dto.getBranchname() != null && !dto.getBranchname().isEmpty())
				entity.setBranchname(dto.getBranchname());

			// -------- DOCTOR --------

			if (dto.getDoctorId() != null && !dto.getDoctorId().isEmpty())
				entity.setDoctorId(dto.getDoctorId());

			if (dto.getDoctorName() != null && !dto.getDoctorName().isEmpty())
				entity.setDoctorName(dto.getDoctorName());

		
			// -------- SERVICE --------

			if (dto.getServiceDate() != null && !dto.getServiceDate().isEmpty())
				entity.setServiceDate(dto.getServiceDate());

			if (dto.getServicetime() != null && !dto.getServicetime().isEmpty())
				entity.setServicetime(dto.getServicetime());

			if (dto.getConsultationType() != null && !dto.getConsultationType().isEmpty())
				entity.setConsultationType(dto.getConsultationType());

			// -------- CONSULTATION FEE --------

			if (dto.getConsultationFee() != null) {

				List<ConsultationFees> list = entity.getListOfConsultationFee();
				if (list == null)
					list = new ArrayList<>();

				ConsultationFees fee = new ConsultationFees();
				fee.setConsulationFee(dto.getConsultationFee());
				fee.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

				list.add(fee);

				entity.setConsultationFee(dto.getConsultationFee());
				entity.setListOfConsultationFee(list);
			}

			if (dto.getListOfConsultationFee() != null && !dto.getListOfConsultationFee().isEmpty()) {

				List<ConsultationFees> list = entity.getListOfConsultationFee();
				if (list == null)
					list = new ArrayList<>();

				for (ConsultationFeesDTO c : dto.getListOfConsultationFee()) {
					ConsultationFees fee = mapper.convertValue(c, ConsultationFees.class);
					list.add(fee);
				}

				entity.setListOfConsultationFee(list);
			}

			if (dto.getConsultationExpiration() != null && !dto.getConsultationExpiration().isEmpty())
				entity.setConsultationExpiration(dto.getConsultationExpiration());

			// -------- STATUS --------

			if (dto.getStatus() != null) {

				entity.setStatus(dto.getStatus());

				List<Status> statusList = entity.getCurrentStatus();
				if (statusList == null) {
					statusList = new ArrayList<>();}

				Status s = new Status();
				s.setStatus(dto.getStatus());
				s.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

				statusList.add(s);
				entity.setCurrentStatus(statusList);}

                if (dto.getStatus() != null && (
                        dto.getStatus().equalsIgnoreCase("cancelled") ||
                                dto.getStatus().equalsIgnoreCase("rescheduled") ||
                                dto.getStatus().equalsIgnoreCase("cancel"))) {
                    try {
                        clinnicfeign.makingFalseDoctorSlot(
                                dto.getDoctorId(),
                                dto.getBranchId(),
                                dto.getServiceDate(),
                                dto.getServicetime()
                        );
                    } catch (Exception e) {
                        // ✅ Don't fail the whole status update just because the downstream slot-release call had trouble
                        log.warn("Failed to release doctor slot for bookingId={}: {}", dto.getBookingId(), e.getMessage(), e);
                    }
                }


//                if (dto.getStatus() != null && (
//                        dto.getStatus().equalsIgnoreCase("Not Started") ||
//                                dto.getStatus().equalsIgnoreCase("In-progress")
//                              )) {
//                    try {
//                        clinnicfeign.makingFalseDoctorSlot(
//                                dto.getDoctorId(),
//                                dto.getBranchId(),
//                                dto.getServiceDate(),
//                                dto.getServicetime()
//                        );
//                    } catch (Exception e) {
//                        // ✅ Don't fail the whole status update just because the downstream slot-release call had trouble
//                        log.warn("Failed to release doctor slot for bookingId={}: {}", dto.getBookingId(), e.getMessage(), e);
//                    }
                //}


			if (dto.getCurrentStatus() != null && !dto.getCurrentStatus().isEmpty()) {
				entity.setCurrentStatus(mapper.convertValue(dto.getCurrentStatus(), new TypeReference<List<Status>>() {
				}));
			}

			if (dto.getReasonForCancel() != null && !dto.getReasonForCancel().isEmpty())
				entity.setReasonForCancel(dto.getReasonForCancel());

			// -------- FILES --------

			if (dto.getAttachments() != null && !dto.getAttachments().isEmpty())
				entity.setAttachments(dto.getAttachments());

			if (dto.getConsentFormPdf() != null && !dto.getConsentFormPdf().isEmpty())
				entity.setConsentFormPdf(dto.getConsentFormPdf());

			if (dto.getPrescriptionPdf() != null && !dto.getPrescriptionPdf().isEmpty())
				entity.setPrescriptionPdf(dto.getPrescriptionPdf());

			// -------- PAYMENT --------

			if (dto.getPaymentType() != null && !dto.getPaymentType().isEmpty())
				entity.setPaymentType(dto.getPaymentType());

			if (dto.getPaymentStatus() != null && !dto.getPaymentStatus().isEmpty())
				entity.setPaymentStatus(dto.getPaymentStatus());

			if (dto.getTotalFee() > 0)
				entity.setTotalFee(dto.getTotalFee());


            if(dto.getPacakgeId() != null){
                entity.setPacakgeId(dto.getPacakgeId());
            }

            if(dto.getPackageName() != null){
                entity.setPackageName(dto.getPackageName());
            }

            if(dto.getPackageType() != null){
                entity.setPackageType(dto.getPackageType());
            }


            if (dto.getScheduleId() != null && !dto.getScheduleId().isEmpty()) {
                entity.setScheduleId(dto.getScheduleId());
            }

            if (dto.getSittingsId() != null && !dto.getSittingsId().isEmpty()) {
                entity.setSittingsId(dto.getSittingsId());
            }

            if (dto.getSittingNumber() != null && dto.getSittingNumber() > 0) {
                entity.setSittingNumber(dto.getSittingNumber());
            }


            if (dto.getDoctorRefCode() != null && !dto.getDoctorRefCode().isEmpty())
				entity.setDoctorRefCode(dto.getDoctorRefCode());

			// -------- THERAPY --------

			if (dto.getTheraphyAnswers() != null) {
				entity.setTheraphyAnswers(mapper.convertValue(dto.getTheraphyAnswers(),
						new TypeReference<Map<String, List<TheraphyAnswersEntity>>>() {
						}));
			}

			if (dto.getDueAmount() >= 0)
				entity.setDueAmount(dto.getDueAmount());

			// -------- REFERRAL --------

			if (dto.getReferredByType() != null && !dto.getReferredByType().isEmpty())
				entity.setReferredByType(dto.getReferredByType());

			if (dto.getReferredByName() != null && !dto.getReferredByName().isEmpty())
				entity.setReferredByName(dto.getReferredByName());

			// -------- MEDICAL --------

			if (dto.getPreviousInjuries() != null && !dto.getPreviousInjuries().isEmpty())
				entity.setPreviousInjuries(dto.getPreviousInjuries());

			if (dto.getCurrentMedications() != null && !dto.getCurrentMedications().isEmpty())
				entity.setCurrentMedications(dto.getCurrentMedications());

			if (dto.getAllergies() != null && !dto.getAllergies().isEmpty())
				entity.setAllergies(dto.getAllergies());

			if (dto.getOccupation() != null && !dto.getOccupation().isEmpty())
				entity.setOccupation(dto.getOccupation());

			// -------- INSURANCE --------

			if (dto.getInsuranceProvider() != null && !dto.getInsuranceProvider().isEmpty())
				entity.setInsuranceProvider(dto.getInsuranceProvider());

			if (dto.getPolicyNumber() != null && !dto.getPolicyNumber().isEmpty())
				entity.setPolicyNumber(dto.getPolicyNumber());

			// -------- ACTIVITY --------

			if (dto.getActivityLevels() != null && !dto.getActivityLevels().isEmpty())
				entity.setActivityLevels(dto.getActivityLevels());

			// -------- FOC --------

			if (dto.getFoc() != null)
				entity.setFoc(dto.getFoc());

			// -------- FOLLOWUP LOGIC --------

			try {
				int days = 0;

				if (entity.getConsultationExpiration() != null)
					days = Integer.parseInt(entity.getConsultationExpiration().replaceAll("[^0-9]", ""));

				LocalDate serviceDate = LocalDate.parse(entity.getServiceDate());
				LocalDate expiryDate = serviceDate.plusDays(days);
				LocalDate today = LocalDate.now();

				if (!today.isAfter(expiryDate) && entity.getFreeFollowUps() != null && entity.getFreeFollowUps() == 0) {

					entity.setIsFollowupStatus(true);

				} else if (today.isAfter(expiryDate)) {

					entity.setIsFollowupStatus(true);

				} else {
					entity.setIsFollowupStatus(false);
				}

			} catch (Exception e) {
				entity.setIsFollowupStatus(false);
			}

			updated = repository.save(entity);

			return new ResponseEntity<>(ResponseStructure.buildResponse(toResponse(updated), "Updated Successfully",
					HttpStatus.OK, HttpStatus.OK.value()), HttpStatus.OK);

		} catch (Exception e) {

			log.error("Error in updateAppointmentBasedOnBookingId for bookingId={}: {}", dto.getBookingId(), e.getMessage(), e);
			return new ResponseEntity<>(ResponseStructure.buildResponse(null, e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<?> getRelationsByCustomerId(String customerId) {
		ResponseStructure<Map<String, List<RelationInfoDTO>>> res = new ResponseStructure<>();
		try {
			List<Booking> bookings = repository.findByCustomerId(customerId);

			Map<String, List<RelationInfoDTO>> data = bookings.stream().collect(Collectors.groupingBy(
					Booking::getRelation, LinkedHashMap::new, Collectors.collectingAndThen(Collectors.mapping(n -> {
						RelationInfoDTO dto = new RelationInfoDTO();
						dto.setAddress(n.getPatientAddress());
						dto.setAge(n.getAge());
						dto.setFullname(n.getName());
						dto.setMobileNumber(n.getMobileNumber());
						dto.setRelation(n.getRelation());
						dto.setGender(n.getGender());
						dto.setCustomerId(n.getCustomerId());
						dto.setPatientId(n.getPatientId());
						return dto;
					}, Collectors.toList()), list -> list.stream().distinct().collect(Collectors.toList())
					)));
			res.setStatusCode(200);
			res.setHttpStatus(HttpStatus.OK);
			res.setData(data);
			res.setMessage("Relations found successfully");
		} catch (Exception e) {
			log.error("Error in getRelationsByCustomerId for customerId={}: {}", customerId, e.getMessage(), e);
			res.setStatusCode(500);
			res.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			res.setMessage("Error: " + e.getMessage());
		}

		return ResponseEntity.status(res.getStatusCode()).body(res);
	}

	@Override
	public BookingResponse checkBookingByDateAndTime(String date, String time, String doctorId) {
		Booking booking = repository.findByServiceDateAndServicetimeAndDoctorId(date, time, doctorId);
		if (booking != null) {
			return toResponse(booking);
		} else {
			return null;
		}

	}

	@Override
	public ResponseEntity<Response> getPatientAndPriceInfo(String clinicId, String branchId, Integer number,
			String startDate, String endDate) {

		try {

			List<Booking> bookings = repository.findByClinicIdAndBranchId(clinicId, branchId);

			if (bookings == null || bookings.isEmpty()) {
				return ResponseEntity.ok(Response.builder().success(true).message("No data found")
						.data(new PatientAndPriceInfo()).status(HttpStatus.OK.value()).build());
			}

			// 🔥 Step 1: Decide Date Range
			LocalDate start;
			LocalDate end;

			if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
				start = LocalDate.parse(startDate);
				end = LocalDate.parse(endDate);
			} else {
				LocalDate today = LocalDate.now();

				if (number == null) {
					return ResponseEntity.badRequest().body(Response.builder().success(false)
							.message("Invalid number value").status(HttpStatus.BAD_REQUEST.value()).build());
				}

				if (number == 1) {
					start = today;
					end = today;
				} else if (number == 2) {
					start = today.minusDays(6);
					end = today;
				} else if (number == 3) {
					start = today.withDayOfMonth(1);
					end = today;
				} else {
					return ResponseEntity.badRequest().body(Response.builder().success(false)
							.message("Invalid number value").status(HttpStatus.BAD_REQUEST.value()).build());
				}
			}

			// 🔥 Step 2: Filter + Map
			List<PatientInfo> patientList = new ArrayList<>();

			double totalConsultation = 0;
			double totalTherapy = 0;
			double totalDue = 0;

			for (Booking booking : bookings) {

				if (booking.getServiceDate() == null)
					continue;

				LocalDate bookingDate;
				try {
					bookingDate = LocalDate.parse(booking.getServiceDate());
				} catch (Exception e) {
					log.warn("Skipping booking with unparsable serviceDate, bookingId={}: {}", booking.getBookingId(), e.getMessage());
					continue;
				}

				if ((bookingDate.isEqual(start) || bookingDate.isAfter(start))
						&& (bookingDate.isEqual(end) || bookingDate.isBefore(end))) {

					PatientInfo info = new PatientInfo();

					info.setClinicId(booking.getClinicId());
					info.setBranchId(booking.getBranchId());
					info.setPatientName(booking.getName());
					info.setDate(booking.getServiceDate());
					info.setDoctorId(booking.getDoctorId());
					info.setConsultationFee(String.valueOf(booking.getConsultationFee()));
					info.setTheraphyFee(String.valueOf(booking.getTotalFee()));
					info.setFinalAmount(booking.getTotalFee());
					info.setDueAmount(booking.getDueAmount());
					info.setConsultationType(booking.getConsultationType());

					patientList.add(info);

					// ✅ FIX 3: guard against null/empty listOfConsultationFee (e.g. FOC
					// bookings) -- previously an unguarded .get(0) here threw
					// IndexOutOfBoundsException/NPE and produced a 500.
					List<ConsultationFees> fees = booking.getListOfConsultationFee();
					if (fees != null && !fees.isEmpty() && fees.get(0) != null) {
						totalConsultation += fees.get(0).getConsulationFee();
					}
					totalTherapy += booking.getTotalFee();
					totalDue += booking.getDueAmount();
				}
			}

			// 🔥 Step 3: Final Calculation
			double grandTotal = totalConsultation + totalTherapy + totalDue;
			double afterExpenses = 0.0;
			try {
				if (number != null && number == 1) {
					Double value = clinicAdminFeign.getTodayExpenses(clinicId, branchId);
					afterExpenses = grandTotal - (value != null ? value : 0);
				} else if (number != null && number == 2) {
					Double value = clinicAdminFeign.getWeeklyExpenses(clinicId, branchId);
					afterExpenses = grandTotal - (value != null ? value : 0);
				} else if (number != null && number == 3) {
					Double value = clinicAdminFeign.getMonthlyExpenses(clinicId, branchId);
					afterExpenses = grandTotal - (value != null ? value : 0);
				} else if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
					Double value = clinicAdminFeign.customFilter(startDate, endDate);
					afterExpenses = grandTotal - (value != null ? value : 0);
				} else {
					afterExpenses = grandTotal;
				}
			} catch (Exception e) {
				log.warn("Failed to fetch expenses for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage());
				afterExpenses = grandTotal;
			}

			PatientAndPriceInfo responseDto = new PatientAndPriceInfo();
			responseDto.setList(patientList);
			responseDto.setTotalConsultationFee(String.valueOf(totalConsultation));
			responseDto.setTotalTheraphyFee(String.valueOf(totalTherapy));
			responseDto.setTotalDueAmount(String.valueOf(totalDue));
			responseDto.setGrandTotalAmount(String.valueOf(grandTotal));
			responseDto.setPriceAfterExpenses(afterExpenses);

			return ResponseEntity
					.ok(Response.builder().success(true).data(responseDto).status(HttpStatus.OK.value()).build());

		} catch (Exception e) {

			log.error("Error in getPatientAndPriceInfo for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.builder().success(false)
					.message(e.getMessage()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build());
		}
	}

	@Override
	public List<Map<String, Object>> getTodayBookings(String cId, String bId) {
		try {
			List<Map<String, Object>> list = new ArrayList<>();
			String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			List<Booking> b = repository.findByClinicIdAndBranchIdAndServiceDate(cId, bId, today);
			if (!b.isEmpty()) {
				List<BookingResponse> dto = toResponses(b);
				dto.forEach(n -> {
					Map<String, Object> map = new LinkedHashMap<>();
					map.put("bookingId", n.getBookingId());
					map.put("serviceDate", n.getServiceDate());
					map.put("servicetime", n.getServicetime());
					map.put("name", n.getName());
					map.put("mobileNumber", resolveMobileNumber(n));
					map.put("doctorId", n.getDoctorId());
					map.put("doctorName", n.getDoctorName());
					map.put("paymentType", n.getPaymentType());
					map.put("visitType", n.getVisitType());
					map.put("status", n.getStatus());
					map.put("followupStatus", n.getFollowupStatus());
					map.put("patientId", n.getPatientId());
					map.put("clinicId", n.getClinicId());
					map.put("customerId", n.getCustomerId());
					map.put("branchId", n.getBranchId());
					map.put("problem", n.getProblem());
					list.add(map);
				});
				return list;
			} else {
				return Collections.emptyList();
			}
		} catch (Exception e) {
			log.error("Error in getTodayBookings for clinicId={}, branchId={}: {}", cId, bId, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public ResponseEntity<Response> getCustomDateBookings(String clinicId, String branchId, String date) {
		try {
			LocalDate localDate = LocalDate.parse(date, FORMATTER);
			String today = localDate.toString();
			List<List<Map<String, Object>>> responseList = new ArrayList<>();
			List<Map<String, Object>> LIST_TO_STORE_FOLLOWUP_BOOKINGS = new ArrayList<>();

			// Fetch bookings for clinic & branch on given date
			List<Booking> data = repository.findByClinicIdAndBranchIdAndServiceDate(clinicId, branchId, today);
			List<BookingResponse> responses = toResponses(data);
			///System.out.println(responses);
			for (BookingResponse n : responses) {

				TreatmentScheduleDTO sittings = new TreatmentScheduleDTO();
				try {
					sittings = clinicAdminFeign.getSittings(
							n.getClinicId(), n.getBranchId(), n.getBookingId(), localDate.toString(), localDate.toString()
					);
				} catch (Exception e) {
					///System.out.println(e.getMessage());
					log.warn("Error fetching sittings/payments for bookingId={}: {}", n.getBookingId(), e.getMessage());
				}
				///System.out.println(sittings);

				Map<String, Double> info = new HashMap<>();
				try {
					info = clinicAdminFeign.getAllPayments(n.getBookingId());
				} catch (Exception e) {///System.out.println(e.getMessage());
				}
				///System.out.println(info);
				responseList.add(buildBookingData(n, sittings, info));}

			    List<String> ids = new LinkedList<>();
			    responseList.stream().flatMap(List::stream).filter(Objects::nonNull).forEach(i->ids.add((String)i.get("bookingId")));
			    List<TreatmentScheduleDTO> dtos = new LinkedList<>();
			try{
			dtos = clinicAdminFeign.getFilteredSittings(
					clinicId,branchId, localDate.toString(), localDate.toString(),ids);
			}catch (Exception e){}

			if(dtos!=null && !dtos.isEmpty()){
				LIST_TO_STORE_FOLLOWUP_BOOKINGS = buildBookingDataBasedOnSesions(dtos);}
			if(!LIST_TO_STORE_FOLLOWUP_BOOKINGS.isEmpty()){
			responseList.add(LIST_TO_STORE_FOLLOWUP_BOOKINGS);}
			// Flatten nested list
			List<Map<String, Object>> flatList = responseList.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());

           // Summary counts
			Map<String, Object> summary = new HashMap<>();
			if (!flatList.isEmpty()) {
				long totalCount = flatList.size();

				long pendingCount = flatList.stream()
						.filter(b -> "PENDING".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

				long confirmedCount = flatList.stream()
						.filter(b -> "CONFIRMED".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

				long inProgressCount = flatList.stream()
						.filter(b -> "IN-PROGRESS".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

			long other = totalCount - pendingCount - confirmedCount - inProgressCount;
			 summary = new HashMap<>();
			summary.put("totalAppointments", totalCount);
			summary.put("pending", pendingCount);
			summary.put("confirmed", confirmedCount);
			summary.put("inProgress", inProgressCount);
			summary.put("Other", other);}

			if (responseList.isEmpty()) {
				return ResponseEntity.ok(new Response(true, Collections.emptyList(), summary, "No sittings found", 200, null, null));
			}
			return ResponseEntity.ok(new Response(true, flatList, summary, "Bookings with sittings fetched", 200, null, null));

		} catch (Exception e) {
			log.error("Error fetching bookings for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(false, null, null, "Error fetching bookings: " + e.getMessage(), 500, null, null));
		}
	}


	@Override
	public ResponseEntity<Response> getFilteredBookingsByStatus(
	        String clinicId,
	        String branchId) {

	    try {

	        List<Booking> bookings =
	                repository.findByClinicIdAndBranchId(
	                        clinicId,
	                        branchId);

	        List<BookingResponse> bookingResponses =
	                bookings != null && !bookings.isEmpty()
	                        ? toResponses(bookings)
	                        : Collections.emptyList();

	        List<Map<String, Object>> filteredBookings =
	                new ArrayList<>();

	        long inProgressCount = 0;
	        long completedCount = 0;
	        long dueForInvestigationCount = 0;
	        long investigationDoneCount = 0;

	        for (BookingResponse booking : bookingResponses) {

	            String followupStatus =
	                    booking.getStatus();

	            boolean includeBooking = false;

	            if ("IN-PROGRESS".equalsIgnoreCase(followupStatus)) {

	                inProgressCount++;
	                includeBooking = true;

	            } else if ("COMPLETED".equalsIgnoreCase(followupStatus)) {

	                completedCount++;
	                includeBooking = true;

	            } else if ("DUE FOR INVESTIGATION".equalsIgnoreCase(followupStatus)) {

	                dueForInvestigationCount++;
	                includeBooking = true;

	            } else if ("INVESTIGATION DONE".equalsIgnoreCase(followupStatus)) {

	                investigationDoneCount++;
	                includeBooking = true;
	            }

	            if (!includeBooking) {
	                continue;
	            }

	            Map<String, Object> map = new LinkedHashMap<>();

	            map.put("bookingId", booking.getBookingId());
	            map.put("serviceDate", booking.getServiceDate());
	            map.put("servicetime", booking.getServicetime());
	            map.put("name", booking.getName());
	            map.put("mobileNumber", resolveMobileNumber(booking));
	            map.put("doctorId", booking.getDoctorId());
	            map.put("doctorName", booking.getDoctorName());
	            map.put("paymentType", booking.getPaymentType());
	            map.put("visitType", booking.getVisitType());
	            map.put("status", booking.getStatus());
	            map.put("followupStatus", booking.getFollowupStatus());
	            map.put("patientId", booking.getPatientId());
	            map.put("clinicId", booking.getClinicId());
	            map.put("customerId", booking.getCustomerId());
	            map.put("branchId", booking.getBranchId());
	            map.put("problem", booking.getProblem());

	            filteredBookings.add(map);
	        }

	        Map<String, Object> summary = new LinkedHashMap<>();

	        summary.put("totalBookings", filteredBookings.size());
	        summary.put("inProgressCount", inProgressCount);
	        summary.put("completedCount", completedCount);
	        summary.put("dueForInvestigationCount", dueForInvestigationCount);
	        summary.put("investigationDoneCount", investigationDoneCount);

	        if (filteredBookings.isEmpty()) {

	            return ResponseEntity.ok(
	                    new Response(
	                            true,
	                            Collections.emptyList(),
	                            summary,
	                            "No bookings found",
	                            200,
	                            null,
	                            null));
	        }

	        return ResponseEntity.ok(
	                new Response(
	                        true,
	                        filteredBookings,
	                        summary,
	                        "Bookings fetched successfully",
	                        200,
	                        null,
	                        null));

	    } catch (Exception e) {

	        log.error(
	                "Error while fetching filtered bookings. clinicId={}, branchId={}, error={}",
	                clinicId,
	                branchId,
	                e.getMessage(),
	                e);

	        return ResponseEntity.status(
	                HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(
	                        new Response(
	                                false,
	                                null,
	                                null,
	                                "Error fetching bookings : " + e.getMessage(),
	                                500,
	                                null,
	                                null));
	    }
	}

	@Override
	public ResponseEntity<Response> getUpcomingBookings(String clinicId, String branchId, int option) {
	  List<List<Map<String, Object>>> list = new ArrayList<>();
		List<Map<String, Object>> LIST_TO_STORE_FOLLOWUP_BOOKINGS  = new LinkedList<>();
	    try {
	        int days;
	        if (option == 1) {
	            days = 3;
	        } else if (option == 2) {
	            days = 7;
	        } else {
	            return ResponseEntity.badRequest()
	                    .body(new Response(false, null, null, "Invalid option (1=3days, 2=7days)", 400, null, null));
	        }

	        LocalDate startDate = LocalDate.now().minusDays(1);
	        LocalDate endDate = startDate.plusDays(days + 1);

	        List<Booking> bookings = repository.findByClinicIdAndBranchIdAndServiceDateBetween(
	                clinicId, branchId,
	                startDate.format(FORMATTER),
	                endDate.format(FORMATTER));
			////System.out.println(bookings);
	        List<BookingResponse> res = toResponses(bookings);
			////System.out.println(res);
			for (BookingResponse n : res) {
				TreatmentScheduleDTO sittings = new TreatmentScheduleDTO();
				try {
					sittings = clinicAdminFeign.getSittings(
							n.getClinicId(), n.getBranchId(), n.getBookingId(), n.getServiceDate(),LocalDate.parse(n.getServiceDate()).plusDays(7).toString()
					);} catch (Exception e) {
					///System.out.println(e.getMessage());
					log.warn("Error fetching sittings/payments for bookingId={}: {}", n.getBookingId(), e.getMessage());}
				///System.out.println(sittings);
				Map<String, Double> info = new HashMap<>();
				try {
					info = clinicAdminFeign.getAllPayments(n.getBookingId());
				}catch (Exception e){System.out.println(e.getMessage());
				}
				////System.out.println(info);
					list.add(buildBookingData(n,sittings,info));
				}
			List<String> ids = new LinkedList<>();
			list.stream().flatMap(List::stream).filter(Objects::nonNull).forEach(i->ids.add((String)i.get("bookingId")));
			List<TreatmentScheduleDTO> dtos = new LinkedList<>();
			////System.out.println(ids);
           try{
			dtos = clinicAdminFeign.getFilteredSittings(
					clinicId,branchId, startDate.plusDays(1).toString(),endDate.minusDays(1).toString(),ids
			);
			  //// System.out.println(dtos);
		   }catch (Exception e){///System.out.println(dtos);
		   }

			if(dtos!=null && !dtos.isEmpty()){
				LIST_TO_STORE_FOLLOWUP_BOOKINGS = buildBookingDataBasedOnSesions(dtos);}
			if(!LIST_TO_STORE_FOLLOWUP_BOOKINGS.isEmpty()){
				list.add(LIST_TO_STORE_FOLLOWUP_BOOKINGS);}

			// Flatten nested list
			///System.out.println(list);
			List<Map<String, Object>> flatList = list.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());
			///System.out.println(flatList);
			// Summary counts
			Map<String, Object> summary = new HashMap<>();
			if (!flatList.isEmpty()) {
				long totalCount = flatList.size();

				long pendingCount = flatList.stream()
						.filter(b -> "PENDING".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

				long confirmedCount = flatList.stream()
						.filter(b -> "CONFIRMED".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

				long inProgressCount = flatList.stream()
						.filter(b -> "IN-PROGRESS".equalsIgnoreCase(
								Optional.ofNullable((String) b.get("status")).orElse("")))
						.count();

				long other = totalCount - pendingCount - confirmedCount - inProgressCount;
		    summary = new HashMap<>();
			summary.put("totalAppointments", totalCount);
			summary.put("pending", pendingCount);
			summary.put("confirmed", confirmedCount);
			summary.put("inProgress", inProgressCount);
			summary.put("Other", other);
	        summary.put("startDate", startDate.plusDays(1).toString());
	        summary.put("endDate", endDate.minusDays(1).toString());}

	        return ResponseEntity.ok(new Response(true, flatList, summary, "Upcoming bookings fetched", 200, null, null));

	    } catch (Exception e) {
	        log.error("Error fetching upcoming bookings for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(false, null, null, "Error fetching upcoming bookings: " + e.getMessage(), 500, null, null));
	    }
	}

//	@Override
//	public ResponseEntity<Response> getBookingByDate(String clinicId, String branchId, String date) {
//
//		try {
//			LocalDate dte = LocalDate.parse(date);
//
//			List<Booking> bookings = repository.findByClinicIdAndBranchIdAndServiceDate(clinicId, branchId,
//					dte.format(FORMATTER));
//
//			List<BookingResponse> res = toResponses(bookings);
//			res = res
//			        .stream()
//			        .filter(booking -> {
//			            String status = booking.getStatus();
//			            return status != null
//			                    && !status.equalsIgnoreCase("CANCELLED")
//			                    && !status.equalsIgnoreCase("CANCEL")
//			                    && !status.equalsIgnoreCase("COMPLETED");
//			        })
//			        .collect(Collectors.toList());
//
//			try {
//				res = res.stream().map(n -> {
//					List<Session> lst = physioDoctorFeign.getPhysioByBookingId(n.getBookingId(), n.getServiceDate())
//							.getBody();
//					if (lst != null) {
//						n.setSession(lst);
//						n.setVisitType("session");
//					} else {
//						n.setSession(null);
//					}
//					return n;
//				}).toList();
//
//			} catch (Exception e) {
//				log.warn("Error while fetching session details for clinicId={}, branchId={}: {}", clinicId, branchId, e.getMessage());
//			}
//
//			long totalCount = bookings.size();
//
//			long pendingCount = bookings.stream()
//					.filter(b -> "PENDING".equalsIgnoreCase(Optional.ofNullable(b.getStatus()).orElse("")))
//					.count();
//
//			long confirmedCount = bookings.stream()
//					.filter(b -> "CONFIRMED".equalsIgnoreCase(Optional.ofNullable(b.getStatus()).orElse("")))
//					.count();
//
//			long inProgressCount = bookings.stream()
//					.filter(b -> "IN-PROGRESS".equalsIgnoreCase(Optional.ofNullable(b.getStatus()).orElse("")))
//					.count();
//
//			Map<String, Object> summary = new HashMap<>();
//			summary.put("totalAppointments", totalCount);
//			summary.put("pending", pendingCount);
//			summary.put("confirmed", confirmedCount);
//			summary.put("inProgress", inProgressCount);
//
//			return ResponseEntity.ok(new Response(true, res, summary, "Bookings fetched", 200, null, null));
//
//		} catch (Exception e) {
//			log.error("Error fetching bookings by date for clinicId={}, branchId={}, date={}: {}", clinicId, branchId, date, e.getMessage(), e);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//					new Response(false, null, null, "Error fetching bookings: " + e.getMessage(), 500, null, null));
//		}
//	}

	@Override
	public ResponseEntity<Response> getBookingByCustomRange(
	        String clinicId,
	        String branchId,
	        String start,
	        String end) {
	        try {
	       List<List<Map<String, Object>>> list = new ArrayList<>();
			List<Map<String, Object>> LIST_TO_STORE_FOLLOWUP_BOOKINGS = new LinkedList<>();
	        log.info("Fetching bookings for clinicId: {}, branchId: {}, start: {}, end: {}",
	                clinicId, branchId, start, end);

	        LocalDate startDate = LocalDate.parse(start);
	        LocalDate endDate = LocalDate.parse(end);

	        String fromDate = startDate.minusDays(1).format(FORMATTER);
	        String toDate = endDate.plusDays(1).format(FORMATTER);

	        List<Booking> bookings =
	                repository.findByClinicIdAndBranchIdAndServiceDateBetween(
	                        clinicId,
	                        branchId,
	                        fromDate,
	                        toDate);

	        log.info("Found {} bookings", bookings != null ? bookings.size() : 0);

	        List<BookingResponse> responses = new ArrayList<>();
	        if (bookings != null && !bookings.isEmpty()) {
	            responses.addAll(toResponses(bookings));
	        }
			for (BookingResponse n : responses) {

				TreatmentScheduleDTO sittings = new TreatmentScheduleDTO();
				try {
					sittings = clinicAdminFeign.getSittings(
							n.getClinicId(), n.getBranchId(), n.getBookingId(), startDate.toString() ,endDate.toString()
					);
				 } catch (Exception e) {
					///System.out.println(e.getMessage());
					log.warn("Error fetching sittings/payments for bookingId={}: {}", n.getBookingId(), e.getMessage());}
				///System.out.println(sittings);
				Map<String, Double> info = new HashMap<>();
				try {
					info = clinicAdminFeign.getAllPayments(n.getBookingId());
				}catch (Exception e){System.out.println(e.getMessage());}
				///System.out.println(info);
					list.add(buildBookingData(n,sittings,info));
				}
				List<String> ids = new LinkedList<>();
				list.stream().flatMap(List::stream).filter(Objects::nonNull).forEach(i->ids.add((String)i.get("bookingId")));
				List<TreatmentScheduleDTO> dtos = new LinkedList<>();
				///System.out.println(ids);
            try{
			dtos = clinicAdminFeign.getFilteredSittings(
					clinicId,branchId, startDate.toString(),endDate.toString(),ids
			);}catch (Exception e){}

			if(dtos!=null && !dtos.isEmpty()){
				LIST_TO_STORE_FOLLOWUP_BOOKINGS = buildBookingDataBasedOnSesions(dtos);}

				if(!LIST_TO_STORE_FOLLOWUP_BOOKINGS.isEmpty()){
					list.add(LIST_TO_STORE_FOLLOWUP_BOOKINGS);}

				// Flatten nested list
				List<Map<String, Object>> flatList = list.stream()
						.flatMap(List::stream)
						.collect(Collectors.toList());

				// Summary counts
				Map<String, Object> summary = new HashMap<>();
				if (!flatList.isEmpty()) {
					long totalCount = flatList.size();

					long pendingCount = flatList.stream()
							.filter(b -> "PENDING".equalsIgnoreCase(
									Optional.ofNullable((String) b.get("status")).orElse("")))
							.count();

					long confirmedCount = flatList.stream()
							.filter(b -> "CONFIRMED".equalsIgnoreCase(
									Optional.ofNullable((String) b.get("status")).orElse("")))
							.count();

					long inProgressCount = flatList.stream()
							.filter(b -> "IN-PROGRESS".equalsIgnoreCase(
									Optional.ofNullable((String) b.get("status")).orElse("")))
							.count();

				long other = totalCount - pendingCount - confirmedCount - inProgressCount;
					summary = new HashMap<>();
					summary.put("totalAppointments", totalCount);
					summary.put("pending", pendingCount);
					summary.put("confirmed", confirmedCount);
					summary.put("inProgress", inProgressCount);
					summary.put("Other", other);
	        log.info("Custom range bookings fetched successfully. Total appointments: {}", totalCount);}

	        return ResponseEntity.ok(
	                new Response(true, flatList, summary, "Custom range bookings fetched", 200, null, null));

	    } catch (Exception e) {
	        log.error("Error fetching custom range bookings for clinicId: {}, branchId: {}", clinicId, branchId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(false, null, null, e.getMessage(), 500, null, null));
	    }
	}


	public ResponseEntity<Response> getBookingById(String bookingId) {
	    try {
	        Optional<Booking> bookingOpt = repository.findByBookingId(bookingId);

	        if (bookingOpt.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(false, null, null, "Booking not found", 404, null, null));
	        }

	        Booking booking = bookingOpt.get();
	        BookingResponse res = mapper.convertValue(booking, BookingResponse.class);

	        // Optionally enrich with physio sessions if needed
	        return ResponseEntity.ok(
	                new Response(true, res, null, "Booking fetched successfully", 200, null, null)
	        );

	    } catch (Exception e) {
	        log.error("Error in getBookingById: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(false, null, null, e.getMessage(), 500, null, null));
	    }
	}


	private Booking updateForFollowup(BookingResponse dto) {
		try {
			Booking entity = repository.findByBookingIdIgnoreCase(dto.getBookingId())
					.orElseThrow(() -> new RuntimeException("Invalid Booking Id"));
			List<FollowupBooking> lst = new LinkedList<>();
			if (entity.getFollwupBookings() == null) {
				lst = new LinkedList<>();
			} else {
				lst = entity.getFollwupBookings();
			}
			entity.setVisitType("follow-up");
			if (dto.getBookingFor() != null && !dto.getBookingFor().isEmpty())
				entity.setBookingFor(dto.getBookingFor());

			if (dto.getName() != null && !dto.getName().isEmpty())
				entity.setName(dto.getName());

			if (dto.getPatientMobileNumber() != null && !dto.getPatientMobileNumber().isEmpty())
				entity.setPatientMobileNumber(dto.getPatientMobileNumber());

			if (dto.getPatientId() != null && !dto.getPatientId().isEmpty())
				entity.setPatientId(dto.getPatientId());

			if (dto.getVisitType() != null && !dto.getVisitType().isEmpty())
				entity.setVisitType(dto.getVisitType());

			if (dto.getPatientAddress() != null && !dto.getPatientAddress().isEmpty())
				entity.setPatientAddress(dto.getPatientAddress());

			if (dto.getAge() != null && !dto.getAge().isEmpty())
				entity.setAge(dto.getAge());

			if (dto.getGender() != null && !dto.getGender().isEmpty())
				entity.setGender(dto.getGender());

			if (dto.getMobileNumber() != null && !dto.getMobileNumber().isEmpty())
				entity.setMobileNumber(dto.getMobileNumber());

			if (dto.getCustomerId() != null && !dto.getCustomerId().isEmpty())
				entity.setCustomerId(dto.getCustomerId());

//			if (dto.getCustomerDeviceId() != null && !dto.getCustomerDeviceId().isEmpty())
//				entity.setCustomerDeviceId(dto.getCustomerDeviceId());

			// -------- FOLLOWUPS -------

			if (dto.getFreeFollowUps() != null)
				entity.setFreeFollowUps(dto.getFreeFollowUps());

			if (dto.getFollowupDate() != null && !dto.getFollowupDate().isEmpty())
				entity.setFollowupDate(dto.getFollowupDate());

			if (dto.getFollowupStatus() != null) {
				entity.setFollowupStatus(dto.getFollowupStatus());
			}
			// -------- PROBLEM --------
			if (dto.getProblem() != null && !dto.getProblem().isEmpty())
				entity.setProblem(dto.getProblem());

			if (dto.getSymptomsDuration() != null && !dto.getSymptomsDuration().isEmpty())
				entity.setSymptomsDuration(dto.getSymptomsDuration());

			// -------- CLINIC --------
			if (dto.getClinicId() != null && !dto.getClinicId().isEmpty())
				entity.setClinicId(dto.getClinicId());

			if (dto.getClinicName() != null && !dto.getClinicName().isEmpty())
				entity.setClinicName(dto.getClinicName());

//			if (dto.getClinicDeviceId() != null && !dto.getClinicDeviceId().isEmpty())
//				entity.setClinicDeviceId(dto.getClinicDeviceId());

			if (dto.getBranchId() != null && !dto.getBranchId().isEmpty())
				entity.setBranchId(dto.getBranchId());

			if (dto.getBranchname() != null && !dto.getBranchname().isEmpty())
				entity.setBranchname(dto.getBranchname());

			// -------- DOCTOR --------
			if (dto.getDoctorId() != null && !dto.getDoctorId().isEmpty())
				entity.setDoctorId(dto.getDoctorId());

			if (dto.getDoctorName() != null && !dto.getDoctorName().isEmpty())
				entity.setDoctorName(dto.getDoctorName());

//			if (dto.getDoctorWebDeviceId() != null && !dto.getDoctorWebDeviceId().isEmpty())
//				entity.setDoctorWebDeviceId(dto.getDoctorWebDeviceId());

			if (dto.getStatus() != null) {

				entity.setStatus(dto.getStatus());

				List<Status> statusList = entity.getCurrentStatus();
				if (statusList == null)
					statusList = new ArrayList<>();

				Status s = new Status();
				s.setStatus(dto.getStatus());
				s.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

				statusList.add(s);
				entity.setCurrentStatus(statusList);
			}

			if (dto.getServiceDate() != null && !dto.getServiceDate().isEmpty())
				entity.setServiceDate(dto.getServiceDate());

			if (dto.getServicetime() != null && !dto.getServicetime().isEmpty())
				entity.setServicetime(dto.getServicetime());

			if (dto.getConsultationType() != null && !dto.getConsultationType().isEmpty())
				entity.setConsultationType(dto.getConsultationType());
			if (dto.getConsultationFee() != null) {
				List<ConsultationFees> consultationFees = entity.getListOfConsultationFee();
				ConsultationFees fee = new ConsultationFees();
				fee.setConsulationFee(dto.getConsultationFee());
				fee.setDATE_TIME(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				consultationFees.add(fee);
				Collections.reverse(consultationFees);
				entity.setConsultationFee(consultationFees.get(0).getConsulationFee());
				entity.setListOfConsultationFee(consultationFees);
			}
			if (dto.getConsultationExpiration() != null && !dto.getConsultationExpiration().isEmpty())
				entity.setConsultationExpiration(dto.getConsultationExpiration());

			// -------- STATUS --------
			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}

			// -------- FILES --------
			if (dto.getAttachments() != null && !dto.getAttachments().isEmpty())
				entity.setAttachments(dto.getAttachments());

			if (dto.getConsentFormPdf() != null && !dto.getConsentFormPdf().isEmpty())
				entity.setConsentFormPdf(dto.getConsentFormPdf());
			// -------- PAYMENT --------
			if (dto.getPaymentType() != null && !dto.getPaymentType().isEmpty()) {
				entity.setPaymentType(dto.getPaymentType());
			}
			if (dto.getPaymentStatus() != null && !dto.getPaymentStatus().isEmpty())
				entity.setPaymentStatus(dto.getPaymentStatus());

			if (dto.getTotalFee() > 0)
				entity.setTotalFee(dto.getTotalFee());

			if (dto.getDoctorRefCode() != null && !dto.getDoctorRefCode().isEmpty())
				entity.setDoctorRefCode(dto.getDoctorRefCode());

			// -------- THERAPY --------
			if (dto.getTheraphyAnswers() != null)
				entity.setTheraphyAnswers(mapper.convertValue(dto.getTheraphyAnswers(),
						new TypeReference<Map<String, List<TheraphyAnswersEntity>>>() {
						}));

			if (dto.getDueAmount() >= 0)
				entity.setDueAmount(dto.getDueAmount());

			// -------- REFERRAL --------
			if (dto.getReferredByType() != null && !dto.getReferredByType().isEmpty())
				entity.setReferredByType(dto.getReferredByType());

			if (dto.getReferredByName() != null && !dto.getReferredByName().isEmpty())
				entity.setReferredByName(dto.getReferredByName());

			// -------- MEDICAL --------
			if (dto.getPreviousInjuries() != null && !dto.getPreviousInjuries().isEmpty())
				entity.setPreviousInjuries(dto.getPreviousInjuries());

			if (dto.getCurrentMedications() != null && !dto.getCurrentMedications().isEmpty())
				entity.setCurrentMedications(dto.getCurrentMedications());

			if (dto.getAllergies() != null && !dto.getAllergies().isEmpty())
				entity.setAllergies(dto.getAllergies());

			if (dto.getOccupation() != null && !dto.getOccupation().isEmpty())
				entity.setOccupation(dto.getOccupation());

			// -------- INSURANCE --------
			if (dto.getInsuranceProvider() != null && !dto.getInsuranceProvider().isEmpty())
				entity.setInsuranceProvider(dto.getInsuranceProvider());

			if (dto.getPolicyNumber() != null && !dto.getPolicyNumber().isEmpty())
				entity.setPolicyNumber(dto.getPolicyNumber());

			// -------- ACTIVITY --------
			if (dto.getActivityLevels() != null && !dto.getActivityLevels().isEmpty())
				entity.setActivityLevels(dto.getActivityLevels());

			// -------- TREATMENTS --------
			if (dto.getFoc() != null)
				entity.setFoc(dto.getFoc());
			if (entity.getFreeFollowUps() != null && entity.getFreeFollowUps() == 0) {
				entity.setIsFollowupStatus(true);
			}
			int days = 0;
			try {
				if (entity.getConsultationExpiration() != null) {
					String consultationExp = entity.getConsultationExpiration();
					days = Integer.parseInt(consultationExp.replaceAll("[^0-9]", ""));
				}

				LocalDate serviceDate = LocalDate.parse(entity.getServiceDate());

				LocalDate expiryDate = serviceDate.plusDays(days);

				LocalDate today = LocalDate.now();

				if (!today.isAfter(expiryDate) && entity.getFreeFollowUps() != null && entity.getFreeFollowUps() == 0) {
					entity.setIsFollowupStatus(true);
				} else if (today.isAfter(expiryDate)) {
					entity.setIsFollowupStatus(true);
				} else {
					entity.setIsFollowupStatus(false);
				}

				if(dto.getFoc().equalsIgnoreCase("foc") && today.isAfter(expiryDate)){
					entity.setFreeFollowUps(0);
					entity.setFreeFollowUpsLeft(0);
				}


			} catch (Exception e) {
				entity.setIsFollowupStatus(false);
			}

			if(entity.getFreeFollowUpsLeft() == 0){
				entity.setFreeFollowUps(0);
			}

			if(dto.getFoc().equalsIgnoreCase("paid")){
				int followups =	adminServiceClient.getFreeFollowUps(entity.getClinicId());
				entity.setFreeFollowUps(followups);
			}

			if (dto.getFoc() != null && dto.getPaymentType() != null) {
				if ("paid".equalsIgnoreCase(dto.getFoc()) && "not paid".equalsIgnoreCase(dto.getPaymentType())) {
					entity.setStatus("pending");
				} else if ("foc".equalsIgnoreCase(dto.getFoc()) && "not paid".equalsIgnoreCase(dto.getPaymentType())) {
					entity.setStatus("confirmed");
				} else {
					if ("paid".equalsIgnoreCase(dto.getFoc()) && !dto.getPaymentType().isEmpty()) {
						entity.setStatus("confirmed");
					}
				}
			}
			FollowupBooking followup = new FollowupBooking();
			followup.setDoctorId(entity.getDoctorId());
			followup.setDoctorName(entity.getDoctorName());
			followup.setServiceDate(entity.getServiceDate());
			followup.setServicetime(entity.getServicetime());
			followup.setStatus(entity.getStatus());
			followup.setVisitType(entity.getVisitType());
			lst.add(followup);

			entity.setFollwupBookings(lst);
			Booking booking = repository.save(entity);
			booking.setFollwupBookings(null);
			return booking;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> searchBookings(String clinicId, String input) {

		List<Booking> bookings = new ArrayList<>();

		// Mobile Number Search
		if (input.matches("^[6-9]\\d{9}$")) {

			bookings = repository.findByMobileNumberAndClinicId(input, clinicId);

			if (bookings.isEmpty()) {
				bookings = repository.findByPatientMobileNumberAndClinicId(input, clinicId);
			}

		} else {

			if (input.length() < 3) {
				throw new IllegalArgumentException("Please enter at least 3 characters to search by patient name");
			}

			bookings = repository.findByPatientIdAndClinicId(input, clinicId);

			if (bookings.isEmpty()) {
				bookings = repository.findByNameContainingIgnoreCaseAndClinicId(input, clinicId);
			}
		}

		if (bookings.isEmpty()) {
			return new ArrayList<>();
		}

		List<BookingResponse> dto = toResponses(bookings);

		List<Map<String, Object>> list = new ArrayList<>();

		dto.forEach(n -> {

			Map<String, Object> map = new LinkedHashMap<>();

			map.put("bookingId", n.getBookingId());
			map.put("serviceDate", n.getServiceDate());
			map.put("servicetime", n.getServicetime());
			map.put("name", n.getName());
			map.put("mobileNumber", resolveMobileNumber(n));
			map.put("doctorId", n.getDoctorId());
			map.put("doctorName", n.getDoctorName());
			map.put("paymentType", n.getPaymentType());
			map.put("visitType", n.getVisitType());
			map.put("status", n.getStatus());
			map.put("followupStatus", n.getFollowupStatus());
			map.put("patientId", n.getPatientId());
			map.put("clinicId", n.getClinicId());
			map.put("customerId", n.getCustomerId());
			map.put("branchId", n.getBranchId());
			map.put("problem", n.getProblem());

			list.add(map);
		});

		return list;
	}



	@Scheduled(cron = "0 0/5 * * * ?")  // runs every 5 minutes
	public void followupReminder() {
		try {
			ZoneId zoneId = ZoneId.of("Asia/Kolkata");
			LocalDate today = LocalDate.now(zoneId);

			// Current time in Asia/Kolkata
			LocalTime nowTime = LocalTime.now(zoneId);

			// Format current time to match DB servicetime format (hh:mm AM/PM)
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
			String currentTime = nowTime.format(timeFormatter);
			///System.out.println(currentTime);
			// ✅ Fetch only today's bookings after current time directly from DB
			List<Booking> responses = repository.findByServiceDateAndServicetimeAfter(today.toString(), currentTime);
			///System.out.println(responses);
			// Filter out cancelled/completed bookings and only "sitting" type
			List<Booking> followUpBookings = responses.stream()
					.filter(booking -> {
						String status = booking.getStatus();
						return status != null
								&& !status.equalsIgnoreCase("CANCELLED")
								&& !status.equalsIgnoreCase("CANCEL")
								&& !status.equalsIgnoreCase("COMPLETED");
					})
					.filter(booking -> {
						String visitType = booking.getVisitType();
						return visitType != null && visitType.equalsIgnoreCase("sitting");
					})
					.collect(Collectors.toList());
			///System.out.println(followUpBookings);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

			for (Booking booking : followUpBookings) {
				try {
					String timeStr = booking.getServicetime().trim().toUpperCase(Locale.ENGLISH);
					LocalTime serviceTime = LocalTime.parse(timeStr, formatter);
					LocalDateTime appointmentTime = LocalDateTime.of(today, serviceTime);
					///System.out.println(appointmentTime);
					long minutesUntilAppointment = Duration.between(LocalDateTime.now(zoneId), appointmentTime).toMinutes();
					///System.out.println(minutesUntilAppointment);
					// ✅ Send reminders only if appointment is within 25–30 minutes OR 100–120 minutes
					if ((minutesUntilAppointment >= 25 && minutesUntilAppointment <= 30)
							|| (minutesUntilAppointment >= 100 && minutesUntilAppointment <= 120)) {

						notificationService.sendFollowupReminder(
								booking.getClinicId(),
								booking.getBranchId(),
								booking.getName(),
								booking.getPatientId(),
								booking.getServicetime(),
								booking.getBookingId(),
								booking.getDoctorName()
						);
					}
				} catch (Exception e) {
					log.warn("Error parsing time for bookingId={}: {}", booking.getBookingId(), e.getMessage());
				}
			}

			log.info("Follow-up reminder job executed. Total upcoming bookings after current time: {}", followUpBookings.size());

		} catch (Exception e) {
			log.error("Error in followupReminder job: {}", e.getMessage(), e);
		}
	}


	private static String resolveMobileNumber(BookingResponse n) {
        String patientMobile = n.getPatientMobileNumber();
        if (patientMobile != null && !patientMobile.isEmpty()) {
            return patientMobile;
        }
        return n.getMobileNumber();
    }


	private List<Map<String, Object>> buildBookingData(BookingResponse booking,
	                                                   TreatmentScheduleDTO schedule,
	                                                   Map<String, Double> paymentInfo) {
		List<Map<String, Object>> responseList = new ArrayList<>();
		///booking.setVisitType("session");
		try {
			if (schedule != null && !schedule.getSittings().isEmpty()) {
				///System.out.println("inside");
				for (Sittings sitting : schedule.getSittings()) {
					///System.out.println(sitting);
					Map<String, Object> map = new LinkedHashMap<>();
					// Booking info
					map.put("bookingId", booking.getBookingId());
					map.put("serviceDate", booking.getServiceDate());
					map.put("servicetime", booking.getServicetime());
					map.put("name", booking.getName());
					map.put("mobileNumber", resolveMobileNumber(booking));
					map.put("doctorId", booking.getDoctorId());
					map.put("doctorName", booking.getDoctorName());
					map.put("paymentType", booking.getPaymentType());
					map.put("visitType", booking.getVisitType());
					map.put("status", booking.getStatus());
					map.put("followupStatus", booking.getFollowupStatus());
					map.put("patientId", booking.getPatientId());
					map.put("clinicId", booking.getClinicId());
					map.put("customerId", booking.getCustomerId());
					map.put("branchId", booking.getBranchId());
					map.put("session", booking.getSession());
					map.put("problem", booking.getProblem());
					map.put("consultationfee", booking.getConsultationFee());
					map.put("freeFollowUps", booking.getFreeFollowUps());
					map.put("address", booking.getPatientAddress());
					map.put("freeFollowupsLeft", booking.getFreeFollowUpsLeft());
					map.put("sitting", sitting);

					// Sitting info from DTO
					map.put("sittingNumber", sitting.getSittingNumber());
					map.put("packageType", schedule.getPackageType());
					map.put("packageId", schedule.getPackageId());
					map.put("packageName", schedule.getPackageName());
					map.put("noOfSittings", schedule.getNumberOfSittings());

					// Payment info
					if (paymentInfo != null) {
						if (paymentInfo.containsKey("finalAmount")) map.put("finalAmount", paymentInfo.get("finalAmount"));
						if (paymentInfo.containsKey("paidAmount")) map.put("paidAmount", paymentInfo.get("paidAmount"));
						if (paymentInfo.containsKey("dueAmount")) map.put("dueAmount", paymentInfo.get("dueAmount"));
						if (paymentInfo.containsKey("discount")) map.put("discount", paymentInfo.get("discount"));
						if (paymentInfo.containsKey("actualAccount")) map.put("actualAmount", paymentInfo.get("actualAccount"));
					}

					responseList.add(map);
				}
			} else {
				Map<String, Object> map = new LinkedHashMap<>();
				// Booking info
				map.put("bookingId", booking.getBookingId());
				map.put("serviceDate", booking.getServiceDate());
				map.put("servicetime", booking.getServicetime());
				map.put("name", booking.getName());
				map.put("mobileNumber", resolveMobileNumber(booking));
				map.put("doctorId", booking.getDoctorId());
				map.put("doctorName", booking.getDoctorName());
				map.put("paymentType", booking.getPaymentType());
				map.put("visitType", booking.getVisitType());
				map.put("status", booking.getStatus());
				map.put("followupStatus", booking.getFollowupStatus());
				map.put("patientId", booking.getPatientId());
				map.put("clinicId", booking.getClinicId());
				map.put("customerId", booking.getCustomerId());
				map.put("branchId", booking.getBranchId());
				map.put("session", booking.getSession());
				map.put("problem", booking.getProblem());
				map.put("consultationfee", booking.getConsultationFee());
				map.put("freeFollowUps", booking.getFreeFollowUps());
				map.put("address", booking.getPatientAddress());
				map.put("freeFollowupsLeft", booking.getFreeFollowUpsLeft());
				map.put("sitting", null);

				// Sitting info from DTO
				map.put("sittingNumber", null);
				map.put("packageType", null);
				map.put("packageId", null);
				map.put("packageName", null);
				map.put("noOfSittings", null);

				// Payment info
				if (paymentInfo != null) {
					if (paymentInfo.containsKey("finalAmount")) map.put("finalAmount", paymentInfo.get("finalAmount"));
					if (paymentInfo.containsKey("paidAmount")) map.put("paidAmount", paymentInfo.get("paidAmount"));
					if (paymentInfo.containsKey("dueAmount")) map.put("dueAmount", paymentInfo.get("dueAmount"));
					if (paymentInfo.containsKey("discount")) map.put("discount", paymentInfo.get("discount"));
					if (paymentInfo.containsKey("actualAccount")) map.put("actualAmount", paymentInfo.get("actualAccount"));
				}

				responseList.add(map);
				///System.out.println(responseList);
			}
		} catch (Exception e) {
			// Log error safely
			///System.err.println("Error building booking data: " + e.getMessage());
		}

		return responseList;
	}


	private List<Map<String, Object>> buildBookingDataBasedOnSesions(List<TreatmentScheduleDTO> schedules) {
		List<Map<String, Object>> responseList = new ArrayList<>();
		///System.out.println(schedules);
		try {
			for (TreatmentScheduleDTO schedule : schedules) {
				Booking booking = new Booking();
				try {
					booking = repository.findByBookingId(schedule.getBookingId()).orElse(null);
					booking.setVisitType("sitting");
				} catch (Exception e) {
					///System.err.println("Error fetching booking: " + e.getMessage());
				}

				if (booking == null || schedule.getSittings().isEmpty()) {
					continue;
				}

				BookingResponse bookingResponse = toResponse(booking);

				Map<String, Double> paymentInfo = new HashMap<>();
				try {
					paymentInfo = clinicAdminFeign.getAllPayments(bookingResponse.getBookingId());
					///System.out.println(paymentInfo);
				} catch (Exception e) {
					///System.err.println("Error fetching payment info: " + e.getMessage());
				}

				for (Sittings sitting : schedule.getSittings()) {
					Map<String, Object> map = new LinkedHashMap<>();
					///System.out.println(sitting);
					// Booking info
					map.put("bookingId", bookingResponse.getBookingId());
					map.put("serviceDate", bookingResponse.getServiceDate());
					map.put("servicetime", bookingResponse.getServicetime());
					map.put("name", bookingResponse.getName());
					map.put("mobileNumber", resolveMobileNumber(bookingResponse));
					map.put("doctorId", bookingResponse.getDoctorId());
					map.put("doctorName", bookingResponse.getDoctorName());
					map.put("paymentType", bookingResponse.getPaymentType());
					map.put("visitType", bookingResponse.getVisitType());
					map.put("status", bookingResponse.getStatus());
					map.put("followupStatus", bookingResponse.getFollowupStatus());
					map.put("patientId", bookingResponse.getPatientId());
					map.put("clinicId", bookingResponse.getClinicId());
					map.put("customerId", bookingResponse.getCustomerId());
					map.put("branchId", bookingResponse.getBranchId());
					map.put("session", bookingResponse.getSession());
					map.put("problem", bookingResponse.getProblem());
					map.put("consultationfee", bookingResponse.getConsultationFee());
					map.put("freeFollowUps", bookingResponse.getFreeFollowUps());
					map.put("address", bookingResponse.getPatientAddress());
					map.put("freeFollowupsLeft", bookingResponse.getFreeFollowUpsLeft());
					map.put("sitting", sitting);

					// Sitting info
					map.put("sittingNumber", sitting.getSittingNumber());
					map.put("packageType", schedule.getPackageType());
					map.put("packageId", schedule.getPackageId());
					map.put("packageName", schedule.getPackageName());
					map.put("noOfSittings", schedule.getNumberOfSittings());

					// Payment info
					if (paymentInfo != null) {
						if (paymentInfo.containsKey("finalAmount")) map.put("finalAmount", paymentInfo.get("finalAmount"));
						if (paymentInfo.containsKey("paidAmount")) map.put("paidAmount", paymentInfo.get("paidAmount"));
						if (paymentInfo.containsKey("dueAmount")) map.put("dueAmount", paymentInfo.get("dueAmount"));
						if (paymentInfo.containsKey("discount")) map.put("discount", paymentInfo.get("discount"));
						if (paymentInfo.containsKey("actualAccount")) map.put("actualAmount", paymentInfo.get("actualAccount"));
					}

					responseList.add(map);
				}
			}
		} catch (Exception e) {
			///System.err.println("Error building booking data based on sessions: " + e.getMessage());
		}

		return responseList;
	}
	@Override
	public Response importPhysioAppointmentsFromExcel(MultipartFile file) {

	    Response response = new Response();

	    int totalRows = 0;
	    int savedRows = 0;
	    int failedRows = 0;

	    List<Map<String, Object>> failedAppointments =
	            new ArrayList<>();

	    // =====================================================
	    // GENERATE ONE IMPORT ID FOR THIS EXCEL UPLOAD
	    // =====================================================

	    String importId = UUID.randomUUID().toString();

	    try {

	        // =====================================================
	        // 1. FILE VALIDATION
	        // =====================================================

	        if (file == null || file.isEmpty()) {

	            response.setSuccess(false);
	            response.setMessage("Excel file is required");
	            response.setStatus(400);

	            return response;
	        }

	        String fileName = file.getOriginalFilename();

	        if (fileName == null
	                || (!fileName.toLowerCase().endsWith(".xlsx")
	                && !fileName.toLowerCase().endsWith(".xls"))) {

	            response.setSuccess(false);

	            response.setMessage(
	                    "Only Excel files (.xlsx or .xls) are allowed"
	            );

	            response.setStatus(400);

	            return response;
	        }

	        // =====================================================
	        // 2. READ EXCEL FILE
	        // =====================================================

	        try (InputStream inputStream = file.getInputStream();
	             Workbook workbook = WorkbookFactory.create(inputStream)) {

	            Sheet sheet = workbook.getSheetAt(0);

	            // =====================================================
	            // 3. SKIP HEADER ROW
	            // =====================================================

	            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

	                Row row = sheet.getRow(i);

	                if (row == null) {
	                    continue;
	                }

	                totalRows++;

	                int excelRowNumber = i + 1;

	                String patientName = "";
	                String patientMobileNumber = "";

	                try {

	                    // =====================================================
	                    // 4. CREATE BOOKING REQUEST
	                    // =====================================================

	                    BookingRequset bookingRequest =
	                            new BookingRequset();

	                    // =====================================================
	                    // SET IMPORT ID
	                    // SAME IMPORT ID FOR ALL BOOKINGS IN THIS EXCEL
	                    // =====================================================

	                    bookingRequest.setImportId(importId);

	                    // =====================================================
	                    // 5. READ EXCEL COLUMNS
	                    // =====================================================

	                    // -----------------------------------------------------
	                    // 1 = NAME
	                    // -----------------------------------------------------

	                    patientName =
	                            getCellValue(row.getCell(1));

	                    bookingRequest.setName(patientName);


	                    // -----------------------------------------------------
	                    // 2 = AGE
	                    // -----------------------------------------------------

	                    bookingRequest.setAge(
	                            getCellValue(row.getCell(2))
	                    );


	                    // -----------------------------------------------------
	                    // 3 = GENDER
	                    // -----------------------------------------------------

	                    bookingRequest.setGender(
	                            getCellValue(row.getCell(3))
	                    );


	                    // -----------------------------------------------------
	                    // 4 = MOBILE NUMBER
	                    // -----------------------------------------------------

	                    patientMobileNumber =
	                            getCellValue(row.getCell(4));

	                    bookingRequest.setPatientMobileNumber(
	                            patientMobileNumber
	                    );

	                    bookingRequest.setMobileNumber(
	                            patientMobileNumber
	                    );


	                    // -----------------------------------------------------
	                    // 5 = EMAIL
	                    // -----------------------------------------------------

	                    bookingRequest.setEmail(
	                            getCellValue(row.getCell(5))
	                    );


	                    // -----------------------------------------------------
	                    // 6 = PATIENT ID
	                    // -----------------------------------------------------

	                    bookingRequest.setPatientId(
	                            getCellValue(row.getCell(6))
	                    );


	                    // -----------------------------------------------------
	                    // 7 = CUSTOMER ID
	                    // -----------------------------------------------------

	                    bookingRequest.setCustomerId(
	                            getCellValue(row.getCell(7))
	                    );


	                    // -----------------------------------------------------
	                    // 8 = BOOKING FOR
	                    // -----------------------------------------------------

	                    bookingRequest.setBookingFor(
	                            getCellValue(row.getCell(8))
	                    );


	                    // -----------------------------------------------------
	                    // 9 = PATIENT ADDRESS
	                    // -----------------------------------------------------

	                    bookingRequest.setPatientAddress(
	                            getCellValue(row.getCell(9))
	                    );


	                    // -----------------------------------------------------
	                    // 10 = CLINIC ID
	                    // -----------------------------------------------------

	                    bookingRequest.setClinicId(
	                            getCellValue(row.getCell(10))
	                    );


	                    // -----------------------------------------------------
	                    // 11 = CLINIC NAME
	                    // -----------------------------------------------------

	                    bookingRequest.setClinicName(
	                            getCellValue(row.getCell(11))
	                    );


	                    // -----------------------------------------------------
	                    // 12 = BRANCH ID
	                    // -----------------------------------------------------

	                    bookingRequest.setBranchId(
	                            getCellValue(row.getCell(12))
	                    );


	                    // -----------------------------------------------------
	                    // 13 = BRANCH NAME
	                    // -----------------------------------------------------

	                    bookingRequest.setBranchname(
	                            getCellValue(row.getCell(13))
	                    );


	                    // -----------------------------------------------------
	                    // 15 = DOCTOR ID
	                    // -----------------------------------------------------

	                    bookingRequest.setDoctorId(
	                            getCellValue(row.getCell(15))
	                    );


	                    // -----------------------------------------------------
	                    // 16 = DOCTOR NAME
	                    // -----------------------------------------------------

	                    bookingRequest.setDoctorName(
	                            getCellValue(row.getCell(16))
	                    );


	                    // -----------------------------------------------------
	                    // 17 = DOCTOR REF CODE
	                    // -----------------------------------------------------

	                    bookingRequest.setDoctorRefCode(
	                            getCellValue(row.getCell(17))
	                    );


	                    // -----------------------------------------------------
	                    // 18 = SERVICE DATE
	                    // -----------------------------------------------------

	                    bookingRequest.setServiceDate(
	                            getCellValue(row.getCell(18))
	                    );


	                    // -----------------------------------------------------
	                    // 19 = SERVICE TIME
	                    // -----------------------------------------------------

	                    bookingRequest.setServicetime(
	                            getCellValue(row.getCell(19))
	                    );


	                    // -----------------------------------------------------
	                    // 20 = VISIT TYPE
	                    // -----------------------------------------------------

	                    bookingRequest.setVisitType(
	                            getCellValue(row.getCell(20))
	                    );


	                    // -----------------------------------------------------
	                    // 21 = CONSULTATION FEE
	                    // -----------------------------------------------------

	                    String consultationFee =
	                            getCellValue(row.getCell(21));

	                    if (consultationFee != null
	                            && !consultationFee.isBlank()) {

	                        bookingRequest.setConsultationFee(
	                                Double.parseDouble(
	                                        consultationFee
	                                )
	                        );
	                    }


	                    // -----------------------------------------------------
	                    // 22 = CONSULTATION EXPIRATION
	                    // -----------------------------------------------------

	                    bookingRequest.setConsultationExpiration(
	                            getCellValue(row.getCell(22))
	                    );


	                    // -----------------------------------------------------
	                    // 23 = FREE FOLLOW UPS
	                    // -----------------------------------------------------

	                    String freeFollowUps =
	                            getCellValue(row.getCell(23));

	                    if (freeFollowUps != null
	                            && !freeFollowUps.isBlank()) {

	                        bookingRequest.setFreeFollowUps(
	                                Integer.parseInt(
	                                        freeFollowUps
	                                )
	                        );
	                    }


	                    // -----------------------------------------------------
	                    // 24 = FOC
	                    // -----------------------------------------------------

	                    bookingRequest.setFoc(
	                            getCellValue(row.getCell(24))
	                    );


	                    // -----------------------------------------------------
	                    // 25 = FOC REASON
	                    // -----------------------------------------------------

	                    bookingRequest.setFocReason(
	                            getCellValue(row.getCell(25))
	                    );


	                    // -----------------------------------------------------
	                    // 26 = PAYMENT TYPE
	                    // -----------------------------------------------------

	                    bookingRequest.setPaymentType(
	                            getCellValue(row.getCell(26))
	                    );


	                    // -----------------------------------------------------
	                    // 27 = TRANSACTION ID
	                    // -----------------------------------------------------

	                    bookingRequest.setTransactionId(
	                            getCellValue(row.getCell(27))
	                    );


	                    // -----------------------------------------------------
	                    // 28 = REFERRED BY NAME
	                    // -----------------------------------------------------

	                    bookingRequest.setReferredByName(
	                            getCellValue(row.getCell(28))
	                    );


	                    // -----------------------------------------------------
	                    // 29 = REFERRED BY TYPE
	                    // -----------------------------------------------------

	                    bookingRequest.setReferredByType(
	                            getCellValue(row.getCell(29))
	                    );


	                    // =====================================================
	                    // 6. BASIC VALIDATIONS
	                    // =====================================================

	                    if (patientName == null
	                            || patientName.isBlank()) {

	                        failedRows++;

	                        addFailedAppointment(
	                                failedAppointments,
	                                excelRowNumber,
	                                patientName,
	                                patientMobileNumber,
	                                "Patient name is required"
	                        );

	                        continue;
	                    }


	                    if (patientMobileNumber == null
	                            || patientMobileNumber.isBlank()) {

	                        failedRows++;

	                        addFailedAppointment(
	                                failedAppointments,
	                                excelRowNumber,
	                                patientName,
	                                patientMobileNumber,
	                                "Patient mobile number is required"
	                        );

	                        continue;
	                    }


	                    if (bookingRequest.getClinicId() == null
	                            || bookingRequest.getClinicId().isBlank()) {

	                        failedRows++;

	                        addFailedAppointment(
	                                failedAppointments,
	                                excelRowNumber,
	                                patientName,
	                                patientMobileNumber,
	                                "Clinic Id is required"
	                        );

	                        continue;
	                    }


	                    if (bookingRequest.getBranchId() == null
	                            || bookingRequest.getBranchId().isBlank()) {

	                        failedRows++;

	                        addFailedAppointment(
	                                failedAppointments,
	                                excelRowNumber,
	                                patientName,
	                                patientMobileNumber,
	                                "Branch Id is required"
	                        );

	                        continue;
	                    }


	                    // =====================================================
	                    // 7. CHECK WHETHER CUSTOMER EXISTS
	                    // =====================================================

	                    CustomerOnbordingDTO existingCustomer = null;

	                    try {

	                        existingCustomer =
	                                clinicAdminFeign
	                                        .getCustomerByMobileNumberAndClinicId(
	                                                patientMobileNumber,
	                                                bookingRequest.getClinicId()
	                                        );

	                    } catch (Exception e) {

	                        log.info(
	                                "Customer not found. Creating new customer for mobile {}",
	                                patientMobileNumber
	                        );
	                    }


	                    // =====================================================
	                    // 8. CUSTOMER EXISTS
	                    // =====================================================

	                    if (existingCustomer != null) {

	                        log.info(
	                                "Customer already exists. Skipping onboarding for mobile {}",
	                                patientMobileNumber
	                        );


	                        if (existingCustomer.getPatientId() != null
	                                && !existingCustomer.getPatientId().isBlank()) {

	                            bookingRequest.setPatientId(
	                                    existingCustomer.getPatientId()
	                            );
	                        }


	                        if (existingCustomer.getCustomerId() != null
	                                && !existingCustomer.getCustomerId().isBlank()) {

	                            bookingRequest.setCustomerId(
	                                    existingCustomer.getCustomerId()
	                            );
	                        }

	                    }


	                    // =====================================================
	                    // 9. CUSTOMER DOES NOT EXIST
	                    // =====================================================

	                    else {

	                        CustomerOnbordingDTO customerDto =
	                                new CustomerOnbordingDTO();


	                        customerDto.setMobileNumber(
	                                bookingRequest.getPatientMobileNumber()
	                        );

	                        customerDto.setEmail(
	                                bookingRequest.getEmail()
	                        );

	                        customerDto.setFullName(
	                                bookingRequest.getName()
	                        );

	                        customerDto.setGender(
	                                bookingRequest.getGender()
	                        );

	                        customerDto.setDateOfBirth(
	                                bookingRequest.getDob()
	                        );

	                        customerDto.setAge(
	                                bookingRequest.getAge()
	                        );

	                        customerDto.setHospitalId(
	                                bookingRequest.getClinicId()
	                        );

	                        customerDto.setHospitalName(
	                                bookingRequest.getClinicName()
	                        );

	                        customerDto.setBranchId(
	                                bookingRequest.getBranchId()
	                        );


	                        // =================================================
	                        // ONBOARD CUSTOMER
	                        // =================================================

	                        try {

	                            ResponseEntity<Response> customerResponseEntity =
	                                    clinicAdminFeign
	                                            .onboardCustomer(
	                                                    customerDto
	                                            );

	                            Response customerResponse = null;

	                            if (customerResponseEntity != null) {

	                                customerResponse =
	                                        customerResponseEntity.getBody();
	                            }


	                            if (customerResponse == null
	                                    || !customerResponse.isSuccess()) {

	                                failedRows++;

	                                String reason =
	                                        customerResponse != null
	                                                && customerResponse.getMessage() != null
	                                                ? customerResponse.getMessage()
	                                                : "Customer onboarding failed";

	                                addFailedAppointment(
	                                        failedAppointments,
	                                        excelRowNumber,
	                                        patientName,
	                                        patientMobileNumber,
	                                        reason
	                                );

	                                continue;
	                            }

	                        } catch (Exception e) {

	                            String errorMessage = e.getMessage();

	                            // =============================================
	                            // CUSTOMER ALREADY EXISTS
	                            // CONTINUE BOOKING
	                            // =============================================

	                            if (errorMessage != null
	                                    && errorMessage.toLowerCase()
	                                    .contains("mobile number already exists")) {

	                                log.info(
	                                        "Customer already exists for mobile {}. Continuing booking.",
	                                        patientMobileNumber
	                                );

	                            } else {

	                                failedRows++;

	                                addFailedAppointment(
	                                        failedAppointments,
	                                        excelRowNumber,
	                                        patientName,
	                                        patientMobileNumber,
	                                        "Customer onboarding failed: "
	                                                + errorMessage
	                                );

	                                continue;
	                            }
	                        }


	                        // =================================================
	                        // FETCH CUSTOMER AGAIN
	                        // =================================================

	                        try {

	                            CustomerOnbordingDTO newCustomer =
	                                    clinicAdminFeign
	                                            .getCustomerByMobileNumberAndClinicId(
	                                                    patientMobileNumber,
	                                                    bookingRequest.getClinicId()
	                                            );


	                            if (newCustomer != null
	                                    && newCustomer.getPatientId() != null
	                                    && !newCustomer.getPatientId().isBlank()) {

	                                bookingRequest.setPatientId(
	                                        newCustomer.getPatientId()
	                                );
	                            }


	                            if (newCustomer != null
	                                    && newCustomer.getCustomerId() != null
	                                    && !newCustomer.getCustomerId().isBlank()) {

	                                bookingRequest.setCustomerId(
	                                        newCustomer.getCustomerId()
	                                );
	                            }

	                        } catch (Exception e) {

	                            log.warn(
	                                    "Unable to fetch customer details for mobile {}",
	                                    patientMobileNumber
	                            );
	                        }
	                    }


	                    // =====================================================
	                    // 10. CREATE PHYSIO APPOINTMENT
	                    // =====================================================

	                    ResponseEntity<?> bookingResponse =
	                            physioAppointment(
	                                    bookingRequest
	                            );


	                    Response bookingResult = null;

	                    if (bookingResponse != null
	                            && bookingResponse.getBody()
	                            instanceof Response) {

	                        bookingResult =
	                                (Response) bookingResponse.getBody();
	                    }


	                    // =====================================================
	                    // 11. CHECK BOOKING RESULT
	                    // =====================================================

	                    if (bookingResult != null
	                            && bookingResult.isSuccess()
	                            && bookingResult.getStatus() == 200) {

	                        savedRows++;

	                        log.info(
	                                "Appointment booked successfully for Excel row {} with importId {}",
	                                excelRowNumber,
	                                importId
	                        );

	                    } else {

	                        failedRows++;

	                        String reason =
	                                bookingResult != null
	                                        && bookingResult.getMessage() != null
	                                        ? bookingResult.getMessage()
	                                        : "Appointment booking failed";

	                        addFailedAppointment(
	                                failedAppointments,
	                                excelRowNumber,
	                                patientName,
	                                patientMobileNumber,
	                                reason
	                        );
	                    }

	                } catch (Exception e) {

	                    failedRows++;

	                    String errorMessage =
	                            e.getMessage() != null
	                                    ? e.getMessage()
	                                    : "Unexpected error";

	                    addFailedAppointment(
	                            failedAppointments,
	                            excelRowNumber,
	                            patientName,
	                            patientMobileNumber,
	                            errorMessage
	                    );

	                    log.error(
	                            "Excel row {} failed: {}",
	                            excelRowNumber,
	                            e.getMessage(),
	                            e
	                    );
	                }
	            }
	        }


	        // =====================================================
	        // 12. IMPORT SUMMARY
	        // =====================================================

	        Map<String, Object> result =
	                new LinkedHashMap<>();

	        // IMPORTANT:
	        // RETURN IMPORT ID ONLY IF BOOKINGS WERE SAVED

	        if (savedRows > 0) {

	            result.put(
	                    "importId",
	                    importId
	            );
	        }

	        result.put(
	                "totalRows",
	                totalRows
	        );

	        result.put(
	                "saved",
	                savedRows
	        );

	        result.put(
	                "failed",
	                failedRows
	        );

	        result.put(
	                "failedAppointments",
	                failedAppointments
	        );


	        // =====================================================
	        // 13. FINAL RESPONSE
	        // =====================================================

	        response.setSuccess(true);

	        response.setMessage(
	                "Physiotherapy appointment Excel import completed successfully"
	        );

	        response.setData(result);

	        response.setStatus(200);

	    } catch (Exception e) {

	        log.error(
	                "Error importing Excel: {}",
	                e.getMessage(),
	                e
	        );

	        response.setSuccess(false);

	        response.setMessage(
	                "Error importing Excel: " + e.getMessage()
	        );

	        response.setStatus(500);
	    }

	    return response;
	}
	private void addFailedAppointment(
	        List<Map<String, Object>> failedAppointments,
	        int excelRow,
	        String patientName,
	        String patientMobileNumber,
	        String reason) {

	    Map<String, Object> failedAppointment =
	            new LinkedHashMap<>();


	    failedAppointment.put(
	            "excelRow",
	            excelRow
	    );


	    failedAppointment.put(
	            "patientName",
	            patientName
	    );


	    failedAppointment.put(
	            "patientMobileNumber",
	            patientMobileNumber
	    );


	    failedAppointment.put(
	            "reason",
	            reason
	    );


	    failedAppointments.add(
	            failedAppointment
	    );
	}
	private String getCellValue(Cell cell) {

	    if (cell == null) {
	        return "";
	    }

	    DataFormatter formatter =
	            new DataFormatter();

	    return formatter
	            .formatCellValue(cell)
	            .trim();
	}
}
