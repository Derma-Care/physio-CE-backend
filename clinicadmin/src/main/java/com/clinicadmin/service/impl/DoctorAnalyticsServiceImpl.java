package com.clinicadmin.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.DoctorDashboardDTO;
import com.clinicadmin.dto.DoctorPerformanceDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.service.DoctorAnalyticsService;

@Service
public class DoctorAnalyticsServiceImpl implements DoctorAnalyticsService {

    @Autowired
    private BookingFeign bookingFeign;

    @Autowired
    private DoctorsRepository doctorsRepository;

    @Override
    public Response getDoctorDashboard(
            String clinicId,
            String branchId,
            Integer type,
            String startDate,
            String endDate) {

        Response response = new Response();

        try {

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> bookingResponse =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(
                            clinicId,
                            branchId);

            // Booking Service Response Validation
            if (bookingResponse == null
                    || bookingResponse.getBody() == null) {

                response.setSuccess(false);
                response.setMessage("Booking Service is not responding.");
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

                return response;
            }

            ResponseStructure<List<Map<String, Object>>> body =
                    bookingResponse.getBody();

            List<Map<String, Object>> bookings = body.getData();

            if (bookings == null || bookings.isEmpty()) {

                response.setSuccess(false);
                response.setMessage("No booking data found.");
                response.setStatus(HttpStatus.NOT_FOUND.value());

                return response;
            }

            return buildDoctorDashboard(
                    bookings,
                    type,
                    startDate,
                    endDate);

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

            return response;
        }
    }

    private Response buildDoctorDashboard(
            List<Map<String, Object>> bookings,
            Integer type,
            String startDate,
            String endDate) {

        Response response = new Response();

        try {

            Map<String, Long> trendData = new LinkedHashMap<>();

            switch (type) {

                case 1:

                    trendData.put("08 AM - 10 AM", 0L);
                    trendData.put("10 AM - 12 PM", 0L);
                    trendData.put("12 PM - 02 PM", 0L);
                    trendData.put("02 PM - 04 PM", 0L);
                    trendData.put("04 PM - 06 PM", 0L);
                    trendData.put("06 PM - 08 PM", 0L);

                    break;

                case 2:

                    trendData.put("Monday", 0L);
                    trendData.put("Tuesday", 0L);
                    trendData.put("Wednesday", 0L);
                    trendData.put("Thursday", 0L);
                    trendData.put("Friday", 0L);
                    trendData.put("Saturday", 0L);
                    trendData.put("Sunday", 0L);

                    break;

                case 3:

                    LocalDate firstDay =
                            LocalDate.now().withDayOfMonth(1);

                    LocalDate lastDay =
                            firstDay.withDayOfMonth(
                                    firstDay.lengthOfMonth());

                    int totalWeeks =
                            ((lastDay.getDayOfMonth() - 1) / 7) + 1;

                    for (int i = 1; i <= totalWeeks; i++) {
                        trendData.put("Week " + i, 0L);
                    }

                    break;

                case 4:

                    trendData.put("Jan", 0L);
                    trendData.put("Feb", 0L);
                    trendData.put("Mar", 0L);
                    trendData.put("Apr", 0L);
                    trendData.put("May", 0L);
                    trendData.put("Jun", 0L);
                    trendData.put("Jul", 0L);
                    trendData.put("Aug", 0L);
                    trendData.put("Sep", 0L);
                    trendData.put("Oct", 0L);
                    trendData.put("Nov", 0L);
                    trendData.put("Dec", 0L);

                    break;

                case 5:

                    LocalDate from =
                            LocalDate.parse(startDate);

                    LocalDate to =
                            LocalDate.parse(endDate);

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("MMM d");

                    while (!from.isAfter(to)) {

                        trendData.put(
                                from.format(formatter),
                                0L);

                        from = from.plusDays(1);
                    }

                    break;

                default:
                    throw new RuntimeException(
                            "Invalid Dashboard Type");
            }

            long totalDoctors = 0;
            long totalConsultations = 0;
            long totalPatientsConsulted = 0;

            Map<String, DoctorPerformanceDTO> doctorMap =
                    new LinkedHashMap<>();

            LocalDate today = LocalDate.now();

            for (Map<String, Object> booking : bookings) {

                String serviceDateStr =
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate", ""));

                if (serviceDateStr.isBlank()) {
                    continue;
                }

                LocalDate serviceDate =
                        LocalDate.parse(serviceDateStr);

                boolean include = false;

                switch (type) {

                    case 1:

                        include =
                                serviceDate.equals(today);

                        break;

                    case 2:

                        LocalDate weekStart =
                                today.with(DayOfWeek.MONDAY);

                        LocalDate weekEnd =
                                today.with(DayOfWeek.SUNDAY);

                        include =
                                !serviceDate.isBefore(weekStart)
                                        && !serviceDate.isAfter(weekEnd);

                        break;

                    case 3:

                        include =
                                serviceDate.getMonthValue()
                                        == today.getMonthValue()
                                        && serviceDate.getYear()
                                        == today.getYear();

                        break;

                    case 4:

                        include =
                                serviceDate.getYear()
                                        == today.getYear();

                        break;

                    case 5:

                        LocalDate fromDate =
                                LocalDate.parse(startDate);

                        LocalDate toDate =
                                LocalDate.parse(endDate);

                        include =
                                !serviceDate.isBefore(fromDate)
                                        && !serviceDate.isAfter(toDate);

                        break;
                }

                if (!include) {
                    continue;
                }

                totalConsultations++;
                String doctorId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorId", ""));

                String doctorName =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorName", "N/A"));

                DoctorPerformanceDTO doctor =
                        doctorMap.computeIfAbsent(
                                doctorId,
                                id -> {

                                    DoctorPerformanceDTO dto =
                                            new DoctorPerformanceDTO();

                                    dto.setDoctorId(doctorId);
                                    dto.setDoctorName(doctorName);

                                    String speciality =
                                            doctorsRepository
                                                    .findByDoctorId(doctorId)
                                                    .map(Doctors::getSpecialization)
                                                    .orElse("");

                                    dto.setSpeciality(speciality);

                                    dto.setTotalScheduled(0);
                                    dto.setPending(0);
                                    dto.setBooked(0);
                                    dto.setCompleted(0);
                                    dto.setCancelled(0);
                                    dto.setCompletionRate(0);

                                    return dto;
                                });

                doctor.setTotalScheduled(
                        doctor.getTotalScheduled() + 1);

                String status =
                        String.valueOf(
                                booking.getOrDefault(
                                        "status", "")).toLowerCase();

                switch (status) {

                    case "pending":

                        doctor.setPending(
                                doctor.getPending() + 1);

                        break;

                    case "booked":
                    case "confirmed":
                    case "scheduled":

                        doctor.setBooked(
                                doctor.getBooked() + 1);

                        break;

                    case "completed":

                        doctor.setCompleted(
                                doctor.getCompleted() + 1);

                        break;

                    case "cancelled":
                    case "canceled":

                        doctor.setCancelled(
                                doctor.getCancelled() + 1);

                        break;
                }

                String label = "";

                switch (type) {

                    case 1:

                        String serviceTime =
                                String.valueOf(
                                        booking.getOrDefault(
                                                "servicetime", ""));

                        if (!serviceTime.isBlank()) {

                            int hour =
                                    Integer.parseInt(
                                            serviceTime.split(":")[0]);

                            if (serviceTime.toUpperCase().contains("PM")
                                    && hour != 12) {
                                hour += 12;
                            }

                            if (serviceTime.toUpperCase().contains("AM")
                                    && hour == 12) {
                                hour = 0;
                            }

                            if (hour >= 8 && hour < 10) {
                                label = "08 AM - 10 AM";
                            } else if (hour >= 10 && hour < 12) {
                                label = "10 AM - 12 PM";
                            } else if (hour >= 12 && hour < 14) {
                                label = "12 PM - 02 PM";
                            } else if (hour >= 14 && hour < 16) {
                                label = "02 PM - 04 PM";
                            } else if (hour >= 16 && hour < 18) {
                                label = "04 PM - 06 PM";
                            } else if (hour >= 18 && hour < 20) {
                                label = "06 PM - 08 PM";
                            }
                        }

                        break;

                    case 2:

                        label =
                                serviceDate.getDayOfWeek()
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.ENGLISH);

                        break;

                    case 3:

                        int week =
                                ((serviceDate.getDayOfMonth() - 1) / 7) + 1;

                        label = "Week " + week;

                        break;

                    case 4:

                        label =
                                serviceDate.getMonth()
                                        .getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.ENGLISH);

                        break;

                    case 5:

                        label =
                                serviceDate.format(
                                        DateTimeFormatter.ofPattern("MMM d"));

                        break;
                }

                if (trendData.containsKey(label)) {

                    trendData.put(
                            label,
                            trendData.get(label) + 1);
                }

            } // End booking loop

            totalDoctors = doctorMap.size();
            totalPatientsConsulted = totalConsultations;

            List<DoctorPerformanceDTO> doctorList =
                    new ArrayList<>();

            for (DoctorPerformanceDTO doctor : doctorMap.values()) {

                if (doctor.getTotalScheduled() > 0) {

                    double completionRate =
                            (doctor.getCompleted() * 100.0)
                                    / doctor.getTotalScheduled();

                    doctor.setCompletionRate(
                            Math.round(completionRate * 100.0) / 100.0);
                }

                doctorList.add(doctor);
            }

            // Summary
            Map<String, Object> summary =
                    new LinkedHashMap<>();

            summary.put(
                    "totalDoctors",
                    totalDoctors);

            summary.put(
                    "totalConsultations",
                    totalConsultations);

            summary.put(
                    "totalPatientsConsulted",
                    totalPatientsConsulted);

            // Trend Chart
            Map<String, Object> chart =
                    new LinkedHashMap<>();

            chart.put(
                    "seriesLabels",
                    new ArrayList<>(trendData.keySet()));

            chart.put(
                    "consultationVolumes",
                    new ArrayList<>(trendData.values()));

            // Dashboard DTO
            DoctorDashboardDTO dashboard = new DoctorDashboardDTO();

            dashboard.setTotalDoctors(totalDoctors);
            dashboard.setTotalAppointments(totalConsultations);
            dashboard.setTotalPatientsConsulted(totalPatientsConsulted);

            dashboard.setDoctorDetails(doctorList);

            // Final Result
            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "summary",
                    summary);

            result.put(
                    "doctorDetails",
                    doctorList);

            result.put(
                    "trendData",
                    chart);

            result.put(
                    "dashboard",
                    dashboard);

            response.setSuccess(true);
            response.setMessage(
                    "Doctor dashboard fetched successfully");
            response.setData(result);
            response.setStatus(
                    HttpStatus.OK.value());

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    @Override
    public Response getDoctorPatients(
            String clinicId,
            String branchId,
            String doctorId,
            Integer type,
            String startDate,
            String endDate) {

        Response response = new Response();

        try {

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> entity =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(
                            clinicId,
                            branchId);

            if (entity == null
                    || entity.getBody() == null
                    || entity.getBody().getData() == null) {

                response.setSuccess(false);
                response.setMessage("No booking data found");
                response.setStatus(HttpStatus.NOT_FOUND.value());

                return response;
            }

            List<Map<String, Object>> bookings =
                    entity.getBody().getData();

            List<Map<String, Object>> patients =
                    new ArrayList<>();

            String doctorName = "";

            LocalDate today = LocalDate.now();

            // Pre-parse range bounds once for type 5 (avoid re-parsing per row)
            LocalDate fromDate = null;
            LocalDate toDate = null;

            if (type != null && type == 5) {
                fromDate = LocalDate.parse(startDate);
                toDate = LocalDate.parse(endDate);
            }

            for (Map<String, Object> booking : bookings) {

                String bookingDoctorId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorId",
                                        ""));

                if (!doctorId.equals(bookingDoctorId)) {
                    continue;
                }

                // ---- Date filtering by type ----
                if (type != null) {

                    String serviceDateStr =
                            String.valueOf(
                                    booking.getOrDefault(
                                            "serviceDate", ""));

                    if (serviceDateStr.isBlank()) {
                        continue;
                    }

                    LocalDate serviceDate =
                            LocalDate.parse(serviceDateStr);

                    boolean include = false;

                    switch (type) {

                        case 1: // Today

                            include =
                                    serviceDate.equals(today);

                            break;

                        case 2: // This Week (Mon-Sun)

                            LocalDate weekStart =
                                    today.with(DayOfWeek.MONDAY);

                            LocalDate weekEnd =
                                    today.with(DayOfWeek.SUNDAY);

                            include =
                                    !serviceDate.isBefore(weekStart)
                                            && !serviceDate.isAfter(weekEnd);

                            break;

                        case 3: // This Month

                            include =
                                    serviceDate.getMonthValue()
                                            == today.getMonthValue()
                                            && serviceDate.getYear()
                                            == today.getYear();

                            break;

                        case 4: // This Year

                            include =
                                    serviceDate.getYear()
                                            == today.getYear();

                            break;

                        case 5: // Custom Range

                            include =
                                    !serviceDate.isBefore(fromDate)
                                            && !serviceDate.isAfter(toDate);

                            break;

                        default:
                            throw new RuntimeException(
                                    "Invalid Type. Allowed values: 1-5");
                    }

                    if (!include) {
                        continue;
                    }
                }
                // ---- End date filtering ----

                doctorName =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorName",
                                        ""));

                Map<String, Object> patient =
                        new LinkedHashMap<>();

                patient.put(
                        "bookingId",
                        booking.get("bookingId"));

                patient.put(
                        "patientId",
                        booking.get("patientId"));

                patient.put(
                        "patientName",
                        booking.get("name"));

                patient.put(
                        "mobileNumber",
                        booking.get("mobileNumber"));

                patient.put(
                        "gender",
                        booking.get("gender"));

                patient.put(
                        "age",
                        booking.get("age"));

                patient.put(
                        "problem",
                        booking.get("problem"));

                patient.put(
                        "serviceDate",
                        booking.get("serviceDate"));

                patient.put(
                        "serviceTime",
                        booking.get("servicetime"));

                patient.put(
                        "status",
                        booking.get("status"));

                patient.put(
                        "paymentType",
                        booking.get("paymentType"));

                patient.put(
                        "consultationFee",
                        booking.get("consultationFee"));

                patient.put(
                        "totalFee",
                        booking.get("totalFee"));

                patients.add(patient);
            }

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("doctorId", doctorId);
            result.put("doctorName", doctorName);
            result.put("totalPatients", patients.size());
            result.put("patients", patients);

            response.setSuccess(true);
            response.setMessage(
                    "Doctor patients fetched successfully");
            response.setData(result);
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
}