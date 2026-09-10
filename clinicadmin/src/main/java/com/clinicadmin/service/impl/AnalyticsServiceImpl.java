package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.CustomerOnbordingDTO;
import com.clinicadmin.dto.DoctorReferralAnalyticsDTO;
import com.clinicadmin.dto.DoctorReferralPatientDTO;
import com.clinicadmin.dto.ReferralChannelDTO;
import com.clinicadmin.dto.ReferralChannelPatientDTO;
import com.clinicadmin.dto.ReferralSummaryDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ResponseStructure;
import com.clinicadmin.dto.TopReferringDoctorDTO;
import com.clinicadmin.entity.ReferredDoctor;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.repository.ReferredDoctorRepository;
import com.clinicadmin.service.AnalyticsService;
import com.clinicadmin.service.CustomerOnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private BookingFeign bookingFeign;
    
    @Autowired
    private CustomerOnboardingService customerOnboardingService;
    
    @Autowired
    
    private PhysiotherapyFeignClient PhysiotherapyFeignClient;

    @Autowired
    private ReferredDoctorRepository referredDoctorRepository;

    @Override
    public Response getDoctorReferralAnalytics(
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

            Map<String, DoctorReferralAnalyticsDTO> doctorAnalytics =
                    new LinkedHashMap<>();


            // =========================================================
            // LOOP BOOKINGS
            // =========================================================

            for (Map<String, Object> booking : bookings) {


                // ================= SERVICE DATE =================

                String serviceDateStr =
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        "")).trim();

                if (serviceDateStr.isBlank()
                        || "null".equalsIgnoreCase(serviceDateStr)) {

                    continue;
                }

                LocalDate serviceDate;

                try {

                    serviceDate =
                            LocalDate.parse(serviceDateStr);

                } catch (Exception e) {

                    // Skip booking if serviceDate is invalid
                    continue;
                }


                // ================= DATE FILTER =================

                boolean include = false;

                switch (type) {

                case 1: // Today

                    include =
                            serviceDate.equals(today);

                    break;


                case 2: // Weekly - Last 7 days including today

                    include =
                            !serviceDate.isBefore(
                                    today.minusDays(6))
                            &&
                            !serviceDate.isAfter(today);

                    break;


                case 3: // Monthly

                    include =
                            serviceDate.getMonthValue()
                                    == today.getMonthValue()
                            &&
                            serviceDate.getYear()
                                    == today.getYear();

                    break;


                case 4: // Yearly

                    include =
                            serviceDate.getYear()
                                    == today.getYear();

                    break;


                case 5: // Custom Date Range

                    if (startDate == null
                            || startDate.isBlank()
                            || endDate == null
                            || endDate.isBlank()) {

                        continue;
                    }

                    LocalDate start =
                            LocalDate.parse(startDate);

                    LocalDate end =
                            LocalDate.parse(endDate);

                    include =
                            !serviceDate.isBefore(start)
                            &&
                            !serviceDate.isAfter(end);

                    break;


                default:

                    include = false;
                    break;
                }


                if (!include) {

                    continue;
                }


                // =========================================================
                // REFERRED DOCTOR ID
                // =========================================================

                String referralId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredDoctorId",
                                        "")).trim();


                // If referredDoctorId is empty/null,
                // it is NOT a doctor referral.
                //
                // This skips:
                // Self
                // Friend
                // Family
                // Facebook
                // Instagram
                // Google
                // Other Sources
                // Others

                if (referralId.isBlank()
                        || "null".equalsIgnoreCase(referralId)) {

                    continue;
                }


                // =========================================================
                // CHECK REGISTERED REFERRED DOCTOR
                // =========================================================

                ReferredDoctor referredDoctor =
                        referredDoctorRepository
                                .findByReferralId(
                                        referralId)
                                .orElse(null);


                // If referralId does not exist in
                // ReferredDoctor collection, skip it.

                if (referredDoctor == null) {

                    continue;
                }


                // =========================================================
                // CALCULATE TOTAL FEE
                // =========================================================

                double totalFee = 0.0;

                Object totalFeeObj =
                        booking.get("totalFee");

                if (totalFeeObj != null) {

                    try {

                        totalFee =
                                Double.parseDouble(
                                        totalFeeObj.toString());

                    } catch (NumberFormatException e) {

                        totalFee = 0.0;

                        System.out.println(
                                "Invalid totalFee value: "
                                        + totalFeeObj);
                    }
                }


                // =========================================================
                // CALCULATE CONSULTATION FEE
                // =========================================================

                double consultationFee = 0.0;

                Object consultationFeeObj =
                        booking.get("consultationFee");

                if (consultationFeeObj != null) {

                    try {

                        consultationFee =
                                Double.parseDouble(
                                        consultationFeeObj.toString());

                    } catch (NumberFormatException e) {

                        consultationFee = 0.0;

                        System.out.println(
                                "Invalid consultationFee value: "
                                        + consultationFeeObj);
                    }
                }


                // =========================================================
                // TOTAL REVENUE
                // =========================================================

                double revenue =
                        totalFee + consultationFee;


                // =========================================================
                // CREATE / GET DOCTOR ANALYTICS
                // =========================================================

                DoctorReferralAnalyticsDTO dto =
                        doctorAnalytics.computeIfAbsent(
                                referralId,
                                id -> {

                                    DoctorReferralAnalyticsDTO analytics =
                                            new DoctorReferralAnalyticsDTO();


                                    // Referral Doctor ID

                                    analytics.setReferralId(
                                            referralId);


                                    // Doctor Name

                                    analytics.setDoctorName(
                                            referredDoctor.getFullName());


                                    // Hospital / Clinic Name

                                    analytics.setClinicHospitalName(
                                            referredDoctor
                                                    .getCurrentHospitalName());


                                    // Specialization

                                    analytics.setSpecialization(
                                            referredDoctor
                                                    .getSpecialization());


                                    // Contact Number

                                    analytics.setContactInfo(
                                            referredDoctor
                                                    .getMobileNumber());


                                    // Initial values

                                    analytics.setPatientsReferred(0);

                                    analytics.setRevenueGenerated(0.0);


                                    return analytics;
                                });


                // =========================================================
                // INCREMENT PATIENT COUNT
                // =========================================================

                dto.setPatientsReferred(
                        dto.getPatientsReferred() + 1);


                // =========================================================
                // ADD REVENUE
                // =========================================================

                dto.setRevenueGenerated(
                        dto.getRevenueGenerated()
                                + revenue);
            }


            // =============================================================
            // RESPONSE
            // =============================================================

            response.setSuccess(true);

            response.setData(
                    new ArrayList<>(
                            doctorAnalytics.values()));

            response.setMessage(
                    "Doctor referral analytics fetched successfully");

            response.setStatus(200);


        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage());

            response.setStatus(500);
        }

        return response;
    }
    
    @Override
    public Response getDoctorReferralPatientDetails(
            String clinicId,
            String branchId,
            String referralId) {

        Response response = new Response();

        try {

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> bookingResponse =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(
                            clinicId,
                            branchId);

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            List<DoctorReferralPatientDTO> result =
                    new ArrayList<>();

            for (Map<String, Object> booking : bookings) {

            	String referredDoctorId =
            	        String.valueOf(
            	                booking.getOrDefault(
            	                        "referredDoctorId",
            	                        ""))
            	                .trim();

            	if (referralId == null
            	        || referralId.isBlank()
            	        || referredDoctorId.isBlank()
            	        || "null".equalsIgnoreCase(referredDoctorId)
            	        || !referralId.trim().equalsIgnoreCase(referredDoctorId)) {

            	    continue;
            	}

                String bookingId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "bookingId",
                                        ""));

                String patientId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "patientId",
                                        ""));

                Map<String, Object> payment = new HashMap<>();

                try {

                    Response paymentResponse =
                            PhysiotherapyFeignClient.getPayment(bookingId);

                    if (paymentResponse != null
                            && paymentResponse.getData() != null) {

                        payment =
                                (Map<String, Object>)
                                        paymentResponse.getData();
                    }

                } catch (Exception e) {

                    // No payment record found.
                    // Still include the referred patient.
                    System.out.println(
                            "Payment not found for bookingId: "
                                    + bookingId);
                }

                String patientName = "";
                String contactNumber = "";

                try {

                    Response customerResponse =
                            customerOnboardingService
                                    .getCustomersByPatientId(
                                            patientId,
                                            clinicId);

                    if (customerResponse != null
                            && customerResponse.getData() != null) {

                        CustomerOnbordingDTO customer =
                                new ObjectMapper()
                                        .convertValue(
                                                customerResponse.getData(),
                                                CustomerOnbordingDTO.class);

                        patientName =
                                customer.getFullName();

                        contactNumber =
                                customer.getMobileNumber();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                DoctorReferralPatientDTO dto =
                        new DoctorReferralPatientDTO();

                dto.setPatientName(patientName);
                dto.setContactNumber(contactNumber);
                dto.setBookingId(bookingId);
                dto.setServiceDate(
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        "")));

                dto.setServiceTime(
                        String.valueOf(
                                booking.getOrDefault(
                                        "servicetime",
                                        "")));
                dto.setDateOfVisit(
                        String.valueOf(
                        		payment.getOrDefault(
                                        "sessionStartDate",
                                        "")));

                dto.setServiceName(
                        String.valueOf(
                                payment.getOrDefault(
                                        "treatmentName",
                                        "")));

                dto.setServiceType(
                        String.valueOf(
                                payment.getOrDefault(
                                        "serviceType",
                                        "")));

                dto.setStatus(
                        String.valueOf(
                                payment.getOrDefault(
                                        "overallStatus",
                                        "")));

                dto.setTotalCost(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "finalAmount",
                                                0))));

                dto.setPaidAmount(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "totalPaid",
                                                0))));

                dto.setPendingAmount(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "balanceAmount",
                                                0))));

                result.add(dto);
            }

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Referral patient details fetched successfully");
            response.setData(result);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
    @Override
    public Response getReferralChannels(
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

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            LocalDate today = LocalDate.now();

            Map<String, ReferralChannelDTO> channelMap =
                    new LinkedHashMap<>();

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
                        LocalDate.parse(serviceDateStr);

                boolean include = false;

                switch (type) {

                case 1: // Today
                    include = serviceDate.equals(today);
                    break;

                case 2: // Weekly
                    include =
                            !serviceDate.isBefore(
                                    today.minusDays(6))
                            && !serviceDate.isAfter(today);
                    break;

                case 3: // Monthly
                    include =
                            serviceDate.getMonthValue()
                                    == today.getMonthValue()
                            && serviceDate.getYear()
                                    == today.getYear();
                    break;

                case 4: // Yearly
                    include =
                            serviceDate.getYear()
                                    == today.getYear();
                    break;

                case 5: // Custom

                    LocalDate start =
                            LocalDate.parse(startDate);

                    LocalDate end =
                            LocalDate.parse(endDate);

                    include =
                            !serviceDate.isBefore(start)
                            && !serviceDate.isAfter(end);

                    break;

                default:
                    include = false;
                }

                if (!include) {
                    continue;
                }

                String referralId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredDoctorId",
                                        "")).trim();

                String doctorRefCode =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorRefCode",
                                        "")).trim();

                String referredByType =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredByType",
                                        "")).trim();

                String referredByName =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredByName",
                                        "")).trim();

                String channel;

                boolean hasReferralId =
                        !referralId.isBlank()
                        && !"null".equalsIgnoreCase(referralId);

                boolean hasDoctorRefCode =
                        !doctorRefCode.isBlank()
                        && !"null".equalsIgnoreCase(doctorRefCode);

                boolean hasReferredByType =
                        !referredByType.isBlank()
                        && !"null".equalsIgnoreCase(referredByType);

                boolean hasReferredByName =
                        !referredByName.isBlank()
                        && !"null".equalsIgnoreCase(referredByName);


                // ================= REMOVE DOCTOR REFERRALS =================

                if (hasReferralId) {

                    // Doctor referrals should not be shown
                    // in Referral Channel Analytics
                    continue;
                }


                // ================= SELF =================

                if ("Self".equalsIgnoreCase(doctorRefCode)) {

                    channel = "Self";


                // ================= ACTUAL REFERRAL CHANNEL =================

                } else if (hasReferredByType) {

                    // Friend, Family, Instagram,
                    // Facebook, Google, Website, etc.
                    channel = referredByType;


                // ================= FALLBACK =================

                } else if (hasReferredByName) {

                    channel = referredByName;


                // ================= ALL EMPTY = SELF =================

                } else if (!hasDoctorRefCode
                        && !hasReferredByType
                        && !hasReferredByName) {

                    channel = "Self";

                } else {

                    channel = "Others";
                }

//                String referredByName =
//                        String.valueOf(
//                                booking.getOrDefault(
//                                        "referredByName",
//                                        ""));

                double totalFee = 0.0;
                double consultationFee = 0.0;

                // ================= TOTAL FEE =================

                Object totalFeeObj =
                        booking.get("totalFee");

                if (totalFeeObj != null) {

                    try {

                        totalFee =
                                Double.parseDouble(
                                        totalFeeObj.toString());

                    } catch (NumberFormatException e) {

                        totalFee = 0.0;

                        System.out.println(
                                "Invalid totalFee value: "
                                        + totalFeeObj);
                    }
                }


                // ================= CONSULTATION FEE =================

                Object consultationFeeObj =
                        booking.get("consultationFee");

                if (consultationFeeObj != null) {

                    try {

                        consultationFee =
                                Double.parseDouble(
                                        consultationFeeObj.toString());

                    } catch (NumberFormatException e) {

                        consultationFee = 0.0;

                        System.out.println(
                                "Invalid consultationFee value: "
                                        + consultationFeeObj);
                    }
                }


                // ================= REVENUE =================

                double revenue =
                        totalFee + consultationFee;

                ReferralChannelDTO dto =
                        channelMap.computeIfAbsent(
                                channel,
                                k -> {
                                    ReferralChannelDTO r =
                                            new ReferralChannelDTO();

                                    r.setChannel(k);
//                                    r.setReferredByName(
//                                            referredByName);

                                    r.setPatientsReferred(0L);
                                    r.setRevenueGenerated(0.0);

                                    return r;
                                });

                dto.setPatientsReferred(
                        dto.getPatientsReferred() + 1);

                dto.setRevenueGenerated(
                        dto.getRevenueGenerated() + revenue);
            }

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Referral channel analytics fetched successfully");
            response.setData(
                    new ArrayList<>(channelMap.values()));

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
    @Override
    public Response getReferralChannelPatientDetails(
            String clinicId,
            String branchId,
            String channel) {

        Response response = new Response();

        try {

            ResponseEntity<ResponseStructure<List<Map<String, Object>>>> bookingResponse =
                    bookingFeign.getBookedServicesByClinicIdWithBranchId(
                            clinicId,
                            branchId);

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            List<ReferralChannelPatientDTO> result =
                    new ArrayList<>();

            for (Map<String, Object> booking : bookings) {

            	String referralId =
            	        String.valueOf(
            	                booking.getOrDefault(
            	                        "referredDoctorId",
            	                        "")).trim();

            	String doctorRefCode =
            	        String.valueOf(
            	                booking.getOrDefault(
            	                        "doctorRefCode",
            	                        "")).trim();

            	String referredByType =
            	        String.valueOf(
            	                booking.getOrDefault(
            	                        "referredByType",
            	                        "")).trim();

            	String referredByName =
            	        String.valueOf(
            	                booking.getOrDefault(
            	                        "referredByName",
            	                        "")).trim();

            	String derivedChannel;

            	// Check valid values
            	boolean hasReferralId =
            	        !referralId.isBlank()
            	        && !"null".equalsIgnoreCase(referralId);

            	boolean hasDoctorRefCode =
            	        !doctorRefCode.isBlank()
            	        && !"null".equalsIgnoreCase(doctorRefCode);

            	boolean hasReferredByType =
            	        !referredByType.isBlank()
            	        && !"null".equalsIgnoreCase(referredByType);

            	boolean hasReferredByName =
            	        !referredByName.isBlank()
            	        && !"null".equalsIgnoreCase(referredByName);


            	// ================= DOCTOR REFERRAL =================

            	if (hasReferralId) {

            	    derivedChannel = "Doctor Referral";


            	// ================= EXPLICIT SELF =================

            	} else if ("Self".equalsIgnoreCase(doctorRefCode)) {

            	    derivedChannel = "Self";


            	// ================= FRIEND / FACEBOOK / INSTAGRAM ETC =================

            	} else if (hasReferredByType) {

            	    derivedChannel = referredByType;


            	// ================= FALLBACK TO REFERRED BY NAME =================

            	} else if (hasReferredByName) {

            	    derivedChannel = referredByName;


            	// ================= ALL EMPTY = SELF =================

            	} else if (!hasDoctorRefCode
            	        && !hasReferredByType
            	        && !hasReferredByName) {

            	    derivedChannel = "Self";

            	} else {

            	    derivedChannel = "Others";
            	}


            	// Match requested channel
            	if (!channel.equalsIgnoreCase(derivedChannel)) {
            	    continue;
            	}

                String bookingId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "bookingId",
                                        ""));

                String patientId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "patientId",
                                        ""));

                Map<String, Object> payment =
                        new HashMap<>();

                try {

                    Response paymentResponse =
                            PhysiotherapyFeignClient
                                    .getPayment(
                                            bookingId);

                    if (paymentResponse != null
                            && paymentResponse.getData() != null) {

                        payment =
                                (Map<String, Object>)
                                        paymentResponse.getData();
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Payment not found for bookingId: "
                                    + bookingId);
                }

                String patientName = "";
                String contactNumber = "";

                try {

                    Response customerResponse =
                            customerOnboardingService
                                    .getCustomersByPatientId(
                                            patientId,
                                            clinicId);

                    if (customerResponse != null
                            && customerResponse.getData() != null) {

                        CustomerOnbordingDTO customer =
                                new ObjectMapper()
                                        .convertValue(
                                                customerResponse.getData(),
                                                CustomerOnbordingDTO.class);

                        patientName =
                                customer.getFullName();

                        contactNumber =
                                customer.getMobileNumber();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                ReferralChannelPatientDTO dto =
                        new ReferralChannelPatientDTO();

                dto.setPatientName(patientName);
                dto.setContactNumber(contactNumber);
                dto.setBookingId(bookingId);

                dto.setServiceDate(
                        String.valueOf(
                                booking.getOrDefault(
                                        "serviceDate",
                                        "")));

                dto.setServiceTime(
                        String.valueOf(
                                booking.getOrDefault(
                                        "servicetime",
                                        "")));

                dto.setServiceName(
                        String.valueOf(
                                payment.getOrDefault(
                                        "treatmentName",
                                        "")));

                dto.setServiceType(
                        String.valueOf(
                                payment.getOrDefault(
                                        "serviceType",
                                        "")));

                dto.setStatus(
                        String.valueOf(
                                payment.getOrDefault(
                                        "overallStatus",
                                        "")));

                dto.setReferredByPerson(
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredByName",
                                        "")));

                dto.setTotalCost(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "finalAmount",
                                                0))));

                dto.setPaidAmount(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "totalPaid",
                                                0))));

                dto.setPendingAmount(
                        Double.parseDouble(
                                String.valueOf(
                                        payment.getOrDefault(
                                                "balanceAmount",
                                                0))));
                dto.setDateOfVisit(
                        String.valueOf(
                                payment.getOrDefault(
                                        "sessionStartDate",
                                        "")));

                result.add(dto);
            }

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Referral channel patient details fetched successfully");
            response.setData(result);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
    
    @Override
    public Response getReferralSummary(
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

            List<Map<String, Object>> bookings =
                    bookingResponse.getBody().getData();

            LocalDate today = LocalDate.now();

            long totalReferrals = 0;
            long doctorReferrals = 0;
            long selfReferrals = 0;
            long otherChannelsReferrals = 0;

            Map<String, Long> doctorCountMap =
                    new HashMap<>();

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
                            !serviceDate.isBefore(
                                    today.minusDays(6))
                            && !serviceDate.isAfter(today);
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

               

                String referralId =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredDoctorId",
                                        "")).trim();

                String doctorRefCode =
                        String.valueOf(
                                booking.getOrDefault(
                                        "doctorRefCode",
                                        "")).trim();

                String referredByType =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredByType",
                                        "")).trim();

                String referredByName =
                        String.valueOf(
                                booking.getOrDefault(
                                        "referredByName",
                                        "")).trim();

             // ================= VALIDATION =================

                boolean hasReferralId =
                        !referralId.isBlank()
                        && !"null".equalsIgnoreCase(referralId);

                boolean hasDoctorRefCode =
                        !doctorRefCode.isBlank()
                        && !"null".equalsIgnoreCase(doctorRefCode);

                boolean hasReferredByType =
                        !referredByType.isBlank()
                        && !"null".equalsIgnoreCase(referredByType);

                boolean hasReferredByName =
                        !referredByName.isBlank()
                        && !"null".equalsIgnoreCase(referredByName);


             // ================= DOCTOR REFERRAL =================

                if (hasReferralId) {

                    doctorReferrals++;
                    totalReferrals++;

                    doctorCountMap.merge(
                            referralId,
                            1L,
                            Long::sum);


                // ================= EXPLICIT SELF =================

                } else if ("Self".equalsIgnoreCase(doctorRefCode)) {

                    selfReferrals++;
                    totalReferrals++;


                // ================= ALL EMPTY = SELF =================

                } else if (!hasDoctorRefCode
                        && !hasReferredByType
                        && !hasReferredByName) {

                    selfReferrals++;
                    totalReferrals++;


                // ================= OTHER CHANNELS =================

                } else {

                    // Friend, Facebook, Instagram,
                    // Google, Website, etc.
                    otherChannelsReferrals++;
                    totalReferrals++;
                }
            }

            double doctorPercentage =
                    totalReferrals == 0
                            ? 0
                            : (doctorReferrals * 100.0)
                                    / totalReferrals;

            // ================= SELF PERCENTAGE =================

            double selfPercentage =
                    totalReferrals == 0
                            ? 0
                            : (selfReferrals * 100.0)
                                    / totalReferrals;

            // ================= OTHER CHANNEL PERCENTAGE =================

            double otherPercentage =
                    totalReferrals == 0
                            ? 0
                            : (otherChannelsReferrals * 100.0)
                                    / totalReferrals;

            String topDoctorName = "N/A";
            long topDoctorPatients = 0;

            for (Map.Entry<String, Long> entry :
                    doctorCountMap.entrySet()) {

                if (entry.getValue()
                        > topDoctorPatients) {

                    topDoctorPatients =
                            entry.getValue();

                    ReferredDoctor doctor =
                            referredDoctorRepository
                                    .findByReferralId(
                                            entry.getKey())
                                    .orElse(null);

                    if (doctor != null) {
                        topDoctorName =
                                doctor.getFullName();
                    }
                }
            }

            TopReferringDoctorDTO topDoctor =
                    new TopReferringDoctorDTO();

            topDoctor.setFullName(
                    topDoctorName);

            topDoctor.setPatientsReferred(
                    topDoctorPatients);

            ReferralSummaryDTO dto =
                    new ReferralSummaryDTO();

            dto.setTotalReferrals(
                    totalReferrals);

            dto.setDoctorReferrals(
                    doctorReferrals);
         // ================= SELF =================

            dto.setSelfReferrals(
                    selfReferrals);

            dto.setSelfReferralsPercentage(
                    Math.round(
                            selfPercentage * 100.0)
                            / 100.0);


            // ================= OTHER CHANNELS =================

            dto.setOtherChannelsReferrals(
                    otherChannelsReferrals);

            dto.setOtherChannelsReferralsPercentage(
                    Math.round(
                            otherPercentage * 100.0)
                            / 100.0);
            dto.setDoctorReferralsPercentage(
                    Math.round(
                            doctorPercentage * 100.0)
                            / 100.0);

            dto.setOtherChannelsReferrals(
                    otherChannelsReferrals);

            dto.setOtherChannelsReferralsPercentage(
                    Math.round(
                            otherPercentage * 100.0)
                            / 100.0);

            dto.setTopReferringDoctor(
                    topDoctor);

            response.setSuccess(true);
            response.setStatus(200);
            response.setMessage(
                    "Referral summary fetched successfully");
            response.setData(dto);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(500);
            response.setMessage(
                    e.getMessage());
        }

        return response;
    }
}