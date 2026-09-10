package com.clinicadmin.service.impl;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PackageAnalyticsDTO;
import com.clinicadmin.dto.PackageDashboardPeriodDTO;
import com.clinicadmin.dto.PackageDashboardTotalsDTO;
import com.clinicadmin.dto.PackagePatientDetailDTO;
import com.clinicadmin.dto.PackageSittingAnalyticsResponseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.Sittings;
import com.clinicadmin.dto.SittingStatusCountsDTO;
import com.clinicadmin.entity.TreatmentSchedule;
import com.clinicadmin.repository.TreatmentScheduleRepository;
import com.clinicadmin.service.PackageAnalyticsService;

@Service
public class PackageAnalyticsServiceImpl implements PackageAnalyticsService {

    @Autowired
    private TreatmentScheduleRepository treatmentScheduleRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter SITTING_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==========================================================
    // PUBLIC API #1 - predefined filters (Today / Week / Month / Year / Overall)
    // ==========================================================

    @Override
    public Response getPackageAnalytics(String clinicId, String branchId, Integer filterType) {

        Response validation = validateClinicAndBranch(clinicId, branchId);
        if (validation != null) {
            return validation;
        }

        LocalDate today = LocalDate.now(ZONE);
        int type = filterType == null ? 5 : filterType;

        LocalDate rangeStart;
        LocalDate rangeEnd;
        String filterLabel;
        boolean overall = false;

        switch (type) {

            case 1: // Today
                rangeStart = today;
                rangeEnd = today;
                filterLabel = "Today";
                break;

            case 2: // This Week (Monday - Sunday, current week up to today)
                rangeStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                rangeEnd = today;
                filterLabel = "This Week";
                break;

            case 3: // This Month
                rangeStart = today.withDayOfMonth(1);
                rangeEnd = today;
                filterLabel = "This Month";
                break;

            case 4: // This Year
                rangeStart = today.withDayOfYear(1);
                rangeEnd = today;
                filterLabel = "This Year";
                break;

            case 5: // Overall
            default:
                rangeStart = null;
                rangeEnd = null;
                overall = true;
                filterLabel = "Overall";
                break;
        }

        try {
            return buildAnalytics(clinicId, branchId, rangeStart, rangeEnd, overall, filterLabel);
        } catch (Exception e) {
            return errorResponse("Failed to retrieve package analytics: " + e.getMessage());
        }
    }

    // ==========================================================
    // PUBLIC API #2 - dedicated custom date range
    // ==========================================================

    @Override
    public Response getPackageAnalyticsCustomRange(String clinicId, String branchId, String startDate, String endDate) {

        Response validation = validateClinicAndBranch(clinicId, branchId);
        if (validation != null) {
            return validation;
        }

        if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
            return badRequest("startDate and endDate are both required (format yyyy-MM-dd)");
        }

        LocalDate rangeStart;
        LocalDate rangeEnd;

        try {
            rangeStart = LocalDate.parse(startDate.trim());
            rangeEnd = LocalDate.parse(endDate.trim());
        } catch (Exception e) {
            return badRequest("startDate/endDate must be in yyyy-MM-dd format");
        }

        if (rangeEnd.isBefore(rangeStart)) {
            return badRequest("endDate cannot be before startDate");
        }

        String filterLabel = "Custom (" + rangeStart + " to " + rangeEnd + ")";

        try {
            return buildAnalytics(clinicId, branchId, rangeStart, rangeEnd, false, filterLabel);
        } catch (Exception e) {
            return errorResponse("Failed to retrieve package analytics: " + e.getMessage());
        }
    }

    // ==========================================================
    // PUBLIC API #3 - dashboard quick totals (Today/Week/Month/Year/Overall at once)
    // ==========================================================

    @Override
    public Response getPackageDashboardTotals(String clinicId, String branchId) {

        Response validation = validateClinicAndBranch(clinicId, branchId);
        if (validation != null) {
            return validation;
        }

        try {

            LocalDate today = LocalDate.now(ZONE);
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate yearStart = today.withDayOfYear(1);

            List<TreatmentSchedule> allSchedules =
                    treatmentScheduleRepository.findAllByClinicIdAndBranchId(clinicId, branchId);

            PackageDashboardPeriodDTO todayPeriod = computePeriod(allSchedules, today, today);
            PackageDashboardPeriodDTO weekPeriod = computePeriod(allSchedules, weekStart, today);
            PackageDashboardPeriodDTO monthPeriod = computePeriod(allSchedules, monthStart, today);
            PackageDashboardPeriodDTO yearPeriod = computePeriod(allSchedules, yearStart, today);
            PackageDashboardPeriodDTO overallPeriod = computePeriod(allSchedules, null, null);

            PackageDashboardTotalsDTO dto = new PackageDashboardTotalsDTO(
                    todayPeriod, weekPeriod, monthPeriod, yearPeriod, overallPeriod);

            Response response = new Response();
            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage("Package dashboard totals retrieved successfully");
            response.setData(dto);
            return response;

        } catch (Exception e) {
            return errorResponse("Failed to retrieve package dashboard totals: " + e.getMessage());
        }
    }

    private PackageDashboardPeriodDTO computePeriod(List<TreatmentSchedule> allSchedules,
                                                      LocalDate rangeStart, LocalDate rangeEnd) {

        boolean overall = rangeStart == null || rangeEnd == null;

        Set<String> distinctPatients = new HashSet<>();
        Set<String> singleSittingPatients = new HashSet<>();
        Set<String> packagePatients = new HashSet<>();
        int bookings = 0;

        for (TreatmentSchedule schedule : allSchedules) {

            if (!overall && !isWithinRange(schedule.getCreatedAt(), rangeStart, rangeEnd)) {
                continue;
            }

            bookings++;

            if (schedule.getPatientId() != null) {
                distinctPatients.add(schedule.getPatientId());

                boolean isSingleSitting = schedule.getSittings() != null && schedule.getSittings().size() == 1;
                if (isSingleSitting) {
                    singleSittingPatients.add(schedule.getPatientId());
                } else {
                    packagePatients.add(schedule.getPatientId());
                }
            }
        }

        return new PackageDashboardPeriodDTO(
                bookings, distinctPatients.size(), singleSittingPatients.size(), packagePatients.size());
    }

    // ==========================================================
    // Shared core - both filtered-analytics public methods funnel into this
    // once their date range (or "overall") has been resolved and validated.
    // ==========================================================

    private Response buildAnalytics(String clinicId,
                                     String branchId,
                                     LocalDate rangeStart,
                                     LocalDate rangeEnd,
                                     boolean overall,
                                     String filterLabel) {

        Response response = new Response();

        List<TreatmentSchedule> allSchedulesRaw =
                treatmentScheduleRepository.findAllByClinicIdAndBranchId(clinicId, branchId);

        List<TreatmentSchedule> allSchedules = allSchedulesRaw.stream()
                .filter(s -> overall || isWithinRange(s.getCreatedAt(), rangeStart, rangeEnd))
                .toList();

        if (allSchedules.isEmpty()) {
            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage("No treatment schedules found for the selected period");
            response.setData(emptyResponse(filterLabel, overall, rangeStart, rangeEnd));
            return response;
        }

        // ==========================
        // Group schedules by packageId (fallback to packageName, then "UNKNOWN")
        // ==========================

        Map<String, List<TreatmentSchedule>> groupedByPackage = new LinkedHashMap<>();

        for (TreatmentSchedule schedule : allSchedules) {
            String key = resolveGroupKey(schedule);
            groupedByPackage.computeIfAbsent(key, k -> new ArrayList<>()).add(schedule);
        }

        List<PackageAnalyticsDTO> packageList = new ArrayList<>();

        // Running totals for the overall summary
        int overallTotal = 0, overallCompleted = 0, overallInProgress = 0,
                overallNotStarted = 0, overallCancelled = 0, overallRescheduled = 0;

        Set<String> allDistinctPatients = new HashSet<>();
        Set<String> allSingleSittingPatients = new HashSet<>();
        Set<String> allPackagePatients = new HashSet<>();
        int overallBookings = 0;

        for (Map.Entry<String, List<TreatmentSchedule>> entry : groupedByPackage.entrySet()) {

            List<TreatmentSchedule> schedulesInGroup = entry.getValue();

            TreatmentSchedule sample = schedulesInGroup.get(0);

            List<PackagePatientDetailDTO> patientDetails = new ArrayList<>();

            int groupTotal = 0, groupCompleted = 0, groupInProgress = 0,
                    groupNotStarted = 0, groupCancelled = 0, groupRescheduled = 0;

            Set<String> distinctPatientsInGroup = new HashSet<>();

            boolean allSingleSitting = true;

            for (TreatmentSchedule schedule : schedulesInGroup) {

                boolean isSingleSitting = schedule.getSittings() != null && schedule.getSittings().size() == 1;

                if (schedule.getPatientId() != null) {
                    distinctPatientsInGroup.add(schedule.getPatientId());
                    allDistinctPatients.add(schedule.getPatientId());

                    if (isSingleSitting) {
                        allSingleSittingPatients.add(schedule.getPatientId());
                    } else {
                        allPackagePatients.add(schedule.getPatientId());
                    }
                }

                if (!isSingleSitting) {
                    allSingleSitting = false;
                }

                List<Sittings> sittings = schedule.getSittings();
                int total = 0, completed = 0, inProgress = 0, notStarted = 0, cancelled = 0, rescheduled = 0;
                String lastDateStr = null;
                LocalDate lastDateParsed = null;

                if (sittings != null) {
                    for (Sittings s : sittings) {
                        total++;

                        String bucket = classifyStatus(s.getTreatmentStatus());
                        switch (bucket) {
                            case "Completed" -> completed++;
                            case "In-progress" -> inProgress++;
                            case "Cancelled" -> cancelled++;
                            case "Rescheduled" -> rescheduled++;
                            default -> notStarted++; // "Not Started" and anything unrecognized
                        }

                        LocalDate parsed = parseSittingDate(s.getDate());
                        if (parsed != null && (lastDateParsed == null || parsed.isAfter(lastDateParsed))) {
                            lastDateParsed = parsed;
                            lastDateStr = s.getDate();
                        }
                    }
                }

                int remaining = notStarted + inProgress + rescheduled;

                SittingStatusCountsDTO patientCounts = new SittingStatusCountsDTO(
                        total, completed, inProgress, notStarted, cancelled, rescheduled, remaining);

                PackagePatientDetailDTO patientDetail = new PackagePatientDetailDTO(
                        schedule.getId(),
                        schedule.getPatientId(),
                        schedule.getPatientName(),
                        schedule.getMobileNumber(),
                        schedule.getBookingId(),
                        schedule.getDoctorName(),
                        patientCounts,
                        lastDateStr);

                patientDetails.add(patientDetail);

                groupTotal += total;
                groupCompleted += completed;
                groupInProgress += inProgress;
                groupNotStarted += notStarted;
                groupCancelled += cancelled;
                groupRescheduled += rescheduled;
            }

            // Sort patients within the group by most recent activity first
            patientDetails.sort(Comparator.comparing(
                    PackagePatientDetailDTO::getLastSittingDate,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            int groupRemaining = groupNotStarted + groupInProgress + groupRescheduled;

            SittingStatusCountsDTO groupCounts = new SittingStatusCountsDTO(
                    groupTotal, groupCompleted, groupInProgress, groupNotStarted, groupCancelled,
                    groupRescheduled, groupRemaining);

            PackageAnalyticsDTO packageDto = new PackageAnalyticsDTO(
                    sample.getPackageId(),
                    sample.getPackageName(),
                    sample.getPackageType(),
                    allSingleSitting,
                    distinctPatientsInGroup.size(),
                    schedulesInGroup.size(),
                    groupCounts,
                    patientDetails);

            packageList.add(packageDto);

            overallTotal += groupTotal;
            overallCompleted += groupCompleted;
            overallInProgress += groupInProgress;
            overallNotStarted += groupNotStarted;
            overallCancelled += groupCancelled;
            overallRescheduled += groupRescheduled;
            overallBookings += schedulesInGroup.size();
        }

        // Sort packages by number of patients, most popular first
        packageList.sort(Comparator.comparing(PackageAnalyticsDTO::getTotalPatients).reversed());

        int overallRemaining = overallNotStarted + overallInProgress + overallRescheduled;

        SittingStatusCountsDTO overallCounts = new SittingStatusCountsDTO(
                overallTotal, overallCompleted, overallInProgress, overallNotStarted, overallCancelled,
                overallRescheduled, overallRemaining);

        PackageSittingAnalyticsResponseDTO result = new PackageSittingAnalyticsResponseDTO();
        result.setFilterApplied(filterLabel);
        result.setRangeStart(overall ? null : rangeStart.format(DAY_FMT));
        result.setRangeEnd(overall ? null : rangeEnd.format(DAY_FMT));
        result.setTotalPackagesAndSittingTypes(packageList.size());
        result.setTotalPatientsOverall(allDistinctPatients.size());
        result.setTotalBookingsOverall(overallBookings);
        result.setTotalSingleSittingPatients(allSingleSittingPatients.size());
        result.setTotalPackagePatients(allPackagePatients.size());
        result.setOverallStatusCounts(overallCounts);
        result.setPackages(packageList);

        response.setSuccess(true);
        response.setStatus(200);
        response.setMessage("Package & sitting analytics retrieved successfully");
        response.setData(result);

        return response;
    }

    // ==========================
    // Helpers
    // ==========================

    private Response validateClinicAndBranch(String clinicId, String branchId) {
        if (clinicId == null || clinicId.isBlank() || branchId == null || branchId.isBlank()) {
            return badRequest("clinicId and branchId are required");
        }
        return null;
    }

    private Response badRequest(String message) {
        Response response = new Response();
        response.setSuccess(false);
        response.setStatus(400);
        response.setMessage(message);
        return response;
    }

    private Response errorResponse(String message) {
        Response response = new Response();
        response.setSuccess(false);
        response.setStatus(500);
        response.setMessage(message);
        return response;
    }

    private boolean isWithinRange(Instant createdAt, LocalDate start, LocalDate end) {
        if (createdAt == null || start == null || end == null) {
            return false;
        }
        LocalDate createdDate = createdAt.atZone(ZONE).toLocalDate();
        return !createdDate.isBefore(start) && !createdDate.isAfter(end);
    }

    private String resolveGroupKey(TreatmentSchedule schedule) {
        if (schedule.getPackageId() != null && !schedule.getPackageId().isBlank()) {
            return schedule.getPackageId();
        }
        if (schedule.getPackageName() != null && !schedule.getPackageName().isBlank()) {
            return "NAME:" + schedule.getPackageName();
        }
        return "UNKNOWN";
    }

    /**
     * treatmentStatus is a free-text field (set directly via the completeSitting
     * endpoint), so we classify defensively by keyword rather than exact match.
     * Anything unrecognized (including null/blank) falls back to "Not Started".
     */
    private String classifyStatus(String rawStatus) {

        if (rawStatus == null || rawStatus.isBlank()) {
            return "Not Started";
        }

        String s = rawStatus.trim().toLowerCase(Locale.ROOT);

        if (s.contains("complet")) {
            return "Completed";
        }
        if (s.contains("cancel")) {
            return "Cancelled";
        }
        if (s.contains("resched")) {
            return "Rescheduled";
        }
        if (s.contains("progress")) {
            return "In-progress";
        }
        return "Not Started";
    }

    private LocalDate parseSittingDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), SITTING_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private PackageSittingAnalyticsResponseDTO emptyResponse(String filterLabel, boolean overall,
                                                               LocalDate rangeStart, LocalDate rangeEnd) {
        SittingStatusCountsDTO emptyCounts = new SittingStatusCountsDTO(0, 0, 0, 0, 0, 0, 0);
        PackageSittingAnalyticsResponseDTO dto = new PackageSittingAnalyticsResponseDTO();
        dto.setFilterApplied(filterLabel);
        dto.setRangeStart(overall ? null : rangeStart.format(DAY_FMT));
        dto.setRangeEnd(overall ? null : rangeEnd.format(DAY_FMT));
        dto.setTotalPackagesAndSittingTypes(0);
        dto.setTotalPatientsOverall(0);
        dto.setTotalBookingsOverall(0);
        dto.setTotalSingleSittingPatients(0);
        dto.setTotalPackagePatients(0);
        dto.setOverallStatusCounts(emptyCounts);
        dto.setPackages(new ArrayList<>());
        return dto;
    }
}