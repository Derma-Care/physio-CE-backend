package com.clinicadmin.service.impl;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.service.DashboardAnalyticsService;
import com.clinicadmin.service.ExpensesService;
import com.clinicadmin.service.PatientAnalyticsService;
import com.clinicadmin.service.TreatmentAnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    private final BookingFeign bookingFeign;
    private final PatientAnalyticsService patientAnalyticsService;
    private final TreatmentAnalyticsService treatmentAnalyticsService;
    private final ObjectMapper mapper;
    private final PhysiotherapyFeignClient physiotherapyFeignClient;
    
    @Autowired
    private CustomerOnboardingRepository customerOnboardingRepository;
    
    @Autowired
    private ExpensesService expensesService;

    @Override

    public Response getDashboard(
            String clinicId,
            String branchId) {

        Response response = new Response();

        try {

            Map<String, Object> dashboard =
                    new LinkedHashMap<>();

         // =====================================================
         // REVENUE SUMMARY
         // =====================================================

            double totalRevenue = 0;
            double previousMonthRevenue = 0;
            double revenueTrend = 0;
            String revenueTrendType = "UP";

            ResponseEntity<Response> revenueResponse =
                    physiotherapyFeignClient.getRevenueSummary(
                            clinicId,
                            branchId);

            if (revenueResponse != null
                    && revenueResponse.getBody() != null
                    && revenueResponse.getBody().getData() != null) {

                Map<String, Object> revenueData =
                        mapper.convertValue(
                                revenueResponse.getBody().getData(),
                                Map.class);

                totalRevenue =
                        ((Number) revenueData.getOrDefault(
                                "lastMonthRevenue",
                                0.0))
                                .doubleValue();

                previousMonthRevenue =
                        ((Number) revenueData.getOrDefault(
                                "lastWeekRevenue",
                                0.0))
                                .doubleValue();

                if (previousMonthRevenue > 0) {

                    revenueTrend =
                            ((totalRevenue - previousMonthRevenue)
                                    / previousMonthRevenue) * 100;

                    revenueTrendType =
                            revenueTrend >= 0
                                    ? "UP"
                                    : "DOWN";

                    revenueTrend =
                            Math.abs(revenueTrend);
                }
            }
            // =====================================================
            // BOOKINGS / APPOINTMENTS
            // =====================================================

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> bookingResponse =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(
                            clinicId,
                            branchId);

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody() != null
                            ? bookingResponse.getBody().getData()
                            : new ArrayList<>();
            long totalTodayAppointments = 0;
            long todayAppointments = 0;
            long pendingAppointments = 0;

            String today = LocalDate.now().toString();

            for (Map<String, Object> booking : bookings) {

                String serviceDate =
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        ""));

                String status =
                        String.valueOf(
                                booking.getOrDefault(
                                        "status",
                                        ""));

                if (today.equals(serviceDate)) {

                    // All today's appointments
                    totalTodayAppointments++;

                    // Today's confirmed appointments
                    if ("confirmed".equalsIgnoreCase(status)) {

                        todayAppointments++;
                    }

                    // Today's pending appointments
                    if ("pending".equalsIgnoreCase(status)) {

                        pendingAppointments++;
                    }
                }
            }
         // =====================================================
         // ACTIVE PATIENTS / NEW PATIENTS
         // =====================================================

         long totalPatients = 0;
         long newPatients = 0;
         long activePatients = 0;

         List<CustomerOnbording> customers =
                 customerOnboardingRepository
                         .findByHospitalIdAndBranchId(
                                 clinicId,
                                 branchId);

         totalPatients = customers.size();

         LocalDate currentDate =
                 LocalDate.now();

         for (CustomerOnbording customer : customers) {

             boolean hasDeviceId =
                     customer.getDeviceId() != null
                     && !customer.getDeviceId().isBlank();

             // Active Patients
             if (hasDeviceId) {

                 activePatients++;
             }

             // New Patients = Current Month + Device ID Present
             if (hasDeviceId
            	        && customer.getCreatedAt() != null
            	        && !customer.getCreatedAt().isBlank()) {

            	    LocalDate createdDate = null;

            	    try {

            	        createdDate =
            	                LocalDateTime.parse(
            	                        customer.getCreatedAt())
            	                        .toLocalDate();

            	    } catch (Exception e) {

            	        try {

            	            DateTimeFormatter formatter =
            	                    DateTimeFormatter.ofPattern(
            	                            "dd/MM/yyyy hh:mm:ss a");

            	            createdDate =
            	                    LocalDateTime.parse(
            	                            customer.getCreatedAt(),
            	                            formatter)
            	                            .toLocalDate();

            	        } catch (Exception ex) {

//            	            log.warn(
//            	                    "Unable to parse createdAt: {}",
//            	                    customer.getCreatedAt());
            	        }
            	    }

            	    if (createdDate != null
            	            && createdDate.getMonthValue()
            	                    == currentDate.getMonthValue()
            	            && createdDate.getYear()
            	                    == currentDate.getYear()) {

            	        newPatients++;
            	    }
            	
                 }
             }
         
         // =====================================================
         // TREATMENT ANALYTICS
         // =====================================================

         double successRate = 0;
         double lastMonthSuccessRate = 0;
         double successTrend = 0;
         String successTrendType = "UP";

         Response treatmentResponse =
                 treatmentAnalyticsService.getTreatmentAnalytics(
                         clinicId,
                         branchId,
                         "all",
                         "4");

         if (treatmentResponse != null
                 && treatmentResponse.getData() != null) {

             Map<String, Object> treatmentData =
                     mapper.convertValue(
                             treatmentResponse.getData(),
                             Map.class);

             successRate =
                     ((Number) treatmentData.getOrDefault(
                             "thisMonthSuccessRate",
                             treatmentData.getOrDefault(
                                     "avgSuccessRate",
                                     0.0)))
                             .doubleValue();

             lastMonthSuccessRate =
                     ((Number) treatmentData.getOrDefault(
                             "lastMonthSuccessRate",
                             0.0))
                             .doubleValue();

             if (lastMonthSuccessRate > 0) {

                 successTrend =
                         ((successRate - lastMonthSuccessRate)
                                 / lastMonthSuccessRate) * 100;

                 successTrendType =
                         successTrend >= 0
                                 ? "UP"
                                 : "DOWN";

                 successTrend =
                         Math.abs(successTrend);
             }
         }
      // =====================================================
      // KPI SUMMARY
      // =====================================================

      Map<String, Object> kpiSummary =
              new LinkedHashMap<>();

      kpiSummary.put(
              "monthlyRevenue",
              Map.of(
                      "value", totalRevenue,
                      "currency", "INR",
                      "trend",
                      Math.round(revenueTrend * 100.0) / 100.0,
                      "trendType",
                      revenueTrendType));

      kpiSummary.put(
              "activePatients",
              Map.of(
                      "value", activePatients,
                      "newPatients", newPatients));

      kpiSummary.put(
              "todayAppointments",
              Map.of(
                      "value", todayAppointments,
                      "pending", pendingAppointments));

      kpiSummary.put(
              "treatmentSuccessRate",
              Map.of(
                      "value",
                      Math.round(successRate * 100.0) / 100.0,
                      "trend",
                      Math.round(successTrend * 100.0) / 100.0,
                      "trendType",
                      successTrendType));

      dashboard.put(
              "kpiSummary",
              kpiSummary);
   // =====================================================
   // EXPENSE ANALYTICS
   // =====================================================

   double monthlyExpense =
           expensesService.getMonthlyExpenses(
                   clinicId,
                   branchId);

   // =====================================================
   // MODULES
   // =====================================================

   String revenueMetric =
           String.format(
                   "₹%.2fL this month",
                   totalRevenue / 100000.0);

   String expenseMetric =
           String.format(
                   "₹%.2fL this month",
                   monthlyExpense / 100000.0);

   String patientMetric =
           String.format(
                   "%,d active patients",
                   activePatients);

   String appointmentMetric =
           totalTodayAppointments + " scheduled today";

   String treatmentMetric =
           Math.round(successRate) + "% success rate";

   // Referral Count
   long totalReferrals =
           bookings.stream()
                   .filter(b -> {
                       String referralId =
                               String.valueOf(
                                       b.getOrDefault(
                                               "referredDoctorId",
                                               ""))
                                       .trim();

                       return !referralId.isBlank()
                               && !"null".equalsIgnoreCase(
                                       referralId);
                   })
                   .count();

   String referralMetric =
           totalReferrals + " referrals · 30d";

   List<Map<String, Object>> modules =
           new ArrayList<>();

   modules.add(
           Map.of(
                   "id", 1,
                   "title", "Revenue Analytics",
                   "description",
                   "View daily, weekly, and monthly revenue data.",
                   "metric", revenueMetric,
                   "route", "/analytics/revenue",
                   "icon", "revenue"));

   modules.add(
           Map.of(
                   "id", 2,
                   "title", "Expense Analytics",
                   "description",
                   "Track clinic expenses and payouts.",
                   "metric", expenseMetric,
                   "route", "/expenses",
                   "icon", "expense"));

   modules.add(
           Map.of(
                   "id", 3,
                   "title", "Referral Analytics",
                   "description",
                   "Monitor patient referrals and sources.",
                   "metric", referralMetric,
                   "route", "/analytics/referrals",
                   "icon", "referral"));

   modules.add(
           Map.of(
                   "id", 4,
                   "title", "Patient Analytics",
                   "description",
                   "Analyze patient demographics and trends.",
                   "metric", patientMetric,
                   "route", "/analytics/patients",
                   "icon", "patient"));

   modules.add(
           Map.of(
                   "id", 5,
                   "title", "Appointment Analytics",
                   "description",
                   "Review appointment volumes and status.",
                   "metric", appointmentMetric,
                   "route", "/analytics/appointments",
                   "icon", "appointment"));

   modules.add(
           Map.of(
                   "id", 6,
                   "title", "Treatment Analytics",
                   "description",
                   "Track popular treatments and success rates.",
                   "metric", treatmentMetric,
                   "route", "/analytics/treatments",
                   "icon", "treatment"));

   dashboard.put("modules", modules);
            response.setSuccess(true);
            response.setMessage(
                    "Analytics dashboard data fetched successfully");
            response.setData(dashboard);
            response.setStatus(200);

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage(
                    "Failed to fetch dashboard data : " + e.getMessage());
            response.setStatus(500);
        }

        return response;
    }
}