package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.clinicadmin.feignclient.BookingFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.Session;
import com.clinicadmin.dto.physioresponse.ExerciseResponse;
import com.clinicadmin.dto.physioresponse.PackageResponse;
import com.clinicadmin.dto.physioresponse.PaymentRecordResponse;
import com.clinicadmin.dto.physioresponse.ProgramResponse;
import com.clinicadmin.dto.physioresponse.TherapyResponse;
import com.clinicadmin.dto.physioresponse.TreatmentAnalyticsResponse;
import com.clinicadmin.dto.physioresponse.TreatmentRow;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.service.TreatmentAnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TreatmentAnalyticsServiceImpl implements TreatmentAnalyticsService {

    @Autowired
    private PhysiotherapyFeignClient physiotherapyDoctorFeign;

    @Autowired
    private BookingFeign bookingFeign;


    private final ObjectMapper mapper = new ObjectMapper();

    // one flattened "treatment occurrence" before grouping
    private static class Entry {
        String name;
        String type; // Activity / Therapy / Program / Package
        String patientId;
        int sessions;
        int completed;
        double revenue; // revenue from COMPLETED sessions only
    }

    // per-exercise session tally within the selected date window
    private static class SessionStats {
        int total;
        int completed;
        double completedRevenue;
    }

    // ========================================================
    // PUBLIC: PERIOD BASED (1=Today, 2=Week, 3=Month, 4=Year)
    // ========================================================
    @Override
    public Response getTreatmentAnalytics(String clinicId, String branchId, String type, String period) {
        Predicate<LocalDate> periodFilter = buildPeriodFilter(period);
        TreatmentAnalyticsResponse analytics = getAnalytics(clinicId, branchId, type, periodFilter);
        return Response.builder().success(true).status(200).message("Treatment analytics fetched successfully")
                .data(analytics).build();
    }

    // ========================================================
    // PUBLIC: CUSTOM DATE RANGE (inclusive, yyyy-MM-dd)
    // ========================================================
    @Override
    public Response getTreatmentAnalyticsByDateRange(String clinicId, String branchId, String type, String fromDate,
                                                     String toDate) {

        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);

        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        final LocalDate f = from;
        final LocalDate t = to;
        Predicate<LocalDate> periodFilter = d -> !d.isBefore(f) && !d.isAfter(t);

        TreatmentAnalyticsResponse analytics = getAnalytics(clinicId, branchId, type, periodFilter);

        return Response.builder().success(true).status(200).message("Treatment analytics fetched successfully")
                .data(analytics).build();
    }

    // ========================================================
    // SHARED CORE
    // ========================================================
    // periodFilter is now applied against each PaymentRecordResponse's
    // sessionStartDate. If a record's sessionStartDate doesn't fall in the
    // selected period, the ENTIRE record (all its exercises/therapies/
    // programs/packages) is skipped. Once a record passes, all of its
    // individual sessions are counted (no more per-session date filtering).
    private TreatmentAnalyticsResponse getAnalytics(String clinicId, String branchId, String type,
                                                    Predicate<LocalDate> periodFilter) {

        List<PaymentRecordResponse> records = fetchPaymentRecords(clinicId, branchId);
        List<Entry> allEntries = new ArrayList<>();

        // record already passed the period check via sessionStartDate above,
        // so every session inside it is included -> no per-session date filter
        Predicate<LocalDate> includeAllSessions = d -> true;

        for (PaymentRecordResponse record : records) {
            if (record.getServiceType() == null)
                continue;

            LocalDate recordStart = parseDate(record.getSessionStartDate());
            if (recordStart == null || !periodFilter.test(recordStart))
                continue; // record didn't start in the selected period -> skip entirely

            String serviceType = record.getServiceType().toLowerCase();
            String category = toCategory(serviceType);

            allEntries.addAll(extractEntries(record, serviceType, category, includeAllSessions));
        }

        String normalizedType = normalize(type);
        boolean allTypes = normalizedType.isEmpty() || normalizedType.equals("alltypes")
                || normalizedType.equals("all");

        // filter on each entry's own resolved type (works for the normal
        // exercise/therapy/program/package path AND for the best-effort
        // fallback path, where the real type is only known per-entry).
        List<Entry> entries = allTypes ? allEntries
                : allEntries.stream().filter(e -> normalize(e.type).equals(normalizedType)).toList();

        return buildResponse(entries);
    }

    // safely parses a yyyy-MM-dd date string, returning null instead of
    // throwing (records with a missing/malformed sessionStartDate are
    // treated as not belonging to any period and get excluded)
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // strip whitespace/dashes/underscores, lowercase — makes comparisons
    // resilient to how the client formats path-variable values
    private String normalize(String value) {
        if (value == null)
            return "";
        return value.trim().toLowerCase().replaceAll("[\\s_-]+", "");
    }

    // ========================================================
    // FETCH RECORDS FROM PHYSIOTHERAPY-DOCTOR SERVICE
    // ========================================================
    @SuppressWarnings("unchecked")
    private List<PaymentRecordResponse> fetchPaymentRecords(String clinicId, String branchId) {
        try {
            Response response = physiotherapyDoctorFeign.getPayments(clinicId, branchId);
            if (response == null || response.getData() == null)
                return List.of();

            List<Map<String, Object>> raw = mapper.convertValue(response.getData(), List.class);
            List<PaymentRecordResponse> result = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                result.add(mapper.convertValue(m, PaymentRecordResponse.class));
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toCategory(String serviceType) {
        return switch (serviceType) {
            case "exercise" -> "Activity";
            case "therapy" -> "Therapy";
            case "program" -> "Program";
            case "package" -> "Package";
            default -> "All";
        };
    }

    // ========================================================
    // EXTRACT TOP-LEVEL ENTRIES PER RECORD (per its serviceType)
    // ========================================================
    @SuppressWarnings("unchecked")
    private List<Entry> extractEntries(PaymentRecordResponse record, String serviceType, String category,
                                       Predicate<LocalDate> dateFilter) {

        List<Entry> list = new ArrayList<>();
        Object data = record.getTherapyWithSessions();
        if (data == null)
            return list;

        List<Map<String, Object>> rawList = mapper.convertValue(data, List.class);

        switch (serviceType) {
            case "exercise" -> {
                for (Map<String, Object> m : rawList) {
                    ExerciseResponse ex = mapper.convertValue(m, ExerciseResponse.class);
                    list.add(fromExercise(ex, category, record.getPatientId(), dateFilter));
                }
            }
            case "therapy" -> {
                for (Map<String, Object> m : rawList) {
                    TherapyResponse t = mapper.convertValue(m, TherapyResponse.class);
                    list.add(fromTherapy(t, category, record.getPatientId(), dateFilter));
                }
            }
            case "program" -> {
                for (Map<String, Object> m : rawList) {
                    ProgramResponse p = mapper.convertValue(m, ProgramResponse.class);
                    list.add(fromProgram(p, category, record.getPatientId(), dateFilter));
                }
            }
            case "package" -> {
                for (Map<String, Object> m : rawList) {
                    PackageResponse pkg = mapper.convertValue(m, PackageResponse.class);
                    list.add(fromPackage(pkg, category, record.getPatientId(), dateFilter));
                }
            }
            default -> {
                // unknown/missing serviceType -> don't guess a single shape and risk
                // dropping the record; try every known shape per node and keep
                // whichever ones parse successfully, so we still surface all the data.
                for (Map<String, Object> m : rawList) {
                    list.addAll(tryAllShapes(m, record.getPatientId(), dateFilter));
                }
            }
        }
        return list;
    }

    // best-effort: attempt package -> program -> therapy -> exercise parsing on a
    // single node and return an Entry for whichever shape actually matches
    private List<Entry> tryAllShapes(Map<String, Object> m, String patientId, Predicate<LocalDate> dateFilter) {
        List<Entry> results = new ArrayList<>();

        try {
            PackageResponse pkg = mapper.convertValue(m, PackageResponse.class);
            if (pkg.getPackageName() != null) {
                results.add(fromPackage(pkg, "Package", patientId, dateFilter));
                return results;
            }
        } catch (Exception ignored) {
        }

        try {
            ProgramResponse p = mapper.convertValue(m, ProgramResponse.class);
            if (p.getProgramName() != null) {
                results.add(fromProgram(p, "Program", patientId, dateFilter));
                return results;
            }
        } catch (Exception ignored) {
        }

        try {
            TherapyResponse t = mapper.convertValue(m, TherapyResponse.class);
            if (t.getTherapyName() != null) {
                results.add(fromTherapy(t, "Therapy", patientId, dateFilter));
                return results;
            }
        } catch (Exception ignored) {
        }

        try {
            ExerciseResponse ex = mapper.convertValue(m, ExerciseResponse.class);
            if (ex.getExerciseName() != null) {
                results.add(fromExercise(ex, "Activity", patientId, dateFilter));
            }
        } catch (Exception ignored) {
        }

        return results;
    }

    private Entry fromExercise(ExerciseResponse ex, String category, String patientId,
                               Predicate<LocalDate> dateFilter) {
        Entry e = new Entry();
        e.name = ex.getExerciseName();
        e.type = category;
        e.patientId = patientId;
        double pricePerSession = resolvePricePerSession(ex);
        SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
        e.sessions = stats.total;
        e.completed = stats.completed;
        e.revenue = stats.completedRevenue;
        return e;
    }

    private Entry fromTherapy(TherapyResponse t, String category, String patientId, Predicate<LocalDate> dateFilter) {
        Entry e = new Entry();
        e.name = t.getTherapyName();
        e.type = category;
        e.patientId = patientId;
        int sessions = 0, completed = 0;
        double revenue = 0;
        if (t.getExercises() != null) {
            for (ExerciseResponse ex : t.getExercises()) {
                double pricePerSession = resolvePricePerSession(ex);
                SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
                sessions += stats.total;
                completed += stats.completed;
                revenue += stats.completedRevenue;
            }
        }
        e.sessions = sessions;
        e.completed = completed;
        e.revenue = revenue;
        return e;
    }

    private Entry fromProgram(ProgramResponse p, String category, String patientId, Predicate<LocalDate> dateFilter) {
        Entry e = new Entry();
        e.name = p.getProgramName();
        e.type = category;
        e.patientId = patientId;
        int sessions = 0, completed = 0;
        double revenue = 0;
        if (p.getTherapyData() != null) {
            for (TherapyResponse t : p.getTherapyData()) {
                if (t.getExercises() == null)
                    continue;
                for (ExerciseResponse ex : t.getExercises()) {
                    double pricePerSession = resolvePricePerSession(ex);
                    SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
                    sessions += stats.total;
                    completed += stats.completed;
                    revenue += stats.completedRevenue;
                }
            }
        }
        e.sessions = sessions;
        e.completed = completed;
        e.revenue = revenue;
        return e;
    }

    private Entry fromPackage(PackageResponse pkg, String category, String patientId, Predicate<LocalDate> dateFilter) {
        Entry e = new Entry();
        e.name = pkg.getPackageName();
        e.type = category;
        e.patientId = patientId;
        int sessions = 0, completed = 0;
        double revenue = 0;
        if (pkg.getPrograms() != null) {
            for (ProgramResponse p : pkg.getPrograms()) {
                if (p.getTherapyData() == null)
                    continue;
                for (TherapyResponse t : p.getTherapyData()) {
                    if (t.getExercises() == null)
                        continue;
                    for (ExerciseResponse ex : t.getExercises()) {
                        double pricePerSession = resolvePricePerSession(ex);
                        SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
                        sessions += stats.total;
                        completed += stats.completed;
                        revenue += stats.completedRevenue;
                    }
                }
            }
        }
        e.sessions = sessions;
        e.completed = completed;
        e.revenue = revenue;
        return e;
    }

    // ========================================================
    // SESSION COUNTING WITH DATE FILTER -> total / completed / completed-revenue
    // (dateFilter is now a pass-through "d -> true" from getAnalytics, since
    // filtering happens at the record level via sessionStartDate; the method
    // itself is unchanged so it still works correctly if a real per-session
    // filter is ever passed in again)
    // ========================================================
    private SessionStats countSessions(List<Session> sessions, Predicate<LocalDate> dateFilter,
                                       double pricePerSession) {
        SessionStats stats = new SessionStats();
        if (sessions == null)
            return stats;

        for (Session s : sessions) {
            if (s.getDate() == null)
                continue;
            LocalDate d;
            try {
                d = LocalDate.parse(s.getDate());
            } catch (Exception ex) {
                continue;
            }
            if (!dateFilter.test(d))
                continue;

            stats.total++;
            if ("Completed".equalsIgnoreCase(s.getStatus())) {
                stats.completed++;
                stats.completedRevenue += pricePerSession;
            }
        }
        return stats;
    }

    // resolves a per-session price for an exercise, falling back to
    // total price / number of sessions when pricePerSession isn't set
    private double resolvePricePerSession(ExerciseResponse ex) {
        if (ex.getPricePerSession() != null && ex.getPricePerSession() > 0) {
            return ex.getPricePerSession();
        }
        Double total = ex.getTotalExercisePrice() != null ? ex.getTotalExercisePrice() : ex.getTotalPrice();
        Integer count = ex.getNoOfSessions();
        if (total != null && count != null && count > 0) {
            return total / count;
        }
        return 0;
    }

    // ========================================================
    // PERIOD -> PREDICATE
    // 1 = Today, 2 = Week, 3 = Month, 4 = Year, anything else / missing = All
    // (now applied against record.sessionStartDate, not per-session dates)
    // ========================================================
    private Predicate<LocalDate> buildPeriodFilter(String period) {
        LocalDate today = LocalDate.now();

        String normalizedPeriod = normalize(period);

        return switch (normalizedPeriod) {
            case "1" -> d -> d.isEqual(today);
            case "2" -> d -> d.get(IsoFields.WEEK_BASED_YEAR) == today.get(IsoFields.WEEK_BASED_YEAR)
                    && d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            case "3" -> d -> d.getMonth() == today.getMonth() && d.getYear() == today.getYear();
            case "4" -> d -> d.getYear() == today.getYear();
            default -> d -> true; // no/unknown period -> all data
        };
    }

    // ========================================================
    // GROUP ENTRIES BY (name, type) -> BUILD FINAL RESPONSE
    // ========================================================
    private TreatmentAnalyticsResponse buildResponse(List<Entry> entries) {

        Map<String, TreatmentRow> grouped = new LinkedHashMap<>();
        Map<String, Set<String>> patientsByTreatment = new LinkedHashMap<>();
        Map<String, List<Double>> revenueByTreatment = new LinkedHashMap<>();

        for (Entry e : entries) {
            // no sessions at all in the selected window -> exclude, so the filter
            // actually narrows the table instead of just zeroing counts. Entries
            // with sessions but zero completions still count toward "sessions",
            // they just won't contribute to revenue/patients (completed-only).
            if (e.sessions == 0)
                continue;

            String key = e.name + "|" + e.type;
            TreatmentRow row = grouped.computeIfAbsent(key, k -> {
                TreatmentRow r = new TreatmentRow();
                r.setTreatmentName(e.name);
                r.setType(e.type);
                return r;
            });
            row.setSessions(row.getSessions() + e.sessions);
            row.setCompleted(row.getCompleted() + e.completed);
            // Count every patient who has sessions in the selected period
            patientsByTreatment.computeIfAbsent(key, k -> new HashSet<>()).add(e.patientId);

            // Revenue only for completed sessions
            if (e.completed > 0) {
                revenueByTreatment.computeIfAbsent(key, k -> new ArrayList<>()).add(e.revenue);
            }
            // only count this patient if they actually completed a session for
            // this treatment in the selected window
//			if (e.completed > 0) {
//				patientsByTreatment.computeIfAbsent(key, k -> new HashSet<>()).add(e.patientId);
//				revenueByTreatment.computeIfAbsent(key, k -> new ArrayList<>()).add(e.revenue);
//			}
        }

        for (Map.Entry<String, TreatmentRow> en : grouped.entrySet()) {

            String key = en.getKey();
            TreatmentRow row = en.getValue();

            row.setPatients(patientsByTreatment.getOrDefault(key, Set.of()).size());

            row.setSuccessRate(row.getSessions() == 0 ? 0 : round2(row.getCompleted() * 100.0 / row.getSessions()));

            double revenue = revenueByTreatment.getOrDefault(key, List.of()).stream().mapToDouble(Double::doubleValue)
                    .sum();

            row.setAvgRevenue(round2(revenue));
        }
        List<TreatmentRow> rows = new ArrayList<>(grouped.values());

        TreatmentAnalyticsResponse res = new TreatmentAnalyticsResponse();
        res.setTreatments(rows);

        res.setTotalTreatmentTypes(rows.size());
        res.setTotalTreatments(rows.size());

        // Total sessions (all sessions in selected period)
        res.setTotalSessions(rows.stream().mapToInt(TreatmentRow::getSessions).sum());

        // Total completed sessions
        int totalCompleted = rows.stream().mapToInt(TreatmentRow::getCompleted).sum();

        // Total patients (only patients with completed sessions)
        Set<String> allPatients = new HashSet<>();
        patientsByTreatment.values().forEach(allPatients::addAll);
        res.setTotalPatients(allPatients.size());

        // Overall success rate
        res.setAvgSuccessRate(
                res.getTotalSessions() == 0 ? 0 : round2((totalCompleted * 100.0) / res.getTotalSessions()));

        // Highly rated treatments
        res.setHighlyRatedCount((int) rows.stream().filter(r -> r.getSuccessRate() >= 90).count());

        // Total revenue (completed sessions only)
        double totalRevenue = revenueByTreatment.values().stream().flatMap(List::stream)
                .mapToDouble(Double::doubleValue).sum();

        // Average revenue per treatment
        res.setAvgRevenuePerTreatment(rows.isEmpty() ? 0 : round2(totalRevenue / rows.size()));

        return res;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

}


//package com.clinicadmin.service.impl;
//
//import java.time.LocalDate;
//import java.time.temporal.IsoFields;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Predicate;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.Session;
//import com.clinicadmin.dto.physioresponse.ExerciseResponse;
//import com.clinicadmin.dto.physioresponse.PackageResponse;
//import com.clinicadmin.dto.physioresponse.PaymentRecordResponse;
//import com.clinicadmin.dto.physioresponse.ProgramResponse;
//import com.clinicadmin.dto.physioresponse.TherapyResponse;
//import com.clinicadmin.dto.physioresponse.TreatmentAnalyticsResponse;
//import com.clinicadmin.dto.physioresponse.TreatmentRow;
//import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
//import com.clinicadmin.service.TreatmentAnalyticsService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Service
//public class TreatmentAnalyticsServiceImpl implements TreatmentAnalyticsService {
//
//	@Autowired
//	private PhysiotherapyFeignClient physiotherapyDoctorFeign;
//
//	private final ObjectMapper mapper = new ObjectMapper();
//
//	// one flattened "treatment occurrence" before grouping
//	private static class Entry {
//		String name;
//		String type; // Activity / Therapy / Program / Package
//		String patientId;
//		int sessions;
//		int completed;
//		double revenue; // revenue from COMPLETED sessions only
//	}
//
//	// per-exercise session tally within the selected date window
//	private static class SessionStats {
//		int total;
//		int completed;
//		double completedRevenue;
//	}
//
//	// ========================================================
//	// PUBLIC: PERIOD BASED (1=Today, 2=Week, 3=Month, 4=Year)
//	// ========================================================
//	@Override
//	public Response getTreatmentAnalytics(String clinicId, String branchId, String type, String period) {
//		Predicate<LocalDate> dateFilter = buildPeriodFilter(period);
//		TreatmentAnalyticsResponse analytics = getAnalytics(clinicId, branchId, type, dateFilter);
//		return Response.builder().success(true).status(200).message("Treatment analytics fetched successfully")
//				.data(analytics).build();
//	}
//
//	// ========================================================
//	// PUBLIC: CUSTOM DATE RANGE (inclusive, yyyy-MM-dd)
//	// ========================================================
//	@Override
//	public Response getTreatmentAnalyticsByDateRange(String clinicId, String branchId, String type, String fromDate,
//			String toDate) {
//
//		LocalDate from = LocalDate.parse(fromDate);
//		LocalDate to = LocalDate.parse(toDate);
//
//		if (from.isAfter(to)) {
//			LocalDate tmp = from;
//			from = to;
//			to = tmp;
//		}
//
//		final LocalDate f = from;
//		final LocalDate t = to;
//		Predicate<LocalDate> dateFilter = d -> !d.isBefore(f) && !d.isAfter(t);
//
//		TreatmentAnalyticsResponse analytics = getAnalytics(clinicId, branchId, type, dateFilter);
//
//		return Response.builder().success(true).status(200).message("Treatment analytics fetched successfully")
//				.data(analytics).build();
//	}
//
//	// ========================================================
//	// SHARED CORE
//	// ========================================================
//	private TreatmentAnalyticsResponse getAnalytics(String clinicId, String branchId, String type,
//			Predicate<LocalDate> dateFilter) {
//
//		List<PaymentRecordResponse> records = fetchPaymentRecords(clinicId, branchId);
//		List<Entry> allEntries = new ArrayList<>();
//
//		for (PaymentRecordResponse record : records) {
//			if (record.getServiceType() == null)
//				continue;
//
//			String serviceType = record.getServiceType().toLowerCase();
//			String category = toCategory(serviceType);
//
//			allEntries.addAll(extractEntries(record, serviceType, category, dateFilter));
//		}
//
//		String normalizedType = normalize(type);
//		boolean allTypes = normalizedType.isEmpty() || normalizedType.equals("alltypes")
//				|| normalizedType.equals("all");
//
//		// filter on each entry's own resolved type (works for the normal
//		// exercise/therapy/program/package path AND for the best-effort
//		// fallback path, where the real type is only known per-entry).
//		List<Entry> entries = allTypes ? allEntries
//				: allEntries.stream().filter(e -> normalize(e.type).equals(normalizedType)).toList();
//
//		return buildResponse(entries);
//	}
//
//	// strip whitespace/dashes/underscores, lowercase — makes comparisons
//	// resilient to how the client formats path-variable values
//	private String normalize(String value) {
//		if (value == null)
//			return "";
//		return value.trim().toLowerCase().replaceAll("[\\s_-]+", "");
//	}
//
//	// ========================================================
//	// FETCH RECORDS FROM PHYSIOTHERAPY-DOCTOR SERVICE
//	// ========================================================
//	@SuppressWarnings("unchecked")
//	private List<PaymentRecordResponse> fetchPaymentRecords(String clinicId, String branchId) {
//		try {
//			Response response = physiotherapyDoctorFeign.getPayments(clinicId, branchId);
//			if (response == null || response.getData() == null)
//				return List.of();
//
//			List<Map<String, Object>> raw = mapper.convertValue(response.getData(), List.class);
//			List<PaymentRecordResponse> result = new ArrayList<>();
//			for (Map<String, Object> m : raw) {
//				result.add(mapper.convertValue(m, PaymentRecordResponse.class));
//			}
//			return result;
//		} catch (Exception e) {
//			return List.of();
//		}
//	}
//
//	private String toCategory(String serviceType) {
//		return switch (serviceType) {
//		case "exercise" -> "Activity";
//		case "therapy" -> "Therapy";
//		case "program" -> "Program";
//		case "package" -> "Package";
//		default -> "All";
//		};
//	}
//
//	// ========================================================
//	// EXTRACT TOP-LEVEL ENTRIES PER RECORD (per its serviceType)
//	// ========================================================
//	@SuppressWarnings("unchecked")
//	private List<Entry> extractEntries(PaymentRecordResponse record, String serviceType, String category,
//			Predicate<LocalDate> dateFilter) {
//
//		List<Entry> list = new ArrayList<>();
//		Object data = record.getTherapyWithSessions();
//		if (data == null)
//			return list;
//
//		List<Map<String, Object>> rawList = mapper.convertValue(data, List.class);
//
//		switch (serviceType) {
//		case "exercise" -> {
//			for (Map<String, Object> m : rawList) {
//				ExerciseResponse ex = mapper.convertValue(m, ExerciseResponse.class);
//				list.add(fromExercise(ex, category, record.getPatientId(), dateFilter));
//			}
//		}
//		case "therapy" -> {
//			for (Map<String, Object> m : rawList) {
//				TherapyResponse t = mapper.convertValue(m, TherapyResponse.class);
//				list.add(fromTherapy(t, category, record.getPatientId(), dateFilter));
//			}
//		}
//		case "program" -> {
//			for (Map<String, Object> m : rawList) {
//				ProgramResponse p = mapper.convertValue(m, ProgramResponse.class);
//				list.add(fromProgram(p, category, record.getPatientId(), dateFilter));
//			}
//		}
//		case "package" -> {
//			for (Map<String, Object> m : rawList) {
//				PackageResponse pkg = mapper.convertValue(m, PackageResponse.class);
//				list.add(fromPackage(pkg, category, record.getPatientId(), dateFilter));
//			}
//		}
//		default -> {
//			// unknown/missing serviceType -> don't guess a single shape and risk
//			// dropping the record; try every known shape per node and keep
//			// whichever ones parse successfully, so we still surface all the data.
//			for (Map<String, Object> m : rawList) {
//				list.addAll(tryAllShapes(m, record.getPatientId(), dateFilter));
//			}
//		}
//		}
//		return list;
//	}
//
//	// best-effort: attempt package -> program -> therapy -> exercise parsing on a
//	// single node and return an Entry for whichever shape actually matches
//	private List<Entry> tryAllShapes(Map<String, Object> m, String patientId, Predicate<LocalDate> dateFilter) {
//		List<Entry> results = new ArrayList<>();
//
//		try {
//			PackageResponse pkg = mapper.convertValue(m, PackageResponse.class);
//			if (pkg.getPackageName() != null) {
//				results.add(fromPackage(pkg, "Package", patientId, dateFilter));
//				return results;
//			}
//		} catch (Exception ignored) {
//		}
//
//		try {
//			ProgramResponse p = mapper.convertValue(m, ProgramResponse.class);
//			if (p.getProgramName() != null) {
//				results.add(fromProgram(p, "Program", patientId, dateFilter));
//				return results;
//			}
//		} catch (Exception ignored) {
//		}
//
//		try {
//			TherapyResponse t = mapper.convertValue(m, TherapyResponse.class);
//			if (t.getTherapyName() != null) {
//				results.add(fromTherapy(t, "Therapy", patientId, dateFilter));
//				return results;
//			}
//		} catch (Exception ignored) {
//		}
//
//		try {
//			ExerciseResponse ex = mapper.convertValue(m, ExerciseResponse.class);
//			if (ex.getExerciseName() != null) {
//				results.add(fromExercise(ex, "Activity", patientId, dateFilter));
//			}
//		} catch (Exception ignored) {
//		}
//
//		return results;
//	}
//
//	private Entry fromExercise(ExerciseResponse ex, String category, String patientId,
//			Predicate<LocalDate> dateFilter) {
//		Entry e = new Entry();
//		e.name = ex.getExerciseName();
//		e.type = category;
//		e.patientId = patientId;
//		double pricePerSession = resolvePricePerSession(ex);
//		SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
//		e.sessions = stats.total;
//		e.completed = stats.completed;
//		e.revenue = stats.completedRevenue;
//		return e;
//	}
//
//	private Entry fromTherapy(TherapyResponse t, String category, String patientId, Predicate<LocalDate> dateFilter) {
//		Entry e = new Entry();
//		e.name = t.getTherapyName();
//		e.type = category;
//		e.patientId = patientId;
//		int sessions = 0, completed = 0;
//		double revenue = 0;
//		if (t.getExercises() != null) {
//			for (ExerciseResponse ex : t.getExercises()) {
//				double pricePerSession = resolvePricePerSession(ex);
//				SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
//				sessions += stats.total;
//				completed += stats.completed;
//				revenue += stats.completedRevenue;
//			}
//		}
//		e.sessions = sessions;
//		e.completed = completed;
//		e.revenue = revenue;
//		return e;
//	}
//
//	private Entry fromProgram(ProgramResponse p, String category, String patientId, Predicate<LocalDate> dateFilter) {
//		Entry e = new Entry();
//		e.name = p.getProgramName();
//		e.type = category;
//		e.patientId = patientId;
//		int sessions = 0, completed = 0;
//		double revenue = 0;
//		if (p.getTherapyData() != null) {
//			for (TherapyResponse t : p.getTherapyData()) {
//				if (t.getExercises() == null)
//					continue;
//				for (ExerciseResponse ex : t.getExercises()) {
//					double pricePerSession = resolvePricePerSession(ex);
//					SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
//					sessions += stats.total;
//					completed += stats.completed;
//					revenue += stats.completedRevenue;
//				}
//			}
//		}
//		e.sessions = sessions;
//		e.completed = completed;
//		e.revenue = revenue;
//		return e;
//	}
//
//	private Entry fromPackage(PackageResponse pkg, String category, String patientId, Predicate<LocalDate> dateFilter) {
//		Entry e = new Entry();
//		e.name = pkg.getPackageName();
//		e.type = category;
//		e.patientId = patientId;
//		int sessions = 0, completed = 0;
//		double revenue = 0;
//		if (pkg.getPrograms() != null) {
//			for (ProgramResponse p : pkg.getPrograms()) {
//				if (p.getTherapyData() == null)
//					continue;
//				for (TherapyResponse t : p.getTherapyData()) {
//					if (t.getExercises() == null)
//						continue;
//					for (ExerciseResponse ex : t.getExercises()) {
//						double pricePerSession = resolvePricePerSession(ex);
//						SessionStats stats = countSessions(ex.getSessions(), dateFilter, pricePerSession);
//						sessions += stats.total;
//						completed += stats.completed;
//						revenue += stats.completedRevenue;
//					}
//				}
//			}
//		}
//		e.sessions = sessions;
//		e.completed = completed;
//		e.revenue = revenue;
//		return e;
//	}
//
//	// ========================================================
//	// SESSION COUNTING WITH DATE FILTER -> total / completed / completed-revenue
//	// ========================================================
//	private SessionStats countSessions(List<Session> sessions, Predicate<LocalDate> dateFilter,
//			double pricePerSession) {
//		SessionStats stats = new SessionStats();
//		if (sessions == null)
//			return stats;
//
//		for (Session s : sessions) {
//			if (s.getDate() == null)
//				continue;
//			LocalDate d;
//			try {
//				d = LocalDate.parse(s.getDate());
//			} catch (Exception ex) {
//				continue;
//			}
//			if (!dateFilter.test(d))
//				continue;
//
//			stats.total++;
//			if ("Completed".equalsIgnoreCase(s.getStatus())) {
//				stats.completed++;
//				stats.completedRevenue += pricePerSession;
//			}
//		}
//		return stats;
//	}
//
//	// resolves a per-session price for an exercise, falling back to
//	// total price / number of sessions when pricePerSession isn't set
//	private double resolvePricePerSession(ExerciseResponse ex) {
//		if (ex.getPricePerSession() != null && ex.getPricePerSession() > 0) {
//			return ex.getPricePerSession();
//		}
//		Double total = ex.getTotalExercisePrice() != null ? ex.getTotalExercisePrice() : ex.getTotalPrice();
//		Integer count = ex.getNoOfSessions();
//		if (total != null && count != null && count > 0) {
//			return total / count;
//		}
//		return 0;
//	}
//
//	// ========================================================
//	// PERIOD -> PREDICATE
//	// 1 = Today, 2 = Week, 3 = Month, 4 = Year, anything else / missing = All
//	// ========================================================
//	private Predicate<LocalDate> buildPeriodFilter(String period) {
//		LocalDate today = LocalDate.now();
//
//		String normalizedPeriod = normalize(period);
//
//		return switch (normalizedPeriod) {
//		case "1" -> d -> d.isEqual(today);
//		case "2" -> d -> d.get(IsoFields.WEEK_BASED_YEAR) == today.get(IsoFields.WEEK_BASED_YEAR)
//				&& d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
//		case "3" -> d -> d.getMonth() == today.getMonth() && d.getYear() == today.getYear();
//		case "4" -> d -> d.getYear() == today.getYear();
//		default -> d -> true; // no/unknown period -> all data
//		};
//	}
//
//	// ========================================================
//	// GROUP ENTRIES BY (name, type) -> BUILD FINAL RESPONSE
//	// ========================================================
//	private TreatmentAnalyticsResponse buildResponse(List<Entry> entries) {
//
//		Map<String, TreatmentRow> grouped = new LinkedHashMap<>();
//		Map<String, Set<String>> patientsByTreatment = new LinkedHashMap<>();
//		Map<String, List<Double>> revenueByTreatment = new LinkedHashMap<>();
//
//		for (Entry e : entries) {
//			// no sessions at all in the selected window -> exclude, so the filter
//			// actually narrows the table instead of just zeroing counts. Entries
//			// with sessions but zero completions still count toward "sessions",
//			// they just won't contribute to revenue/patients (completed-only).
//			if (e.sessions == 0)
//				continue;
//
//			String key = e.name + "|" + e.type;
//			TreatmentRow row = grouped.computeIfAbsent(key, k -> {
//				TreatmentRow r = new TreatmentRow();
//				r.setTreatmentName(e.name);
//				r.setType(e.type);
//				return r;
//			});
//			row.setSessions(row.getSessions() + e.sessions);
//			row.setCompleted(row.getCompleted() + e.completed);
//			// Count every patient who has sessions in the selected period
//			patientsByTreatment.computeIfAbsent(key, k -> new HashSet<>()).add(e.patientId);
//
//			// Revenue only for completed sessions
//			if (e.completed > 0) {
//				revenueByTreatment.computeIfAbsent(key, k -> new ArrayList<>()).add(e.revenue);
//			}
//			// only count this patient if they actually completed a session for
//			// this treatment in the selected window
////			if (e.completed > 0) {
////				patientsByTreatment.computeIfAbsent(key, k -> new HashSet<>()).add(e.patientId);
////				revenueByTreatment.computeIfAbsent(key, k -> new ArrayList<>()).add(e.revenue);
////			}
//		}
//
//		for (Map.Entry<String, TreatmentRow> en : grouped.entrySet()) {
//
//			String key = en.getKey();
//			TreatmentRow row = en.getValue();
//
//			row.setPatients(patientsByTreatment.getOrDefault(key, Set.of()).size());
//
//			row.setSuccessRate(row.getSessions() == 0 ? 0 : round2(row.getCompleted() * 100.0 / row.getSessions()));
//
//			double revenue = revenueByTreatment.getOrDefault(key, List.of()).stream().mapToDouble(Double::doubleValue)
//					.sum();
//
//			row.setAvgRevenue(round2(revenue));
//		}
//		List<TreatmentRow> rows = new ArrayList<>(grouped.values());
//
//		TreatmentAnalyticsResponse res = new TreatmentAnalyticsResponse();
//		res.setTreatments(rows);
//
//		res.setTotalTreatmentTypes(rows.size());
//		res.setTotalTreatments(rows.size());
//
//		// Total sessions (all sessions in selected period)
//		res.setTotalSessions(rows.stream().mapToInt(TreatmentRow::getSessions).sum());
//
//		// Total completed sessions
//		int totalCompleted = rows.stream().mapToInt(TreatmentRow::getCompleted).sum();
//
//		// Total patients (only patients with completed sessions)
//		Set<String> allPatients = new HashSet<>();
//		patientsByTreatment.values().forEach(allPatients::addAll);
//		res.setTotalPatients(allPatients.size());
//
//		// Overall success rate
//		res.setAvgSuccessRate(
//				res.getTotalSessions() == 0 ? 0 : round2((totalCompleted * 100.0) / res.getTotalSessions()));
//
//		// Highly rated treatments
//		res.setHighlyRatedCount((int) rows.stream().filter(r -> r.getSuccessRate() >= 90).count());
//
//		// Total revenue (completed sessions only)
//		double totalRevenue = revenueByTreatment.values().stream().flatMap(List::stream)
//				.mapToDouble(Double::doubleValue).sum();
//
//		// Average revenue per treatment
//		res.setAvgRevenuePerTreatment(rows.isEmpty() ? 0 : round2(totalRevenue / rows.size()));
//
//		return res;
//	}
//
//	private double round2(double v) {
//		return Math.round(v * 100.0) / 100.0;
//	}
//}