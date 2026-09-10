package com.clinicadmin.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.AgeGroupAnalytics;
import com.clinicadmin.dto.PatientAnalyticsRequest;
import com.clinicadmin.dto.PatientAnalyticsResponse;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.Summary;
import com.clinicadmin.dto.TrendData;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.service.PatientAnalyticsService;

@Service
public class PatientAnalyticsServiceImpl implements PatientAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(PatientAnalyticsServiceImpl.class);

    private static final int TODAY = 1, WEEK = 2, MONTH = 3, YEAR = 4, CUSTOM = 5;

    // Current createdAt format, written by CustomerOnboardingServiceImpl.getIndianDateTime()
    // e.g. "14/07/2026 02:42:51 PM"
    private static final DateTimeFormatter CREATED_AT_FORMAT =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd/MM/yyyy hh:mm:ss a")
                    .toFormatter(Locale.ENGLISH);

    // Legacy createdAt format used before the storage format was switched over
    // to CREATED_AT_FORMAT above, e.g. "2026-06-01T06:16:04.297517422".
    // Older documents in Mongo still carry this ISO format, so we must be able
    // to parse both — otherwise those records parse to null and silently drop
    // out of newPatients / newPatientsTrend / ageGroupAnalytics while still
    // being counted in totalPatients (which is size()-based, not date-based).
    private static final DateTimeFormatter LEGACY_ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Matches clinic hours format like "07:00 AM" / "10:00 PM"
    private static final DateTimeFormatter CLINIC_TIME_FORMAT =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("hh:mm a")
                    .toFormatter(Locale.ENGLISH);

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    // Label format for CUSTOM range trend buckets, e.g. "Jul 07", "Aug 02"
    // 3-letter month abbreviation + zero-padded day, so multi-month custom
    // ranges (e.g. Jun 28 -> Aug 03) read cleanly without repeating the year.
    private static final DateTimeFormatter CUSTOM_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH);

    // Fallback window used only if the branch lookup fails or hours are missing
    private static final int DEFAULT_OPEN_HOUR = 8;
    private static final int DEFAULT_CLOSE_HOUR = 20;
    private static final int SLOT_INTERVAL_HOURS = 2;

    @Autowired
    private CustomerOnboardingRepository onboardingRepository;

    @Autowired
    private AdminServiceClient adminServiceClient;

    @Override
    public Response getPatientAnalytics(String clinicId, String branchId, int filterType,
                                         PatientAnalyticsRequest request) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime start;
        LocalDateTime end = now;

        switch (filterType) {
            case TODAY:
                start = now.toLocalDate().atStartOfDay();
                break;
            case WEEK: {
                // Calendar week: Monday -> Sunday of the current week (not a rolling 7-day window)
                LocalDate weekStart = now.toLocalDate()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(6); // Sunday
                start = weekStart.atStartOfDay();
                end = weekEnd.atTime(23, 59, 59);
                break;
            }
            case MONTH:
                start = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
            case YEAR:
                start = now.toLocalDate().withDayOfYear(1).atStartOfDay();
                break;
            default:
                Response err = new Response();
                err.setSuccess(false);
                err.setMessage("Invalid filterType. Use 1=Today, 2=Week, 3=Month, 4=Year, 5=Custom");
                err.setStatus(400);
                return err;
        }

        String search = request != null ? request.getSearch() : null;
        return buildAnalytics(clinicId, branchId, start, end, filterType, search);
    }

    @Override
    public Response getCustomPatientAnalytics(String clinicId, String branchId, PatientAnalyticsRequest request) {
        if (request == null || request.getStartDate() == null || request.getEndDate() == null) {
            Response err = new Response();
            err.setSuccess(false);
            err.setMessage("startDate and endDate are required for custom filter");
            err.setStatus(400);
            return err;
        }
        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);
        return buildAnalytics(clinicId, branchId, start, end, CUSTOM, request.getSearch());
    }

    // ---------------- core builder ----------------
    private Response buildAnalytics(String clinicId, String branchId,
                                     LocalDateTime start, LocalDateTime end,
                                     int filterType, String search) {
        Response response = new Response();
        try {
            List<CustomerOnbording> allBranchCustomers =
                    onboardingRepository.findByHospitalIdAndBranchId(clinicId, branchId);

            if (search != null && !search.isBlank()) {
                allBranchCustomers = allBranchCustomers.stream()
                        .filter(c -> matchesAgeGroup(c, search))
                        .collect(Collectors.toList());
            }

            // Previous-period boundaries used for growth % comparisons (both the
            // top-level summary.growthRate and each ageGroupAnalytics[].growthTrend).
            //
            // Each filterType compares against its own natural predecessor:
            //   TODAY  -> yesterday (full day)
            //   WEEK   -> last calendar week (Mon-Sun)
            //   MONTH  -> the full previous calendar month
            //   YEAR   -> the full previous calendar year
            //   CUSTOM -> the immediately preceding period of the same length
            //
            // We deliberately do NOT do a generic "shift back by N elapsed days"
            // here: for MONTH/YEAR, `end` is `now` (today), so the elapsed-day
            // count is only a partial slice of the period. Shifting by that count
            // would land prevStart somewhere in the middle of the previous
            // month/year instead of at its actual start, corrupting the
            // comparison (e.g. on Jul 14, "previous month" would become part of
            // June instead of the whole of June).
            LocalDateTime prevStart;
            LocalDateTime prevEnd = start.minusSeconds(1);
            switch (filterType) {
                case TODAY:
                    prevStart = start.minusDays(1);
                    break;
                case WEEK:
                    prevStart = start.minusWeeks(1);
                    break;
                case MONTH:
                    prevStart = start.minusMonths(1);
                    break;
                case YEAR:
                    prevStart = start.minusYears(1);
                    break;
                default: { // CUSTOM
                    long periodDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
                    prevStart = start.minusDays(periodDays);
                    break;
                }
            }

            List<CustomerOnbording> newInPeriod = filterByCreatedAt(allBranchCustomers, start, end);
            List<CustomerOnbording> newInPrevPeriod = filterByCreatedAt(allBranchCustomers, prevStart, prevEnd);

            // NOTE: no "last visit"/appointment field exists on CustomerOnbording yet,
            // so "active" is approximated as customers who have a deviceId (i.e. have
            // logged into the app at least once). Swap this for real visit/booking
            // data if/when that's available.
            long activePatients = allBranchCustomers.stream()
                    .filter(c -> c.getDeviceId() != null && !c.getDeviceId().isBlank())
                    .count();

            // NOTE: totalPatients / activePatients are branch-wide counts (all-time),
            // independent of the start/end filter window. newPatients / newPatientsTrend
            // are the only summary fields scoped to the selected date range. This is
            // intentional: "you have N patients total, M of them joined in this specific range."
            Summary summary = new Summary();
            summary.setTotalPatients(allBranchCustomers.size());
            summary.setNewPatients(newInPeriod.size());
            summary.setActivePatients((int) activePatients);
            summary.setGrowthRate(growthRate(newInPeriod.size(), newInPrevPeriod.size()));

            List<TrendData> trend = buildTrend(newInPeriod, start, end, filterType, clinicId, branchId);

            // ageGroupAnalytics is scoped to the selected date range: male/female/total
            // reflect only patients who newly registered within [start, end], not the
            // branch's all-time demographic mix.
            List<AgeGroupAnalytics> ageGroups = buildAgeGroupAnalytics(newInPeriod, newInPrevPeriod);

            PatientAnalyticsResponse data = new PatientAnalyticsResponse();
            data.setSummary(summary);
            data.setNewPatientsTrend(trend);
            data.setAgeGroupAnalytics(ageGroups);

            response.setSuccess(true);
            response.setMessage("Patient analytics fetched successfully");
            response.setData(data);
            response.setStatus(200);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error fetching patient analytics: " + e.getMessage());
            response.setStatus(500);
        }
        return response;
    }

    // ---------------- helpers ----------------

    private List<CustomerOnbording> filterByCreatedAt(List<CustomerOnbording> customers,
                                                        LocalDateTime start, LocalDateTime end) {
        return customers.stream()
                .filter(c -> {
                    LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                    return createdAt != null && !createdAt.isBefore(start) && !createdAt.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    /**
     * Parses the raw createdAt string into a LocalDateTime, supporting BOTH
     * formats that exist in the data:
     *
     *   1) Current format  -> "dd/MM/yyyy hh:mm:ss a"        e.g. "14/07/2026 02:42:51 PM"
     *   2) Legacy format   -> ISO_LOCAL_DATE_TIME             e.g. "2026-06-01T06:16:04.297517422"
     *
     * Background: CustomerOnboardingServiceImpl originally stored createdAt via
     * LocalDateTime.now().toString() (ISO format), then was changed to
     * getIndianDateTime() (dd/MM/yyyy hh:mm:ss a). Older Mongo documents were
     * never migrated, so both formats coexist in the collection today. Without
     * this fallback, every legacy-format record fails to parse, returns null,
     * and is silently dropped from newPatients / newPatientsTrend /
     * ageGroupAnalytics — while still being counted in totalPatients (which is
     * a plain size(), not date-filtered). That mismatch is exactly what made
     * "220 total patients" but only 1-8 "new patients" show up across every
     * filter range.
     *
     * Also normalizes whitespace before parsing: some upstream sources (Mongo
     * exports, copy/paste, certain drivers/locales) emit a non-breaking space
     * (U+00A0) or repeated spaces between the time and the am/pm marker
     * instead of a single regular ASCII space — e.g. "11:44:17\u00A0am".
     * DateTimeFormatter matches the literal space in the pattern exactly, so
     * that single invisible character would otherwise cause parsing to throw.
     */
    private LocalDateTime parseCreatedAt(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) return null;

        String normalized = createdAt
                .replace('\u00A0', ' ')   // non-breaking space -> regular space
                .replaceAll("\\s+", " ")  // collapse repeated/mixed whitespace
                .trim();

        // Try current format first (dd/MM/yyyy hh:mm:ss a)
        try {
            return LocalDateTime.parse(normalized, CREATED_AT_FORMAT);
        } catch (Exception primaryEx) {
            // Fall back to legacy ISO format (e.g. "2026-06-01T06:16:04.297517422")
            try {
                return LocalDateTime.parse(normalized, LEGACY_ISO_FORMAT);
            } catch (Exception legacyEx) {
                // Don't swallow silently — a record with an unparseable createdAt
                // is still counted in totalPatients but quietly dropped from
                // newPatients/trend/ageGroupAnalytics, which looks like a bug from
                // the outside. Log it so mismatched formats are visible.
                log.warn("Could not parse createdAt='{}' with either 'dd/MM/yyyy hh:mm:ss a' or ISO_LOCAL_DATE_TIME",
                        createdAt, legacyEx);
                return null;
            }
        }
    }

    private double growthRate(int current, int previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round(((current - previous) * 100.0 / previous) * 10.0) / 10.0;
    }

    private List<TrendData> buildTrend(List<CustomerOnbording> newInPeriod,
            LocalDateTime start, LocalDateTime end, int filterType,
            String clinicId, String branchId) {
        Map<String, Integer> buckets = new LinkedHashMap<>();

        if (filterType == TODAY) {
            int[] hourSlots = resolveHourSlots(clinicId, branchId);
            String[] labels = toLabels(hourSlots);
            for (String h : labels) buckets.put(h, 0);
            for (CustomerOnbording c : newInPeriod) {
                LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                if (createdAt == null) continue;
                String bucket = nearestHourBucket(createdAt.getHour(), hourSlots, labels);
                buckets.merge(bucket, 1, Integer::sum);
            }
        } else if (filterType == WEEK) {
            for (LocalDate d = start.toLocalDate(); !d.isAfter(end.toLocalDate()); d = d.plusDays(1)) {
                buckets.put(d.getDayOfWeek().toString().substring(0, 3), 0);
            }
            for (CustomerOnbording c : newInPeriod) {
                LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                if (createdAt == null) continue;
                String bucket = createdAt.getDayOfWeek().toString().substring(0, 3);
                buckets.merge(bucket, 1, Integer::sum);
            }
        } else if (filterType == YEAR) {
            for (int m = 1; m <= 12; m++) {
                String label = LocalDate.of(2000, m, 1).getMonth().toString().substring(0, 3);
                buckets.put(label, 0);
            }
            for (CustomerOnbording c : newInPeriod) {
                LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                if (createdAt == null) continue;
                String label = createdAt.getMonth().toString().substring(0, 3);
                buckets.merge(label, 1, Integer::sum);
            }
        } else if (filterType == MONTH) {
            // "Week 1".."Week N" buckets, matching the mockup — Week N = days
            // (N-1)*7+1 .. N*7 of the calendar month.
            //
            // IMPORTANT: bucket labels must cover the ENTIRE calendar month
            // (1st -> last day), not just start..end. When filterType=MONTH,
            // `end` is `now` (today), so if we looped start..end here, any
            // week after "today" would never get a bucket created at all
            // (not even a 0) instead of showing as an upcoming/empty week.
            // e.g. on Jul 14, this previously produced only Week 1 + Week 2,
            // silently dropping Week 3/4/5.
            //
            // The actual counts below are still correctly bounded by
            // newInPeriod (which only contains records up to `end`), so
            // future weeks in the current month will correctly show 0.
            LocalDate monthStart = start.toLocalDate().withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            for (LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
                buckets.putIfAbsent(weekBucketLabel(d), 0);
            }
            for (CustomerOnbording c : newInPeriod) {
                LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                if (createdAt == null) continue;
                buckets.merge(weekBucketLabel(createdAt.toLocalDate()), 1, Integer::sum);
            }
        } else { // CUSTOM -> daily buckets, labeled e.g. "Jul 07"
            for (LocalDate d = start.toLocalDate(); !d.isAfter(end.toLocalDate()); d = d.plusDays(1)) {
                buckets.put(d.format(CUSTOM_LABEL_FORMAT), 0);
            }
            for (CustomerOnbording c : newInPeriod) {
                LocalDateTime createdAt = parseCreatedAt(c.getCreatedAt());
                if (createdAt == null) continue;
                buckets.merge(createdAt.toLocalDate().format(CUSTOM_LABEL_FORMAT), 1, Integer::sum);
            }
        }

        List<TrendData> trend = new ArrayList<>();
        for (Map.Entry<String, Integer> e : buckets.entrySet()) {
            TrendData t = new TrendData();
            t.setLabel(e.getKey());
            t.setPatients(e.getValue());
            trend.add(t);
        }
        return trend;
    }

    private String weekBucketLabel(LocalDate day) {
        int weekNumber = ((day.getDayOfMonth() - 1) / 7) + 1; // days 1-7 -> Week 1, 8-14 -> Week 2, ...
        return "Week " + weekNumber;
    }

    /**
     * Fetches the clinic's real opening/closing hours via AdminServiceClient.getClinicById()
     * and generates dynamic time slots instead of a hardcoded 8am-8pm range.
     * Falls back to DEFAULT_OPEN_HOUR/DEFAULT_CLOSE_HOUR if the lookup fails
     * or the clinic has no hours configured.
     */
    @SuppressWarnings("unchecked")
    private int[] resolveHourSlots(String clinicId, String branchId) {
        int openHour = DEFAULT_OPEN_HOUR;
        int closeHour = DEFAULT_CLOSE_HOUR;
        try {
            Response clinicResp = adminServiceClient
                    .getClinicById(clinicId)
                    .getBody();
            if (clinicResp != null && clinicResp.isSuccess() && clinicResp.getData() != null) {
                Object rawData = clinicResp.getData();
                String openingTime = null;
                String closingTime = null;
                // ---- getData() is generic (Object), Feign deserializes it as a Map ----
                if (rawData instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) rawData;
                    Object opening = data.get("openingTime");
                    Object closing = data.get("closingTime");
                    openingTime = opening != null ? opening.toString() : null;
                    closingTime = closing != null ? closing.toString() : null;
                }
                // ---- If getData() ever returns a typed ClinicDTO instead, use: ----
                // ClinicDTO clinic = (ClinicDTO) rawData;
                // openingTime = clinic.getOpeningTime();
                // closingTime = clinic.getClosingTime();
                if (openingTime != null && !openingTime.isBlank()
                        && closingTime != null && !closingTime.isBlank()) {
                    openHour = LocalTime.parse(openingTime.trim(), CLINIC_TIME_FORMAT).getHour();
                    closeHour = LocalTime.parse(closingTime.trim(), CLINIC_TIME_FORMAT).getHour();
                } else {
                    log.warn("Clinic hours missing for clinicId={}. Using default {}am-{}pm.",
                            clinicId, DEFAULT_OPEN_HOUR, DEFAULT_CLOSE_HOUR - 12);
                }
            } else {
                log.warn("Clinic lookup returned no data for clinicId={}. Using default hours.", clinicId);
            }
        } catch (Exception e) {
            log.warn("Could not resolve clinic hours for clinicId={}. Falling back to default hours.",
                    clinicId, e);
        }

        // Safety check: if parsed hours are invalid, fall back to defaults
        // rather than producing a broken/empty trend.
        if (openHour >= closeHour) {
            log.warn("Invalid clinic hours resolved (open={}, close={}) for clinicId={}. Using defaults.",
                    openHour, closeHour, clinicId);
            openHour = DEFAULT_OPEN_HOUR;
            closeHour = DEFAULT_CLOSE_HOUR;
        }

        List<Integer> slots = new ArrayList<>();
        for (int h = openHour; h <= closeHour; h += SLOT_INTERVAL_HOURS) {
            slots.add(h);
        }
        if (slots.isEmpty() || slots.get(slots.size() - 1) != closeHour) {
            slots.add(closeHour);
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private String[] toLabels(int[] hourSlots) {
        String[] labels = new String[hourSlots.length];
        for (int i = 0; i < hourSlots.length; i++) {
            int h = hourSlots[i] % 24;
            String period = h >= 12 ? "pm" : "am";
            int display = (h % 12 == 0) ? 12 : h % 12;
            labels[i] = display + period;
        }
        return labels;
    }

    private String nearestHourBucket(int hour, int[] slots, String[] labels) {
        int closestIdx = 0;
        int closestDiff = Integer.MAX_VALUE;
        for (int i = 0; i < slots.length; i++) {
            int diff = Math.abs(slots[i] - hour);
            if (diff < closestDiff) { closestDiff = diff; closestIdx = i; }
        }
        return labels[closestIdx];
    }

    // Scoped to the date-filtered lists (newInPeriod / newInPrevPeriod) instead
    // of the branch's all-time customer list, so male/female/total reflect only
    // new registrations within the selected range.
    private List<AgeGroupAnalytics> buildAgeGroupAnalytics(List<CustomerOnbording> newInPeriod,
                                                            List<CustomerOnbording> newInPrevPeriod) {
        String[] groups = {"0-18 Years", "19-35 Years", "36-50 Years", "51+ Years"};
        List<AgeGroupAnalytics> result = new ArrayList<>();
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            List<CustomerOnbording> inGroup = newInPeriod.stream()
                    .filter(c -> groupOf(parseAge(c.getAge())).equals(group))
                    .collect(Collectors.toList());
            int male = (int) inGroup.stream().filter(c -> isGender(c, "male")).count();
            int female = (int) inGroup.stream().filter(c -> isGender(c, "female")).count();
            int prevCount = (int) newInPrevPeriod.stream()
                    .filter(c -> groupOf(parseAge(c.getAge())).equals(group)).count();
            AgeGroupAnalytics a = new AgeGroupAnalytics();
            a.setId((long) (i + 1));
            a.setAgeGroup(group);
            a.setMale(male);
            a.setFemale(female);
            a.setTotal(male + female);
            a.setGrowthTrend((int) Math.round(growthRate(inGroup.size(), prevCount)));
            result.add(a);
        }
        return result;
    }

    private boolean isGender(CustomerOnbording c, String gender) {
        return c.getGender() != null && c.getGender().equalsIgnoreCase(gender);
    }

    private boolean matchesAgeGroup(CustomerOnbording c, String search) {
        String group = groupOf(parseAge(c.getAge()));
        return group.toLowerCase().contains(search.toLowerCase());
    }

    // Handles ages like "27 yrs" / "27" by stripping any non-digit characters.
    private int parseAge(String age) {
        if (age == null || age.isBlank()) return -1;
        try {
            String digitsOnly = age.replaceAll("[^0-9]", "");
            if (digitsOnly.isBlank()) return -1;
            return Integer.parseInt(digitsOnly);
        } catch (Exception e) {
            log.warn("Could not parse age='{}'", age, e);
            return -1;
        }
    }

    private String groupOf(int age) {
        if (age < 0) return "Unknown";
        if (age <= 18) return "0-18 Years";
        if (age <= 35) return "19-35 Years";
        if (age <= 50) return "36-50 Years";
        return "51+ Years";
    }
}