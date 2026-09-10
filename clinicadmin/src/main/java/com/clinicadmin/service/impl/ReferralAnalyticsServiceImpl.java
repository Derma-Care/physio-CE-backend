package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.BookingResponse;
import com.clinicadmin.dto.ReferralAnalyticsResponse;
import com.clinicadmin.dto.ReferralAnalyticsSummaryResponse;
import com.clinicadmin.dto.ReferredPatientDTO;
import com.clinicadmin.dto.ReferringDoctorLiteDTO;
import com.clinicadmin.dto.ReferringDoctorSummaryDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.Payment;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.repository.PaymentRepository;
import com.clinicadmin.service.ReferralAnalyticsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReferralAnalyticsServiceImpl implements ReferralAnalyticsService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int OPTION_OVERALL = 5;

    @Autowired
    private BookingFeign bookingFeign;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper mapper;

    // ---------- full (with patients) ----------

    @Override
    public Response getReferralAnalytics(String clinicId, String branchId) {
        return getReferralAnalytics(clinicId, branchId, OPTION_OVERALL);
    }

    @Override
    public Response getReferralAnalytics(String clinicId, String branchId, int option) {

        String[] range = resolveDateRange(option);
        if (range == null) {
            Response invalid = new Response();
            invalid.setSuccess(false);
            invalid.setStatus(400);
            invalid.setMessage("Invalid option. Use 1=day, 2=week, 3=month, 4=year, 5=overall");
            return invalid;
        }

        return buildReferralAnalytics(clinicId, branchId, range[0], range[1]);
    }

    @Override
    public Response getReferralAnalyticsByCustomRange(String clinicId, String branchId, String startDate, String endDate) {
        return buildReferralAnalytics(clinicId, branchId, startDate, endDate);
    }

    // ---------- summary (no patients, for dashboard tables) ----------

    @Override
    public Response getReferralAnalyticsSummary(String clinicId, String branchId) {
        return getReferralAnalyticsSummary(clinicId, branchId, OPTION_OVERALL);
    }

    @Override
    public Response getReferralAnalyticsSummary(String clinicId, String branchId, int option) {

        String[] range = resolveDateRange(option);
        if (range == null) {
            Response invalid = new Response();
            invalid.setSuccess(false);
            invalid.setStatus(400);
            invalid.setMessage("Invalid option. Use 1=day, 2=week, 3=month, 4=year, 5=overall");
            return invalid;
        }

        return buildSummary(clinicId, branchId, range[0], range[1]);
    }

    @Override
    public Response getReferralAnalyticsSummaryByCustomRange(String clinicId, String branchId, String startDate, String endDate) {
        return buildSummary(clinicId, branchId, startDate, endDate);
    }

    private Response buildSummary(String clinicId, String branchId, String startDate, String endDate) {

        Response fullResponse = buildReferralAnalytics(clinicId, branchId, startDate, endDate);

        if (!fullResponse.isSuccess() || fullResponse.getData() == null) {
            return fullResponse;
        }

        ReferralAnalyticsResponse full = (ReferralAnalyticsResponse) fullResponse.getData();

        ReferralAnalyticsSummaryResponse summary = new ReferralAnalyticsSummaryResponse();
        summary.setSelfPatientCount(full.getSelfPatientCount());
        summary.setSelfRevenue(full.getSelfRevenue());
        summary.setOtherPatientCount(full.getOtherPatientCount());
        summary.setOtherRevenue(full.getOtherRevenue());
        summary.setTotalReferrals(full.getSelfPatientCount() + full.getOtherPatientCount());

        List<ReferringDoctorLiteDTO> lite = full.getDoctorWise().stream()
                .map(d -> new ReferringDoctorLiteDTO(
                        d.getDoctorRefCode(), d.getReferredByName(), d.getPatientCount(), d.getRevenue()))
                .collect(Collectors.toList());
        summary.setDoctorWise(lite);

        lite.stream()
                .max((a, b) -> {
                    int byCount = a.getPatientCount().compareTo(b.getPatientCount());
                    return byCount != 0 ? byCount : Double.compare(a.getRevenue(), b.getRevenue());
                })
                .ifPresent(top -> {
                    summary.setTopReferringDoctorName(top.getReferredByName());
                    summary.setTopReferringDoctorPatientCount(top.getPatientCount());
                });

        Response response = new Response();
        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("Referral Analytics Summary Retrieved Successfully");
        response.setData(summary);
        return response;
    }

    // ---------- single doctor / self drill-down ----------

    @Override
    public Response getPatientsByReferringDoctor(String clinicId, String branchId, String doctorRefCode) {
        return getPatientsByReferringDoctor(clinicId, branchId, doctorRefCode, OPTION_OVERALL);
    }

    @Override
    public Response getPatientsByReferringDoctor(String clinicId, String branchId, String doctorRefCode, int option) {

        String[] range = resolveDateRange(option);
        if (range == null) {
            Response invalid = new Response();
            invalid.setSuccess(false);
            invalid.setStatus(400);
            invalid.setMessage("Invalid option. Use 1=day, 2=week, 3=month, 4=year, 5=overall");
            return invalid;
        }

        return buildDoctorDrillDown(clinicId, branchId, doctorRefCode, range[0], range[1]);
    }

    @Override
    public Response getPatientsByReferringDoctorCustomRange(String clinicId, String branchId, String doctorRefCode, String startDate, String endDate) {
        return buildDoctorDrillDown(clinicId, branchId, doctorRefCode, startDate, endDate);
    }

    private Response buildDoctorDrillDown(String clinicId, String branchId, String doctorRefCode, String startDate, String endDate) {

        Response fullAnalyticsResponse = buildReferralAnalytics(clinicId, branchId, startDate, endDate);

        if (!fullAnalyticsResponse.isSuccess() || fullAnalyticsResponse.getData() == null) {
            return fullAnalyticsResponse;
        }

        ReferralAnalyticsResponse analytics = (ReferralAnalyticsResponse) fullAnalyticsResponse.getData();

        Response response = new Response();

        if ("self".equalsIgnoreCase(doctorRefCode)) {
            ReferringDoctorSummaryDTO selfAsGroup = new ReferringDoctorSummaryDTO();
            selfAsGroup.setDoctorRefCode("self");
            selfAsGroup.setReferredByName("Self");
            selfAsGroup.setPatientCount(analytics.getSelfPatientCount());
            selfAsGroup.setRevenue(analytics.getSelfRevenue());
            selfAsGroup.setPatients(analytics.getSelfPatients());

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage("Patients retrieved successfully");
            response.setData(selfAsGroup);
            return response;
        }

        ReferringDoctorSummaryDTO doctorSummary = analytics.getDoctorWise().stream()
                .filter(d -> doctorRefCode.equals(d.getDoctorRefCode()))
                .findFirst()
                .orElse(null);

        if (doctorSummary == null) {
            response.setSuccess(false);
            response.setStatus(404);
            response.setMessage("No referrals found for this doctor in the given period");
            return response;
        }

        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("Patients retrieved successfully");
        response.setData(doctorSummary);
        return response;
    }

    // ---------- shared core ----------

    private String[] resolveDateRange(int option) {
        LocalDate today = LocalDate.now();
        switch (option) {
            case 1: return new String[]{ today.format(DATE_FORMAT), today.format(DATE_FORMAT) };
            case 2: return new String[]{ today.minusDays(6).format(DATE_FORMAT), today.format(DATE_FORMAT) };
            case 3: return new String[]{ today.minusDays(29).format(DATE_FORMAT), today.format(DATE_FORMAT) };
            case 4: return new String[]{ today.minusDays(364).format(DATE_FORMAT), today.format(DATE_FORMAT) };
            case 5: return new String[]{ null, null };
            default: return null;
        }
    }

    private Response buildReferralAnalytics(String clinicId, String branchId, String startDate, String endDate) {

        Response response = new Response();

        try {

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> bookingResponse =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(clinicId, branchId);

            if (bookingResponse == null
                    || bookingResponse.getBody() == null
                    || bookingResponse.getBody().getData() == null) {

                response.setSuccess(false);
                response.setStatus(404);
                response.setMessage("Bookings Not Found");
                return response;
            }

            List<BookingResponse> bookings =
                    mapper.convertValue(
                            bookingResponse.getBody().getData(),
                            new TypeReference<List<BookingResponse>>() {});

            if (startDate != null && endDate != null && !startDate.isBlank() && !endDate.isBlank()) {
                bookings = bookings.stream()
                        .filter(b -> isWithinRange(b.getServiceDate(), startDate, endDate))
                        .collect(Collectors.toList());
            }

            List<Payment> payments =
                    paymentRepository.findByClinicIdAndBranchId(clinicId, branchId);

            Map<String, Payment> paymentByBookingId =
                    payments.stream()
                            .collect(Collectors.toMap(
                                    Payment::getBookingId,
                                    Function.identity(),
                                    (a, b) -> a));

            // cache enriched patient-detail lookups so a patient with
            // multiple bookings only triggers one Feign call per request
            Map<String, BookingResponse> enrichmentCache = new HashMap<>();

            ReferralAnalyticsResponse analytics = new ReferralAnalyticsResponse();
            Map<String, ReferringDoctorSummaryDTO> doctorSummaryMap = new LinkedHashMap<>();

            for (BookingResponse booking : bookings) {

                Payment payment = paymentByBookingId.get(booking.getBookingId());

                double cost = (payment != null && payment.getFinalAmount() != null)
                        ? payment.getFinalAmount() : 0.0;
                double paid = (payment != null && payment.getAmountPaying() != null)
                        ? payment.getAmountPaying() : 0.0;
                double due = (payment != null && payment.getRemainingDue() != null)
                        ? payment.getRemainingDue() : 0.0;

                ReferredPatientDTO patientDto = buildPatientDto(booking, cost, paid, due, enrichmentCache);

                if (isSelf(booking)) {

                    analytics.setSelfPatientCount(analytics.getSelfPatientCount() + 1);
                    analytics.setSelfRevenue(analytics.getSelfRevenue() + paid);
                    analytics.getSelfPatients().add(patientDto);
                    continue;
                }

                analytics.setOtherPatientCount(analytics.getOtherPatientCount() + 1);
                analytics.setOtherRevenue(analytics.getOtherRevenue() + paid);

                String referringDoctorId = referralKey(booking);

                ReferringDoctorSummaryDTO doctorSummary =
                        doctorSummaryMap.computeIfAbsent(referringDoctorId, id -> {
                            ReferringDoctorSummaryDTO dto = new ReferringDoctorSummaryDTO();
                            dto.setDoctorRefCode(id);
                            dto.setReferredByName(booking.getReferredByName());
                            return dto;
                        });

                doctorSummary.setPatientCount(doctorSummary.getPatientCount() + 1);
                doctorSummary.setRevenue(doctorSummary.getRevenue() + paid);
                doctorSummary.getPatients().add(patientDto);
            }
            analytics.setDoctorWise(new ArrayList<>(doctorSummaryMap.values()));

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage("Referral Analytics Retrieved Successfully");
            response.setData(analytics);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    /**
     * Builds a patient row, enriching mobile/package details via
     * BookingFeign#getBookingByPatientId when the base booking is
     * missing them. Falls back to the base booking's own fields
     * (or nulls) if enrichment fails or finds no match.
     */
    private ReferredPatientDTO buildPatientDto(BookingResponse booking, double cost, double paid, double due,
                                                Map<String, BookingResponse> enrichmentCache) {

        BookingResponse enriched = enrichPatientDetails(booking.getPatientId(), booking.getBookingId(), enrichmentCache);

        ReferredPatientDTO dto = new ReferredPatientDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setPatientId(booking.getPatientId());
        dto.setPatientName(booking.getName());
        dto.setDoctorName(booking.getDoctorName());
        dto.setCost(cost);
        dto.setPaidAmount(paid);
        dto.setDueAmount(due);

        String mobile = resolveMobileNumber(booking);
        String packageType = booking.getPackageType();
        String packageName = booking.getPackageName();

        if (enriched != null) {
            if (mobile == null || mobile.isBlank()) {
                mobile = resolveMobileNumber(enriched);
            }
            if (packageType == null || packageType.isBlank()) {
                packageType = enriched.getPackageType();
            }
            if (packageName == null || packageName.isBlank()) {
                packageName = enriched.getPackageName();
            }
        }

        dto.setPatientMobileNumber(mobile);
        dto.setPackageType(packageType);
        dto.setPackageName(packageName);

        return dto;
    }

    /**
     * Calls BookingFeign#getBookingByPatientId to fetch the richer booking
     * record for this patient, matched to the current bookingId. Cached per
     * patientId within a single request. Returns null on any failure or
     * no-match — callers must treat that as "no enrichment available".
     *
     * ASSUMPTION: getBookingByPatientId returns a body shaped like
     * { success, data: [...BookingResponse-like objects...], message, status }.
     * If the real shape differs, this safely falls back to the original
     * booking's own fields rather than throwing.
     */
    @SuppressWarnings("unchecked")
    private BookingResponse enrichPatientDetails(String patientId, String bookingId,
                                                  Map<String, BookingResponse> enrichmentCache) {

        if (patientId == null || patientId.isBlank()) {
            return null;
        }

        if (enrichmentCache.containsKey(patientId)) {
            return enrichmentCache.get(patientId);
        }

        try {
            ResponseEntity<?> raw = bookingFeign.getBookingByPatientId(patientId);

            if (raw == null || raw.getBody() == null) {
                enrichmentCache.put(patientId, null);
                return null;
            }

            Map<String, Object> body = mapper.convertValue(raw.getBody(), new TypeReference<Map<String, Object>>() {});
            Object dataNode = body.get("data");

            if (dataNode == null) {
                enrichmentCache.put(patientId, null);
                return null;
            }

            List<BookingResponse> candidates;
            if (dataNode instanceof List) {
                candidates = mapper.convertValue(dataNode, new TypeReference<List<BookingResponse>>() {});
            } else {
                BookingResponse single = mapper.convertValue(dataNode, BookingResponse.class);
                candidates = List.of(single);
            }

            BookingResponse match = candidates.stream()
                    .filter(b -> bookingId != null && bookingId.equals(b.getBookingId()))
                    .findFirst()
                    .orElse(candidates.isEmpty() ? null : candidates.get(0));

            enrichmentCache.put(patientId, match);
            return match;

        } catch (Exception e) {
            e.printStackTrace();
            enrichmentCache.put(patientId, null);
            return null;
        }
    }

    private String resolveMobileNumber(BookingResponse booking) {
        String primary = booking.getPatientMobileNumber();
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return booking.getMobileNumber();
    }

    private boolean isWithinRange(String serviceDate, String startDate, String endDate) {
        if (serviceDate == null || serviceDate.isBlank()) {
            return false;
        }
        return serviceDate.compareTo(startDate) >= 0 && serviceDate.compareTo(endDate) <= 0;
    }

    private boolean isSelf(BookingResponse booking) {
        String refCode = booking.getDoctorRefCode();
        return refCode == null || refCode.isBlank() || refCode.equalsIgnoreCase("self");
    }

    private String referralKey(BookingResponse booking) {
        return booking.getDoctorRefCode();
    }
}