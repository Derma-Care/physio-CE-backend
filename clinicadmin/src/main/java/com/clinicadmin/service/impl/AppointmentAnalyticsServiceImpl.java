package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.AppointmentSummaryDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.service.AppointmentAnalyticsService;

@Service
public class AppointmentAnalyticsServiceImpl
        implements AppointmentAnalyticsService {

    @Autowired
    private BookingFeign bookingFeign;
    
    @Autowired
    private DoctorsRepository doctorsRepository;
    @Autowired
    private AdminServiceClient adminServiceClient;

    @Autowired
    private PhysiotherapyFeignClient physiotherapyFeignClient;
    @Override
    public Response getAppointmentAnalytics(
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

            if (bookingResponse == null
                    || bookingResponse.getBody() == null
                    || bookingResponse.getBody().getData() == null) {

                response.setSuccess(false);
                response.setMessage("No booking data found");
                response.setStatus(HttpStatus.NOT_FOUND.value());

                return response;
            }

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            ResponseEntity<Response> clinicResponse =
                    adminServiceClient.getClinicById(
                            clinicId);

            Map<String, Object> clinic =
                    (Map<String, Object>) clinicResponse
                            .getBody()
                            .getData();

            LocalTime openingTime =
                    parseClinicTime(
                            String.valueOf(
                                    clinic.get("openingTime")));

            LocalTime closingTime =
                    parseClinicTime(
                            String.valueOf(
                                    clinic.get("closingTime")));

            DateTimeFormatter timeFormatter =
                    DateTimeFormatter.ofPattern(
                            "hh:mm a");

            Map<String, Long> chartData =
                    new LinkedHashMap<>();

            switch (type) {

                case 1:

                    LocalTime slot =
                            openingTime;

                    while (slot.isBefore(
                            closingTime)) {

                        chartData.put(
                                slot.format(
                                        timeFormatter),
                                0L);

                        slot =
                                slot.plusHours(2);
                    }

                    break;

                case 2:

                    chartData.put("Monday", 0L);
                    chartData.put("Tuesday", 0L);
                    chartData.put("Wednesday", 0L);
                    chartData.put("Thursday", 0L);
                    chartData.put("Friday", 0L);
                    chartData.put("Saturday", 0L);
                    chartData.put("Sunday", 0L);

                    break;

                case 3:

                    LocalDate firstDayOfMonth =
                            LocalDate.now()
                                    .withDayOfMonth(1);

                    LocalDate lastDayOfMonth =
                            firstDayOfMonth.withDayOfMonth(
                                    firstDayOfMonth.lengthOfMonth());

                    int totalWeeks =
                            ((lastDayOfMonth.getDayOfMonth() - 1) / 7) + 1;

                    for (int i = 1; i <= totalWeeks; i++) {

                        chartData.put(
                                "Week " + i,
                                0L);
                    }

                    break;

                case 4:

                    chartData.put("Jan", 0L);
                    chartData.put("Feb", 0L);
                    chartData.put("Mar", 0L);
                    chartData.put("Apr", 0L);
                    chartData.put("May", 0L);
                    chartData.put("Jun", 0L);
                    chartData.put("Jul", 0L);
                    chartData.put("Aug", 0L);
                    chartData.put("Sep", 0L);
                    chartData.put("Oct", 0L);
                    chartData.put("Nov", 0L);
                    chartData.put("Dec", 0L);

                    break;

                case 5:

                    LocalDate customDate =
                            LocalDate.parse(startDate);

                    LocalDate customEnd =
                            LocalDate.parse(endDate);

                    DateTimeFormatter customFormatter =
                            DateTimeFormatter.ofPattern("MMM d");

                    while (!customDate.isAfter(customEnd)) {

                        chartData.put(
                                customDate.format(customFormatter),
                                0L);

                        customDate =
                                customDate.plusDays(1);
                    }

                    break;
            }

            long totalAppointments = 0;
            long completedCount = 0;
            long cancelledCount = 0;
            long missedCount = 0;

            Map<String, Map<String, Object>> practitionerMap =
                    new LinkedHashMap<>();

            LocalDate today = LocalDate.now();

            for (Map<String, Object> booking : bookings) {

                String serviceDateStr =
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        ""));

                if (serviceDateStr == null
                        || serviceDateStr.isBlank()) {
                    continue;
                }

                LocalDate serviceDate =
                        LocalDate.parse(
                                serviceDateStr);

                boolean include = false;

                switch (type) {

                    case 1:
                        include =
                                serviceDate.equals(today);
                        break;

                    case 2:
                        include =
                                !serviceDate.isBefore(
                                        today.minusDays(6))
                                && !serviceDate.isAfter(
                                        today);
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

                        LocalDate start =
                                LocalDate.parse(
                                        startDate);

                        LocalDate end =
                                LocalDate.parse(
                                        endDate);

                        include =
                                !serviceDate.isBefore(start)
                                && !serviceDate.isAfter(end);

                        break;
                }

                if (!include) {
                    continue;
                }

                totalAppointments++;

                String doctorId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorId",
                                        ""));

                String doctorName =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorName",
                                        "N/A"));

                String finalSpecialization =
                        doctorsRepository
                                .findByDoctorId(
                                        doctorId)
                                .map(
                                        Doctors::getSpecialization)
                                .filter(
                                        specialization -> specialization != null
                                                && !specialization.isBlank())
                                .orElse(
                                        "N/A");

                Map<String, Object> practitioner =
                        practitionerMap.computeIfAbsent(
                                doctorId,
                                key -> {

                                    Map<String, Object> map =
                                            new HashMap<>();

                                    map.put(
                                            "id",
                                            doctorId);

                                    map.put(
                                            "doctor",
                                            doctorName);

                                    map.put(
                                            "specialty",
                                            finalSpecialization);

                                    map.put(
                                            "total",
                                            0L);

                                    map.put(
                                            "completed",
                                            0L);

                                    map.put(
                                            "cancelled",
                                            0L);

                                    return map;
                                });

                practitioner.put(
                        "total",
                        ((Long) practitioner.get(
                                "total")) + 1);
               
                String label = "";

                switch (type) {

                    case 1:

                        String serviceTimeStr =
                                String.valueOf(
                                        booking.getOrDefault(
                                                "servicetime",
                                                ""));

                        if (!serviceTimeStr.isBlank()) {

                            LocalTime serviceTime =
                                    LocalTime.parse(
                                            serviceTimeStr,
                                            timeFormatter);

                            LocalTime currentSlot =
                                    openingTime;

                            while (currentSlot.isBefore(
                                    closingTime)) {

                                LocalTime nextSlot =
                                        currentSlot.plusHours(
                                                2);

                                if ((serviceTime.equals(
                                        currentSlot)
                                        || serviceTime.isAfter(
                                        currentSlot))
                                        && serviceTime.isBefore(
                                        nextSlot)) {

                                    label =
                                            currentSlot.format(
                                                    timeFormatter);

                                    break;
                                }

                                currentSlot =
                                        nextSlot;
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
                                ((serviceDate.getDayOfMonth() - 1) / 7)
                                        + 1;

                        label =
                                "Week " + week;

                        break;

                    case 4:

                        label =
                                serviceDate.getMonth()
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.ENGLISH);

                        break;

                    case 5:

                        DateTimeFormatter customFormatter =
                                DateTimeFormatter.ofPattern("MMM d");

                        label =
                                serviceDate.format(customFormatter);

                        break;   
                        
                }

                if (chartData.containsKey(
                        label)) {

                    chartData.put(
                            label,
                            chartData.get(
                                    label) + 1);
                }

                String bookingId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "bookingId",
                                        ""));

                String status =
                        String.valueOf(
                                booking.getOrDefault(
                                        "status",
                                        ""));

                String followupStatus =
                        String.valueOf(
                                booking.getOrDefault(
                                        "followupStatus",
                                        ""));

                boolean cancelled =
                        "cancelled".equalsIgnoreCase(
                                status)
                                || "cancelled".equalsIgnoreCase(
                                followupStatus);

//                boolean paymentCompleted =
//                        false;
//
//                try {
//
//                    Response paymentResponse =
//                            physiotherapyFeignClient
//                                    .getPayment(
//                                            bookingId);
//
//                    if (paymentResponse != null
//                            && paymentResponse.getData() != null) {
//
//                        Map<String, Object> payment =
//                                (Map<String, Object>) paymentResponse
//                                        .getData();
//
//                        String overallStatus =
//                                String.valueOf(
//                                        payment.getOrDefault(
//                                                "overallStatus",
//                                                ""));
//
//                        paymentCompleted =
//                                "completed".equalsIgnoreCase(
//                                        overallStatus);
//                    }
//
//                } catch (Exception e) {
//
//                    System.out.println(
//                            "Payment not found for bookingId : "
//                                    + bookingId);
//                }

                boolean paymentFound = false;
                boolean paymentCompleted = false;
                try {

                    Response paymentResponse =
                            physiotherapyFeignClient
                                    .getPayment(
                                            bookingId);

                    if (paymentResponse != null
                            && paymentResponse.getData() != null) {

                        paymentFound = true;

                        Map<String, Object> payment =
                                (Map<String, Object>) paymentResponse
                                        .getData();

                        String overallStatus =
                                String.valueOf(
                                        payment.getOrDefault(
                                                "overallStatus",
                                                ""));

                        paymentCompleted =
                                "completed".equalsIgnoreCase(
                                        overallStatus);
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Payment not found for bookingId : "
                                    + bookingId);
                }

                boolean bookingCompleted =
                        "completed".equalsIgnoreCase(status)
                        || "completed".equalsIgnoreCase(followupStatus);

                if (bookingCompleted && paymentCompleted) {

                    completedCount++;

                    practitioner.put(
                            "completed",
                            ((Long) practitioner.get(
                                    "completed")) + 1);
                }
                else if (bookingCompleted && !paymentCompleted) {

                    missedCount++;
                }

                if (cancelled) {

                    cancelledCount++;

                    practitioner.put(
                            "cancelled",
                            ((Long) practitioner.get(
                                    "cancelled")) + 1);
                
                }
            }
            long bookedCount =
                    totalAppointments
                            - cancelledCount;

            Map<String, Object> summary =
                    new HashMap<>();

            summary.put(
                    "totalAppointments",
                    totalAppointments);

            summary.put(
                    "completed",
                    completedCount);

            summary.put(
                    "cancelled",
                    cancelledCount);

            summary.put(
                    "missed",
                    missedCount);

            summary.put(
                    "booked",
                    bookedCount);

            Map<String, Object> trendData =
                    new HashMap<>();

            trendData.put(
                    "seriesLabels",
                    new ArrayList<>(
                            chartData.keySet()));

            trendData.put(
                    "appointmentVolumes",
                    new ArrayList<>(
                            chartData.values()));

            List<Map<String, Object>> practitioners =
                    new ArrayList<>(
                            practitionerMap.values());

            Map<String, Object> dashboard =
                    new HashMap<>();

            dashboard.put(
                    "summary",
                    summary);

            dashboard.put(
                    "practitioners",
                    practitioners);

            dashboard.put(
                    "trendData",
                    trendData);
            response.setSuccess(true);
            response.setMessage(
                    "Appointment analytics fetched successfully");
            response.setData(
                    dashboard);
            response.setStatus(
                    HttpStatus.OK.value());

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage(
                    e.getMessage());
            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
    @Override
    public Response getAppointmentSummary(
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

            if (bookingResponse == null
                    || bookingResponse.getBody() == null
                    || bookingResponse.getBody().getData() == null) {

                response.setSuccess(false);
                response.setMessage("No booking data found");
                response.setStatus(404);

                return response;
            }

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            LocalDate today = LocalDate.now();

            long totalAppointments = 0;
            long completedCount = 0;
            long cancelledCount = 0;
            long missedCount = 0;

            for (Map<String, Object> booking : bookings) {

                String serviceDateStr =
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        ""));

                if (serviceDateStr.isBlank()) {
                    continue;
                }

                LocalDate serviceDate =
                        LocalDate.parse(serviceDateStr);

                boolean include = false;

                switch (type) {

                    case 1:
                        include = serviceDate.equals(today);
                        break;

                    case 2:
                        include =
                                !serviceDate.isBefore(today.minusDays(6))
                                && !serviceDate.isAfter(today);
                        break;

                    case 3:
                        include =
                                serviceDate.getMonthValue() == today.getMonthValue()
                                && serviceDate.getYear() == today.getYear();
                        break;

                    case 4:
                        include =
                                serviceDate.getYear() == today.getYear();
                        break;

                    case 5:

                        LocalDate start =
                                LocalDate.parse(startDate);

                        LocalDate end =
                                LocalDate.parse(endDate);

                        include =
                                !serviceDate.isBefore(start)
                                && !serviceDate.isAfter(end);

                        break;
                }

                if (!include) {
                    continue;
                }

                totalAppointments++;

                String bookingId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "bookingId",
                                        ""));

                String status =
                        String.valueOf(
                                booking.getOrDefault(
                                        "status",
                                        ""));

                String followupStatus =
                        String.valueOf(
                                booking.getOrDefault(
                                        "followupStatus",
                                        ""));

//                boolean completed =
//                        "completed".equalsIgnoreCase(status)
//                        || "completed".equalsIgnoreCase(followupStatus);

                boolean cancelled =
                        "cancelled".equalsIgnoreCase(status)
                        || "cancelled".equalsIgnoreCase(followupStatus);

//                if (completed) {
//                    completedCount++;
//                }

                if (cancelled) {
                    cancelledCount++;
                }

                boolean paymentFound = false;
                boolean paymentCompleted = false;

                try {

                    Response paymentResponse =
                            physiotherapyFeignClient
                                    .getPayment(bookingId);

                    if (paymentResponse != null
                            && paymentResponse.getData() != null) {

                        Map<String, Object> payment =
                                (Map<String, Object>) paymentResponse.getData();

                        String overallStatus =
                                String.valueOf(
                                        payment.getOrDefault(
                                                "overallStatus",
                                                ""));

                        paymentCompleted =
                                "completed".equalsIgnoreCase(
                                        overallStatus);
                    }

                } catch (Exception e) {
                }

//                if (!completed
//                        && !cancelled
//                        && !paymentCompleted) {
//
//                    missedCount++;
//                }
            }

            AppointmentSummaryDTO dto =
                    new AppointmentSummaryDTO();

            dto.setTotalAppointments(totalAppointments);
            dto.setCompleted(completedCount);
            dto.setCancelled(cancelledCount);
            dto.setMissed(missedCount);

            response.setSuccess(true);
            response.setMessage(
                    "Appointment summary fetched successfully");
            response.setData(dto);
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(500);
        }

        return response;
    }
    private LocalTime parseClinicTime(String timeStr) {

        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }

        timeStr = timeStr.trim();

        try {

            return LocalTime.parse(timeStr);

        } catch (Exception e) {

            try {

                return LocalTime.parse(
                        timeStr,
                        DateTimeFormatter.ofPattern(
                                "hh:mm a"));

            } catch (Exception ex) {

                try {

                    return LocalTime.parse(
                            timeStr.toUpperCase(),
                            DateTimeFormatter.ofPattern(
                                    "h:mm a"));

                } catch (Exception exception) {

                    throw new RuntimeException(
                            "Invalid time format : "
                                    + timeStr);
                }
            }
        }
    }
}