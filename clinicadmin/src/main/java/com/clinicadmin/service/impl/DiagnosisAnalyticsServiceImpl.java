package com.clinicadmin.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.DiagnosisCenterDTO;
import com.clinicadmin.dto.DiagnosisDashboardDTO;
import com.clinicadmin.dto.DiagnosisDetailsDTO;
import com.clinicadmin.dto.DiagnosisListResponseDTO;
import com.clinicadmin.dto.DiagnosisPatientDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.VendorTestDTO;
import com.clinicadmin.dto.VendorTestResponseDTO;
import com.clinicadmin.entity.TestOrder;
import com.clinicadmin.repository.TestOrderRepository;
import com.clinicadmin.service.DiagnosisAnalyticsService;

@Service
public class DiagnosisAnalyticsServiceImpl implements DiagnosisAnalyticsService {

    @Autowired
    private TestOrderRepository repository;

    /**
     * type codes:
     * 1 = Today
     * 2 = This Week (last 7 days)
     * 3 = This Month
     * 4 = This Year
     * 5 = Custom range (requires startDate & endDate)
     */
    private Instant[] resolveDateRange(Integer type,
            String startDate,
            String endDate) {

                 LocalDateTime now = LocalDateTime.now();
                 LocalDateTime rangeStart;
                 LocalDateTime rangeEnd = now;

                   switch (type) {

                  case 1:
                  rangeStart = LocalDate.now().atStartOfDay();
                  break;

                  case 2:
                  rangeStart = LocalDate.now().minusDays(6).atStartOfDay();
                   break;

                 case 3:
               rangeStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                 break;

                case 4:
              rangeStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
                  break;

             case 5:

                 if (startDate == null || startDate.isBlank()
                   || endDate == null || endDate.isBlank()) {

                         throw new IllegalArgumentException(
                  "startDate and endDate are required for custom range");
                   }

                     rangeStart = LocalDate.parse(startDate).atStartOfDay();
                   rangeEnd = LocalDate.parse(endDate).atTime(LocalTime.MAX);
             break;

                          default:
                   throw new IllegalArgumentException(
                      "Invalid type value. Allowed: 1 (Today), 2 (Week), 3 (Month), 4 (Year), 5 (Custom)");
  }

          return new Instant[] {
          rangeStart.atZone(ZoneId.systemDefault()).toInstant(),
          rangeEnd.atZone(ZoneId.systemDefault()).toInstant()
   };
}

    @Override
    public Response getDashboard(String clinicId,
                                 String branchId,
                                 Integer type,
                                 String startDate,
                                 String endDate) {

        Response response = new Response();

        try {

            Instant[] range = resolveDateRange(type, startDate, endDate);

            List<TestOrder> orders =
                    repository.findByClinicIdAndBranchIdAndOrderedAtBetween(
                            clinicId,
                            branchId,
                            range[0],
                            range[1]);

            // Ignore only null status
            List<TestOrder> validOrders = orders.stream()
                    .filter(order -> order.getStatus() != null)
                    .toList();

            DiagnosisDashboardDTO dashboard = new DiagnosisDashboardDTO();

            // Total Patients
            dashboard.setTotalPatients((long) validOrders.size());

            // Total Diagnosis Centers
            dashboard.setTotalDiagnosisCenters(
                    validOrders.stream()
                            .map(TestOrder::getVendorId)
                            .filter(id -> id != null && !id.isBlank())
                            .distinct()
                            .count());

            // Overall Counts
            long tested = 0;
            long pending = 0;
            long notTested = 0;

            for (TestOrder order : validOrders) {

                String status = order.getStatus();

                if ("Yes".equalsIgnoreCase(status)) {
                    tested++;
                } else if ("Pending".equalsIgnoreCase(status)) {
                    pending++;
                } else if ("No".equalsIgnoreCase(status)) {
                    notTested++;
                }
            }

            dashboard.setTested(tested);
            dashboard.setPending(pending);
            dashboard.setNotTested(notTested);

            response.setSuccess(true);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Diagnosis dashboard fetched successfully");
            response.setData(dashboard);

        } catch (IllegalArgumentException e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to fetch dashboard");
        }

        return response;
    }
    @Override
    public Response getDiagnosisTests(String clinicId,
                                      String branchId,
                                      Integer type,
                                      String startDate,
                                      String endDate) {

        Response response = new Response();

        try {

            Instant[] range = resolveDateRange(type, startDate, endDate);

            List<TestOrder> orders =
                    repository.findByClinicIdAndBranchIdAndOrderedAtBetween(
                            clinicId,
                            branchId,
                            range[0],
                            range[1]);

            // Ignore only null status
            List<TestOrder> validOrders = orders.stream()
                    .filter(order -> order.getStatus() != null)
                    .toList();

            // Group by Vendor
            Map<String, List<TestOrder>> groupedVendors = validOrders.stream()
                    .filter(order -> order.getVendorId() != null)
                    .collect(Collectors.groupingBy(TestOrder::getVendorId));

            List<DiagnosisCenterDTO> diagnosisCenters = new ArrayList<>();

            for (Map.Entry<String, List<TestOrder>> entry : groupedVendors.entrySet()) {

                List<TestOrder> vendorOrders = entry.getValue();

                DiagnosisCenterDTO dto = new DiagnosisCenterDTO();

                dto.setVendorId(entry.getKey());
                dto.setVendorName(vendorOrders.get(0).getVendorName());

                long totalPatients = vendorOrders.size();
                long testedPatients = 0;
                long pendingPatients = 0;
                long notTestedPatients = 0;

                for (TestOrder order : vendorOrders) {

                    String status = order.getStatus();

                    if ("Yes".equalsIgnoreCase(status)) {
                        testedPatients++;
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        pendingPatients++;
                    } else if ("No".equalsIgnoreCase(status)) {
                        notTestedPatients++;
                    }
                }

                double yield = totalPatients == 0
                        ? 0
                        : ((double) testedPatients / totalPatients) * 100;

                dto.setTotalPatients(totalPatients);
                dto.setTestedPatients(testedPatients);
                dto.setPendingPatients(pendingPatients);
                dto.setNotTestedPatients(notTestedPatients);
                dto.setYield(Math.round(yield * 100.0) / 100.0);

                diagnosisCenters.add(dto);
            }

            diagnosisCenters.sort(
                    Comparator.comparing(DiagnosisCenterDTO::getTotalPatients)
                            .reversed());

            DiagnosisListResponseDTO result = new DiagnosisListResponseDTO();
            result.setDiagnosisCenters(diagnosisCenters);

            response.setSuccess(true);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Diagnosis centers fetched successfully");
            response.setData(result);

        } catch (IllegalArgumentException e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to fetch diagnosis centers");
        }

        return response;
    }
    @Override
    public Response getDiagnosisDetails(String clinicId,
                                        String branchId,
                                        String vendorId,
                                        Integer type,
                                        String startDate,
                                        String endDate) {

        Response response = new Response();

        try {

            Instant[] range = resolveDateRange(type, startDate, endDate);

            List<TestOrder> orders =
                    repository.findByClinicIdAndBranchIdAndVendorIdAndOrderedAtBetween(
                            clinicId,
                            branchId,
                            vendorId,
                            range[0],
                            range[1]);

            if (orders.isEmpty()) {

                response.setSuccess(false);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("Diagnosis Center not found");
                response.setData(null);

                return response;
            }

            // Ignore only null status
            List<TestOrder> validOrders = orders.stream()
                    .filter(order -> order.getStatus() != null)
                    .toList();

            DiagnosisDetailsDTO details = new DiagnosisDetailsDTO();

            details.setVendorId(vendorId);
            details.setVendorName(validOrders.get(0).getVendorName());

            // ================= Counts =================

            long totalPatients = validOrders.size();
            long testedPatients = 0;
            long pendingPatients = 0;
            long notTestedPatients = 0;

            for (TestOrder order : validOrders) {

                String status = order.getStatus();

                if ("Yes".equalsIgnoreCase(status)) {
                    testedPatients++;
                } else if ("Pending".equalsIgnoreCase(status)) {
                    pendingPatients++;
                } else if ("No".equalsIgnoreCase(status)) {
                    notTestedPatients++;
                }
            }

            double yield = totalPatients == 0
                    ? 0
                    : ((double) testedPatients / totalPatients) * 100;

            details.setTotalPatients(totalPatients);
            details.setTestedPatients(testedPatients);
            details.setPendingPatients(pendingPatients);
            details.setNotTestedPatients(notTestedPatients);
            details.setYield(Math.round(yield * 100.0) / 100.0);

            // ================= Patient List =================

            List<DiagnosisPatientDTO> patients = new ArrayList<>();

            for (TestOrder order : validOrders) {

                DiagnosisPatientDTO dto = new DiagnosisPatientDTO();

                dto.setPatientId(order.getPatientId());
                dto.setPatientName(order.getPatientName());
                dto.setMobileNumber(order.getMobileNumber());

                dto.setDoctorId(order.getDoctorId());
                dto.setDoctorName(order.getDoctorName());

                dto.setVendorId(order.getVendorId());
                dto.setVendorName(order.getVendorName());

                dto.setBookingId(order.getBookingId());

                dto.setStatus(order.getStatus());

                dto.setOrderedAt(order.getOrderedAt());

                patients.add(dto);
            }

            patients.sort(
                    Comparator.comparing(
                            DiagnosisPatientDTO::getOrderedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            details.setPatients(patients);

            response.setSuccess(true);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Diagnosis center details fetched successfully");
            response.setData(details);

        } catch (IllegalArgumentException e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to fetch diagnosis center details");
        }

        return response;
    }
    
    @Override
    public Response getVendorTestSummary(String clinicId,
                                         String branchId,
                                         String vendorId,
                                         String testNameId) {

        Response response = new Response();

        try {

            List<TestOrder> orders =
                    repository.findByClinicIdAndBranchIdAndVendorIdAndTestNameId(
                            clinicId,
                            branchId,
                            vendorId,
                            testNameId);

            if (orders.isEmpty()) {

                response.setSuccess(false);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("Diagnosis Test not found");
                response.setData(null);

                return response;
            }

            List<TestOrder> validOrders = orders.stream()
                    .filter(order -> order.getStatus() != null)
                    .toList();

            VendorTestResponseDTO result = new VendorTestResponseDTO();

            result.setVendorId(vendorId);
            result.setVendorName(validOrders.get(0).getVendorName());

            long totalAssigned = validOrders.size();
            long tested = 0;
            long pending = 0;
            long notTested = 0;

            for (TestOrder order : validOrders) {

                String status = order.getStatus();

                if ("Yes".equalsIgnoreCase(status)) {
                    tested++;
                } else if ("Pending".equalsIgnoreCase(status)) {
                    pending++;
                } else if ("No".equalsIgnoreCase(status)) {
                    notTested++;
                }
            }

            double yield = totalAssigned == 0
                    ? 0
                    : ((double) tested / totalAssigned) * 100;

            result.setTotalAssigned(totalAssigned);
            result.setTested(tested);
            result.setPending(pending);
            result.setNotTested(notTested);
            result.setYield(Math.round(yield * 100.0) / 100.0);

            List<VendorTestDTO> tests = new ArrayList<>();

            VendorTestDTO dto = new VendorTestDTO();

            dto.setTestName(validOrders.get(0).getTestName());
            dto.setTestNameId(testNameId);
            dto.setTotalAssigned(totalAssigned);
            dto.setTested(tested);
            dto.setPending(pending);
            dto.setNotTested(notTested);

            tests.add(dto);

            result.setTests(tests);
            List<DiagnosisPatientDTO> patients = new ArrayList<>();

            for (TestOrder order : validOrders) {

                DiagnosisPatientDTO patient = new DiagnosisPatientDTO();

                patient.setPatientId(order.getPatientId());
                patient.setPatientName(order.getPatientName());
                patient.setMobileNumber(order.getMobileNumber());

                patient.setDoctorId(order.getDoctorId());
                patient.setDoctorName(order.getDoctorName());

                patient.setVendorId(order.getVendorId());
                patient.setVendorName(order.getVendorName());

                patient.setBookingId(order.getBookingId());

                patient.setStatus(order.getStatus());

                patient.setOrderedAt(order.getOrderedAt());

                patients.add(patient);
            }

            patients.sort(
                    Comparator.comparing(
                            DiagnosisPatientDTO::getOrderedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            result.setPatients(patients);
            response.setSuccess(true);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Vendor test summary fetched successfully");
            response.setData(result);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to fetch vendor test summary");
        }

        return response;
    }
    
    @Override
    public Response getVendorTests(String clinicId,
                                   String branchId,
                                   String vendorId) {

        Response response = new Response();

        try {

            List<TestOrder> orders =
                    repository.findByClinicIdAndBranchIdAndVendorId(
                            clinicId,
                            branchId,
                            vendorId);

            if (orders.isEmpty()) {

                response.setSuccess(false);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("No tests found");
                response.setData(null);

                return response;
            }

            List<TestOrder> validOrders = orders.stream()
                    .filter(order -> order.getStatus() != null)
                    .toList();

            Map<String, List<TestOrder>> groupedTests = validOrders.stream()
                    .collect(Collectors.groupingBy(TestOrder::getTestNameId));

            List<VendorTestDTO> tests = new ArrayList<>();

            for (Map.Entry<String, List<TestOrder>> entry : groupedTests.entrySet()) {

                List<TestOrder> testOrders = entry.getValue();

                VendorTestDTO dto = new VendorTestDTO();

                dto.setTestName(testOrders.get(0).getTestName());
                dto.setTestNameId(testOrders.get(0).getTestNameId());

                long totalAssigned = testOrders.size();
                long tested = 0;
                long pending = 0;
                long notTested = 0;

                for (TestOrder order : testOrders) {

                    String status = order.getStatus();

                    if ("Yes".equalsIgnoreCase(status)) {
                        tested++;
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        pending++;
                    } else if ("No".equalsIgnoreCase(status)) {
                        notTested++;
                    }
                }

                dto.setTotalAssigned(totalAssigned);
                dto.setTested(tested);
                dto.setPending(pending);
                dto.setNotTested(notTested);

                tests.add(dto);
            }

            tests.sort(Comparator.comparing(VendorTestDTO::getTotalAssigned).reversed());

            response.setSuccess(true);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Vendor tests fetched successfully");
            response.setData(tests);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to fetch vendor tests");
        }

        return response;
    }
}