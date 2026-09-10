package com.clinicadmin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.clinicadmin.dto.AttendanceDTO;
import com.clinicadmin.dto.DailyAllUsersResponseDTO;
import com.clinicadmin.dto.DailyAttendanceResponseDTO;
import com.clinicadmin.dto.MonthlyAttendanceResponseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TimeLocationDTO;
import com.clinicadmin.dto.UserLoginDetailsDTO;
import com.clinicadmin.entity.Attendance;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.entity.ReceptionistEntity;
import com.clinicadmin.entity.TherapistAttendance;
import com.clinicadmin.entity.TimeLocation;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.AttendanceRepository;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.repository.ReceptionistRepository;
import com.clinicadmin.repository.TherapistAttendanceRepository;
import com.clinicadmin.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository repo;

    private final AdminServiceClient adminServiceClient;

    private final TherapistAttendanceRepository therapistAttendanceRepo;
    
    private final DoctorsRepository doctorsRepository;

    private final ReceptionistRepository receptionistRepository;

    @Autowired
    private DoctorLoginCredentialsRepository credentialsRepository;
    @Override
    public Response save(AttendanceDTO dto) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATION
            // =====================================================

            if (dto.getUserId() == null
                    || dto.getUserId().isBlank()) {

                throw new RuntimeException("userId is required");
            }

            if (dto.getDate() == null
                    || dto.getDate().isBlank()) {

                throw new RuntimeException("date is required");
            }

            if (dto.getLogin() == null
                    || dto.getLogin().getTime() == null
                    || dto.getLogin().getTime().isBlank()) {

                throw new RuntimeException("login time is required");
            }

            // =====================================================
            // 2. FIND TODAY'S ATTENDANCE
            // =====================================================

            Optional<Attendance> existingOpt =
                    repo.findByUserIdAndDate(
                            dto.getUserId(),
                            dto.getDate()
                    );

            Attendance entity;

            if (existingOpt.isPresent()) {

                entity = existingOpt.get();

                // =================================================
                // IF ALREADY LOGGED OUT
                // =================================================

                if ("LOGGED_OUT".equalsIgnoreCase(
                        entity.getStatus())) {

                    throw new RuntimeException(
                            "Attendance already logged out for today"
                    );
                }

                // =================================================
                // UPDATE LOGIN
                // =================================================

                mapDtoToEntity(dto, entity);

            } else {

                // =================================================
                // CREATE NEW ATTENDANCE
                // =================================================

                entity = new Attendance();

                mapDtoToEntity(dto, entity);
            }

            // =====================================================
            // 3. GET SHIFT START TIME
            // =====================================================

            String shiftStartTime =
                    getShiftStartTime(
                            entity.getUserId(),
                            entity.getRole(),
                            entity.getClinicId(),
                            entity.getBranchId()
                    );

            // =====================================================
            // 4. PARSE LOGIN TIME
            // =====================================================

            int loginMinutes =
                    parseTimeToMinutes(
                            entity.getLogin().getTime()
                    );

            int shiftStartMinutes =
                    parseTimeToMinutes(
                            shiftStartTime
                    );

            // =====================================================
            // 5. CALCULATE LATE TIME
            // =====================================================

            if (loginMinutes > shiftStartMinutes) {

                int lateMinutes =
                        loginMinutes - shiftStartMinutes;

                entity.setLateTime(
                        formatMinutes(lateMinutes)
                );

                entity.setStatus("LATE");

                // Save late reason if provided

                if (dto.getLogin().getReason() != null
                        && !dto.getLogin().getReason().isBlank()) {

                    entity.getLogin().setReason(
                            dto.getLogin().getReason().trim()
                    );
                }

            } else {

                // =================================================
                // EMPLOYEE IS ON TIME
                // =================================================

                entity.setLateTime("0m");

                entity.getLogin().setReason(null);

                entity.setStatus("LOGGED_IN");
            }

            // =====================================================
            // 6. LOGIN ONLY
            // =====================================================

            entity.setIdleTime(null);
            entity.setLogTime(null);
            entity.setWorkingHours(null);
            entity.setOvertime(null);

            // =====================================================
            // 7. SAVE ATTENDANCE
            //
            // DOCTOR
            // PHYSIOTHERAPIST
            // RECEPTIONIST
            //
            // All are stored in Attendance collection.
            // =====================================================

            Attendance savedAttendance =
                    repo.save(entity);

            // =====================================================
            // 8. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    existingOpt.isPresent()
                            ? "Attendance updated successfully"
                            : "Attendance created successfully"
            );

            response.setData(savedAttendance);

            response.setStatus(
                    existingOpt.isPresent()
                            ? 200
                            : 201
            );

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(400);
        }

        return response;
    }
    @Override
    public Response updateActivity(AttendanceDTO dto) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATION
            // =====================================================

            if (dto.getUserId() == null
                    || dto.getUserId().isBlank()
                    || dto.getDate() == null
                    || dto.getDate().isBlank()) {

                throw new RuntimeException(
                        "userId and date are required"
                );
            }

            // =====================================================
            // 2. FIND ATTENDANCE
            // =====================================================

            Optional<Attendance> optional =
                    repo.findByUserIdAndDate(
                            dto.getUserId(),
                            dto.getDate()
                    );

            if (optional.isEmpty()) {

                throw new RuntimeException(
                        "Attendance not found for update"
                );
            }

            Attendance entity = optional.get();

            // =====================================================
            // 3. RESOLVE ROLE IF ROLE IS NULL
            // =====================================================

            String role = entity.getRole();

            if (role == null || role.isBlank()) {

                // =================================================
                // TRY DOCTOR / PHYSIOTHERAPIST
                // Both are stored in Doctors collection
                // =================================================

                Optional<Doctors> doctorOpt =
                        doctorsRepository.findByDoctorId(
                                entity.getUserId()
                        );

                if (doctorOpt.isPresent()) {

                    role = doctorOpt.get().getRole();

                    // If role is not configured
                    if (role == null || role.isBlank()) {
                        role = "DOCTOR";
                    }

                } else {

                    // =================================================
                    // TRY RECEPTIONIST
                    // =================================================

                    Optional<ReceptionistEntity> receptionistOpt =
                            receptionistRepository.findByClinicIdAndId(
                                    entity.getClinicId(),
                                    entity.getUserId()
                            );

                    if (receptionistOpt.isPresent()) {

                        role = "RECEPTIONIST";

                    } else {

                        throw new RuntimeException(
                                "Unable to determine role for userId: "
                                        + entity.getUserId()
                        );
                    }
                }

                // Store resolved role in Attendance
                entity.setRole(role);
            }

            boolean updated = false;

            // =====================================================
            // 4. LOGIN UPDATE
            // =====================================================

            if (dto.getLoginTime() != null
                    && !dto.getLoginTime().isBlank()) {

                // =================================================
                // CREATE LOGIN OBJECT IF NULL
                // =================================================

                if (entity.getLogin() == null) {
                    entity.setLogin(new TimeLocation());
                }

                entity.getLogin().setTime(
                        dto.getLoginTime()
                );

                // =================================================
                // GET SHIFT START TIME
                // =================================================

                String shiftStartTime =
                        getShiftStartTime(
                                entity.getUserId(),
                                entity.getRole(),
                                entity.getClinicId(),
                                entity.getBranchId()
                        );

                // =================================================
                // PARSE LOGIN + SHIFT START
                // =================================================

                int loginMinutes =
                        parseTimeToMinutes(
                                dto.getLoginTime()
                        );

                int shiftStartMinutes =
                        parseTimeToMinutes(
                                shiftStartTime
                        );

                // =================================================
                // LATE TIME
                // =================================================

                if (loginMinutes > shiftStartMinutes) {

                    int lateMinutes =
                            loginMinutes - shiftStartMinutes;

                    entity.setLateTime(
                            formatMinutes(lateMinutes)
                    );

                    entity.setStatus("LATE");

                    // =================================================
                    // SAVE LATE REASON INSIDE LOGIN
                    // =================================================

                    if (dto.getReason() != null
                            && !dto.getReason().isBlank()) {

                        entity.getLogin().setReason(
                                dto.getReason().trim()
                        );
                    }

                } else {

                    // =================================================
                    // EMPLOYEE IS ON TIME
                    // =================================================

                    entity.setLateTime("0m");

                    entity.getLogin().setReason(null);

                    entity.setStatus("LOGGED_IN");
                }

                updated = true;
            }

            // =====================================================
            // 5. LOGOUT UPDATE
            // =====================================================

            if (dto.getLogoutTime() != null
                    && !dto.getLogoutTime().isBlank()) {

                // =================================================
                // LOGIN MUST EXIST
                // =================================================

                if (entity.getLogin() == null
                        || entity.getLogin().getTime() == null
                        || entity.getLogin().getTime().isBlank()) {

                    throw new RuntimeException(
                            "Login time is missing in existing attendance"
                    );
                }

                // =================================================
                // CREATE LOGOUT OBJECT
                // =================================================

                if (entity.getLogout() == null) {
                    entity.setLogout(new TimeLocation());
                }

                entity.getLogout().setTime(
                        dto.getLogoutTime()
                );

                // =================================================
                // GET TIMES
                // =================================================

                String loginTime =
                        entity.getLogin().getTime();

                String logoutTime =
                        dto.getLogoutTime();

                String shiftStartTime =
                        getShiftStartTime(
                                entity.getUserId(),
                                role,
                                entity.getClinicId(),
                                entity.getBranchId()
                        );

                String shiftEndTime =
                        getShiftEndTime(
                                entity.getUserId(),
                                role,
                                entity.getClinicId(),
                                entity.getBranchId()
                        );

                // =================================================
                // VALIDATION
                // =================================================

                if (loginTime == null
                        || loginTime.isBlank()) {

                    throw new RuntimeException(
                            "Login time is empty"
                    );
                }

                if (logoutTime == null
                        || logoutTime.isBlank()) {

                    throw new RuntimeException(
                            "Logout time is empty"
                    );
                }

                if (shiftStartTime == null
                        || shiftStartTime.isBlank()) {

                    throw new RuntimeException(
                            "Shift start time is empty"
                    );
                }

                if (shiftEndTime == null
                        || shiftEndTime.isBlank()) {

                    throw new RuntimeException(
                            "Shift end time is empty"
                    );
                }

                // =================================================
                // PARSE TIMES
                // =================================================

                int loginMinutes =
                        parseTimeToMinutes(
                                loginTime
                        );

                int logoutMinutes =
                        parseTimeToMinutes(
                                logoutTime
                        );

                int shiftStartMinutes =
                        parseTimeToMinutes(
                                shiftStartTime
                        );

                int shiftEndMinutes =
                        parseTimeToMinutes(
                                shiftEndTime
                        );

                // =================================================
                // 1. LATE TIME
                // =================================================

                int lateMinutes = 0;

                if (loginMinutes > shiftStartMinutes) {

                    lateMinutes =
                            loginMinutes - shiftStartMinutes;

                    entity.setLateTime(
                            formatMinutes(
                                    lateMinutes
                            )
                    );

                    // =================================================
                    // SAVE LATE REASON INSIDE LOGIN
                    // =================================================

                    if (entity.getLogin().getReason() == null
                            || entity.getLogin().getReason().isBlank()) {

                        if (dto.getReason() != null
                                && !dto.getReason().isBlank()) {

                            entity.getLogin().setReason(
                                    dto.getReason().trim()
                            );
                        }
                    }

                } else {

                    entity.setLateTime("0m");

                    entity.getLogin().setReason(null);
                }

                // =================================================
                // 2. WORKING HOURS
                //
                // Actual login -> logout
                // =================================================

                int totalWorkingMinutes =
                        logoutMinutes - loginMinutes;

                if (totalWorkingMinutes < 0) {
                    totalWorkingMinutes = 0;
                }

                entity.setLogTime(
                        formatMinutes(
                                totalWorkingMinutes
                        )
                );

                entity.setWorkingHours(
                        formatMinutes(
                                totalWorkingMinutes
                        )
                );

                // =================================================
                // 3. EARLY LOGOUT
                //
                // Based on configured shift end time.
                // =================================================

                int earlyLogoutMinutes = 0;

                if (logoutMinutes < shiftEndMinutes) {

                    earlyLogoutMinutes =
                            shiftEndMinutes - logoutMinutes;

                    // =================================================
                    // SAVE EARLY LOGOUT REASON INSIDE LOGOUT
                    // =================================================

                    if (dto.getReason() != null
                            && !dto.getReason().isBlank()) {

                        entity.getLogout().setReason(
                                dto.getReason().trim()
                        );
                    }
                }

                // =================================================
                // 4. GROSS OVERTIME
                //
                // Logout after shift end
                // =================================================

                int grossOvertimeMinutes = 0;

                if (logoutMinutes > shiftEndMinutes) {

                    grossOvertimeMinutes =
                            logoutMinutes - shiftEndMinutes;

                    // No early logout
                    entity.getLogout().setReason(null);
                }

                // =================================================
                // 5. NET OVERTIME
                //
                // Gross overtime - late arrival
                // =================================================

                int netOvertimeMinutes =
                        grossOvertimeMinutes
                                - lateMinutes;

                if (netOvertimeMinutes < 0) {
                    netOvertimeMinutes = 0;
                }

                entity.setOvertime(
                        formatMinutes(
                                netOvertimeMinutes
                        )
                );

                // =================================================
                // 6. TOTAL IDLE TIME
                //
                // Late arrival + Early logout
                // - Gross overtime
                // =================================================

                int totalIdleMinutes =
                        lateMinutes
                                + earlyLogoutMinutes
                                - grossOvertimeMinutes;

                if (totalIdleMinutes < 0) {
                    totalIdleMinutes = 0;
                }

                entity.setIdleTime(
                        formatMinutes(
                                totalIdleMinutes
                        )
                );

                // =================================================
                // 7. LOGOUT STATUS
                // =================================================

                entity.setStatus(
                        "LOGGED_OUT"
                );

                updated = true;
            }

            // =====================================================
            // 6. CHECK UPDATE
            // =====================================================

            if (!updated) {

                throw new RuntimeException(
                        "No matching update found"
                );
            }

            // =====================================================
            // 7. SAVE ATTENDANCE
            //
            // DOCTOR
            // PHYSIOTHERAPIST
            // RECEPTIONIST
            //
            // ALL ARE SAVED IN Attendance COLLECTION
            // =====================================================

            Attendance savedAttendance =
                    repo.save(entity);

            // =====================================================
            // 8. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    "Attendance updated successfully"
            );

            response.setData(
                    savedAttendance
            );

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(400);
        }

        return response;
    }
    private String getShiftEndTime(
            String userId,
            String role,
            String clinicId,
            String branchId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        // =====================================================
        // DOCTOR / PHYSIOTHERAPIST
        // Both are stored in Doctors collection
        // =====================================================

        if ("DOCTOR".equalsIgnoreCase(role)
                || "PHYSIOTHERAPIST".equalsIgnoreCase(role)) {

            Optional<Doctors> doctorOpt =
                    doctorsRepository.findByDoctorId(userId);

            if (doctorOpt.isEmpty()) {

                throw new RuntimeException(
                        "Doctor/Physiotherapist not found for doctorId: "
                                + userId
                );
            }

            Doctors doctor = doctorOpt.get();

            // =================================================
            // CHECK CLINIC
            // =================================================

            if (clinicId != null
                    && !clinicId.isBlank()
                    && !clinicId.equals(doctor.getHospitalId())) {

                throw new RuntimeException(
                        "User does not belong to clinicId: "
                                + clinicId
                );
            }

            // =================================================
            // CHECK BRANCH
            // =================================================

            if (branchId != null && !branchId.isBlank()) {

                boolean branchExists = doctor.getBranches() != null
                        && doctor.getBranches().stream()
                                .anyMatch(branch ->
                                        branchId.equals(branch.getBranchId())
                                );

                if (!branchExists) {
                    throw new RuntimeException(
                            "Doctor does not belong to branchId: "
                                    + branchId
                    );
                }
            }

            // =================================================
            // GET AVAILABLE TIME
            // =================================================

            String availableTimes =
                    doctor.getAvailableTimes();

            if (availableTimes == null
                    || availableTimes.isBlank()) {

                throw new RuntimeException(
                        "Shift timing not configured for userId: "
                                + userId
                );
            }

            // =================================================
            // SPLIT SHIFT
            //
            // Example:
            // 10:00 AM - 6:00 PM
            //
            // parts[0] = 10:00 AM
            // parts[1] = 6:00 PM
            // =================================================

            String[] parts =
                    availableTimes.split("-", 2);

            if (parts.length != 2) {

                throw new RuntimeException(
                        "Invalid shift timing: "
                                + availableTimes
                );
            }

            String shiftEnd =
                    parts[1].trim();

            if (shiftEnd.isBlank()) {

                throw new RuntimeException(
                        "Shift end time not configured for userId: "
                                + userId
                );
            }

            return shiftEnd;
        }

        // =====================================================
        // RECEPTIONIST
        //
        // Receptionist shift comes from Clinic Onboarding
        //
        // closingTime = shift end
        // =====================================================

        if ("RECEPTIONIST".equalsIgnoreCase(role)) {

            if (clinicId == null || clinicId.isBlank()) {

                throw new RuntimeException(
                        "clinicId is required for receptionist shift"
                );
            }

            // =================================================
            // GET CLINIC FROM CLINIC ONBOARDING
            // =================================================

            ResponseEntity<Response> clinicResponse =
                    adminServiceClient.getClinicById(clinicId);

            if (clinicResponse == null
                    || clinicResponse.getBody() == null
                    || clinicResponse.getBody().getData() == null) {

                throw new RuntimeException(
                        "Clinic not found for clinicId: "
                                + clinicId
                );
            }

            // =================================================
            // GET CLINIC DATA
            // =================================================

            @SuppressWarnings("unchecked")
            Map<String, Object> clinic =
                    (Map<String, Object>)
                            clinicResponse.getBody().getData();

            // =================================================
            // GET CLOSING TIME
            // =================================================

            Object closingTime =
                    clinic.get("closingTime");

            if (closingTime == null
                    || String.valueOf(closingTime).isBlank()) {

                throw new RuntimeException(
                        "Clinic closingTime not configured for clinicId: "
                                + clinicId
                );
            }

            // =================================================
            // RETURN CLINIC CLOSING TIME
            // =================================================

            return String.valueOf(closingTime).trim();
        }

        // =====================================================
        // UNSUPPORTED ROLE
        // =====================================================

        throw new RuntimeException(
                "Shift timing not supported for role: "
                        + role
        );
    }

    @Override
    public Response getDaily(String userId, String date) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. FIND ATTENDANCE
            // =====================================================

            Optional<Attendance> optional =
                    repo.findByUserIdAndDate(
                            userId,
                            date
                    );

            // =====================================================
            // 2. NO ATTENDANCE
            // =====================================================

            if (optional.isEmpty()) {

                DailyAttendanceResponseDTO emptyDto =
                        new DailyAttendanceResponseDTO();

                emptyDto.setDate(date);
                emptyDto.setStatus("Not Logged In");

                emptyDto.setLogTime(null);

                emptyDto.setWorkingHours("00:00");

                emptyDto.setIdleTime("00:00");

                emptyDto.setLateTime("0m");

                emptyDto.setOvertime("0m");

                emptyDto.setReason(null);

                emptyDto.setLogin(null);

                emptyDto.setLogout(null);

                response.setSuccess(true);

                response.setMessage(
                        "No attendance found"
                );

                response.setData(emptyDto);

                response.setStatus(200);

                return response;
            }

            // =====================================================
            // 3. GET ATTENDANCE
            // =====================================================

            Attendance entity =
                    optional.get();
            
            String role = entity.getRole();

            String shiftStartTime =
                    getShiftStartTime(
                            userId,
                            role,
                            entity.getClinicId(),
                            entity.getBranchId()
                    );

            String shiftEndTime =
                    getShiftEndTime(
                            userId,
                            role,
                            entity.getClinicId(),
                            entity.getBranchId()
                    );

            String shift =
                    calculateShiftDuration(
                            shiftStartTime,
                            shiftEndTime
                    );

            DailyAttendanceResponseDTO dto =
                    new DailyAttendanceResponseDTO();

            // =====================================================
            // 4. BASIC DETAILS
            // =====================================================

            dto.setDate(
                    entity.getDate()
            );

            dto.setStatus(
                    entity.getStatus()
            );

            dto.setLogTime(
                    entity.getLogTime()
            );
            
            dto.setShift(shift);

            // =====================================================
            // 5. WORKING HOURS
            // =====================================================

            dto.setWorkingHours(
                    entity.getWorkingHours() != null
                            ? entity.getWorkingHours()
                            : "00:00"
            );

            // =====================================================
            // 6. IDLE TIME
            // =====================================================

            dto.setIdleTime(
                    entity.getIdleTime() != null
                            ? entity.getIdleTime()
                            : "00:00"
            );

            // =====================================================
            // 7. LATE TIME
            // =====================================================

            dto.setLateTime(
                    entity.getLateTime() != null
                            ? entity.getLateTime()
                            : "0m"
            );

            // =====================================================
            // 8. OVERTIME
            // =====================================================

            dto.setOvertime(
                    entity.getOvertime() != null
                            ? entity.getOvertime()
                            : "0m"
            );

            // =====================================================
            // 9. ABSENT REASON
            //
            // Top-level reason is ONLY for ABSENT
            // =====================================================

            if ("ABSENT".equalsIgnoreCase(
                    entity.getStatus())) {

                dto.setReason(
                        entity.getReason()
                );

            } else {

                dto.setReason(null);
            }

            // =====================================================
            // 10. LOGIN
            //
            // Login reason = Late login reason
            // =====================================================

            if (entity.getLogin() != null) {

                TimeLocationDTO login =
                        new TimeLocationDTO();

                login.setTime(
                        entity.getLogin().getTime()
                );

                login.setReason(
                        entity.getLogin().getReason()
                );

                dto.setLogin(login);
            }

            // =====================================================
            // 11. LOGOUT
            //
            // Logout reason = Early logout reason
            // =====================================================

            if (entity.getLogout() != null) {

                TimeLocationDTO logout =
                        new TimeLocationDTO();

                logout.setTime(
                        entity.getLogout().getTime()
                );

                logout.setReason(
                        entity.getLogout().getReason()
                );

                dto.setLogout(logout);
            }

            // =====================================================
            // 12. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    "Daily report fetched successfully"
            );

            response.setData(dto);

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(404);
        }

        return response;
    }

    @Override
    public Response getMonthlyReport(String userId, String month) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATION
            // =====================================================

            if (userId == null || userId.isBlank()) {

                throw new RuntimeException(
                        "userId is required"
                );
            }

            if (month == null || month.length() != 7) {

                throw new RuntimeException(
                        "Invalid month format. Use yyyy-MM"
                );
            }

            // =====================================================
            // 2. GET MONTHLY ATTENDANCE
            // =====================================================

            List<Attendance> list =
                    repo.findByUserIdAndDateStartingWith(
                            userId,
                            month
                    );

            // =====================================================
            // 3. RESULT
            // =====================================================

            List<MonthlyAttendanceResponseDTO> result =
                    new ArrayList<>();

            // =====================================================
            // 4. GET USER SHIFT
            //
            // Doctor / Physiotherapist:
            //     Doctors availableTimes
            //
            // Receptionist:
            //     Clinic openingTime - closingTime
            // =====================================================

            String role = null;
            String clinicId = null;
            String branchId = null;

            if (list != null && !list.isEmpty()) {

                Attendance firstAttendance = list.get(0);

                role = firstAttendance.getRole();
                clinicId = firstAttendance.getClinicId();
                branchId = firstAttendance.getBranchId();
            }

            String shiftStartTime =
                    getShiftStartTime(
                            userId,
                            role,
                            clinicId,
                            branchId
                    );

            String shiftEndTime =
                    getShiftEndTime(
                            userId,
                            role,
                            clinicId,
                            branchId
                    );

            // =====================================================
            // 5. CALCULATE ASSIGNED SHIFT DURATION
            // =====================================================

            String shift =
                    calculateShiftDuration(
                            shiftStartTime,
                            shiftEndTime
                    );

            // =====================================================
            // 6. DAY-BY-DAY ATTENDANCE
            // =====================================================

            for (Attendance att : list) {

                MonthlyAttendanceResponseDTO dto =
                        new MonthlyAttendanceResponseDTO();

                // =================================================
                // DATE
                // =================================================

                dto.setDate(
                        att.getDate()
                );

                // =================================================
                // ASSIGNED SHIFT
                // =================================================

                dto.setShift(
                        shift
                );

                // =================================================
                // LOGIN
                // =================================================

                if (att.getLogin() != null) {

                    dto.setInTime(
                            att.getLogin().getTime()
                    );
                }

                // =================================================
                // LOGOUT
                // =================================================

                if (att.getLogout() != null) {

                    dto.setOutTime(
                            att.getLogout().getTime()
                    );
                }

                // =================================================
                // ACTUAL LOG TIME FOR THIS DAY
                // =================================================

                dto.setLogTime(
                        att.getLogTime()
                );

                // =================================================
                // ACTUAL WORKING HOURS FOR THIS DAY
                // =================================================

                dto.setWorkingHours(
                        att.getWorkingHours() != null
                                ? att.getWorkingHours()
                                : "0h 0m"
                );

                // =================================================
                // IDLE TIME FOR THIS DAY
                // =================================================

                dto.setIdleTime(
                        att.getIdleTime() != null
                                ? att.getIdleTime()
                                : "0h 0m"
                );

                result.add(dto);
            }

            // =====================================================
            // 7. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    "Monthly report fetched successfully"
            );

            response.setData(result);

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(400);
        }

        return response;
    }
    
    private String calculateShiftDuration(
            String shiftStartTime,
            String shiftEndTime) {

        int startMinutes =
                parseTimeToMinutes(shiftStartTime);

        int endMinutes =
                parseTimeToMinutes(shiftEndTime);

        int duration =
                endMinutes - startMinutes;

        if (duration < 0) {
            duration += 24 * 60;
        }

        return formatMinutes(duration);
    }

    private void mapDtoToEntity(AttendanceDTO dto, Attendance entity) {

        entity.setUserId(dto.getUserId());
        entity.setClinicId(dto.getClinicId());
        entity.setBranchId(dto.getBranchId());
        entity.setDate(dto.getDate());
        entity.setRole(
                dto.getRole() != null
                        ? dto.getRole().trim().toUpperCase()
                        : null
        );

        if (dto.getLogin() != null) {
            TimeLocation login = new TimeLocation();
            login.setTime(dto.getLogin().getTime());
            entity.setLogin(login);
        }

        if (dto.getLogout() != null) {
            TimeLocation logout = new TimeLocation();
            logout.setTime(dto.getLogout().getTime());
            entity.setLogout(logout);
        }
    }

    private AttendanceDTO mapEntityToDto(Attendance entity) {

        AttendanceDTO dto = new AttendanceDTO();

        dto.setUserId(entity.getUserId());
        dto.setRole(entity.getRole());
        dto.setClinicId(entity.getClinicId());
        dto.setBranchId(entity.getBranchId());
        dto.setDate(entity.getDate());

        if (entity.getLogin() != null) {
            TimeLocationDTO login = new TimeLocationDTO();
            login.setTime(entity.getLogin().getTime());
            dto.setLogin(login);
        }

        if (entity.getLogout() != null) {
            TimeLocationDTO logout = new TimeLocationDTO();
            logout.setTime(entity.getLogout().getTime());
            dto.setLogout(logout);
        }

        return dto;
    }

    private int parseTimeToMinutes(String input) {

        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Time is required");
        }

        String value = input
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", " ");

        try {

            // =====================================================
            // 10:00 AM / 10:00 PM
            // =====================================================

            if (value.matches("\\d{1,2}:\\d{2}\\s+(AM|PM)")) {

                String[] parts = value.split(" ");

                String timePart = parts[0];
                String period = parts[1];

                String[] timeParts = timePart.split(":");

                int hours =
                        Integer.parseInt(timeParts[0]);

                int minutes =
                        Integer.parseInt(timeParts[1]);

                if (hours < 1 || hours > 12) {
                    throw new RuntimeException(
                            "Invalid hour: " + hours
                    );
                }

                if (minutes < 0 || minutes > 59) {
                    throw new RuntimeException(
                            "Invalid minutes: " + minutes
                    );
                }

                // 12 AM = 00:00
                if ("AM".equals(period)) {

                    if (hours == 12) {
                        hours = 0;
                    }

                }
                // 12 PM = 12:00
                else {

                    if (hours != 12) {
                        hours += 12;
                    }
                }

                return hours * 60 + minutes;
            }

            // =====================================================
            // 24-HOUR FORMAT
            // 10:00
            // 18:00
            // 07:30
            // =====================================================

            if (value.matches("\\d{1,2}:\\d{2}")) {

                String[] parts = value.split(":");

                int hours =
                        Integer.parseInt(parts[0]);

                int minutes =
                        Integer.parseInt(parts[1]);

                if (hours < 0 || hours > 23) {
                    throw new RuntimeException(
                            "Invalid hour: " + hours
                    );
                }

                if (minutes < 0 || minutes > 59) {
                    throw new RuntimeException(
                            "Invalid minutes: " + minutes
                    );
                }

                return hours * 60 + minutes;
            }

            // =====================================================
            // 2h / 2h 30m / 30m
            // =====================================================

            String lowerValue =
                    value.toLowerCase();

            if (lowerValue.contains("h")
                    || lowerValue.contains("m")) {

                int hours = 0;
                int minutes = 0;

                if (lowerValue.contains("h")) {

                    String hourPart =
                            lowerValue
                                    .substring(
                                            0,
                                            lowerValue.indexOf("h")
                                    )
                                    .trim();

                    hours =
                            Integer.parseInt(hourPart);
                }

                if (lowerValue.contains("m")) {

                    String minutePart =
                            lowerValue
                                    .substring(
                                            lowerValue.indexOf("h") + 1
                                    )
                                    .replace("m", "")
                                    .trim();

                    if (!minutePart.isEmpty()) {
                        minutes =
                                Integer.parseInt(minutePart);
                    }
                }

                return hours * 60 + minutes;
            }

            throw new RuntimeException(
                    "Unsupported time format: " + input
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid time format: " + input
            );
        }
    }
    private String formatMinutes(int total) {
        return (total / 60) + "h " + (total % 60) + "m";
    }

    private void validateLoginDistance(
            String clinicId,
            String branchId,
            String role) {

        ResponseEntity<Response> responseEntity =
                adminServiceClient.getClinicById(clinicId);

        if (responseEntity == null
                || responseEntity.getBody() == null
                || responseEntity.getBody().getData() == null) {
            throw new RuntimeException("Clinic location not found");
        }

        try {

            // Doctor & Physiotherapist — no branch requirement
            if ("doctor".equalsIgnoreCase(role)
                    || "physiotherapist".equalsIgnoreCase(role)) {
                return;
            }

            if (branchId == null || branchId.isBlank()) {
                throw new RuntimeException("branchId is required");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> clinic =
                    (Map<String, Object>) responseEntity.getBody().getData();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> branches =
                    (List<Map<String, Object>>) clinic.get("branches");

            if (branches == null || branches.isEmpty()) {
                throw new RuntimeException("No branches found for this clinic");
            }

            Map<String, Object> branch = null;

            for (Map<String, Object> b : branches) {
                if (branchId.equals(String.valueOf(b.get("branchId")))) {
                    branch = b;
                    break;
                }
            }

            if (branch == null) {
                throw new RuntimeException(
                        "Branch not found for clinicId: "
                                + clinicId
                                + " and branchId: "
                                + branchId
                );
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to validate branch: " + e.getMessage()
            );
        }
    }

    @Override
    public Response getDailyByClinicAndBranch(
            String clinicId,
            String branchId,
            String date) {

        Response response = new Response();

        try {

            // =========================================================
            // 1. GET ALL USERS
            // =========================================================

        	// =========================================================
        	// 1. GET ALL USERS
        	// =========================================================

        	// Get normal staff/receptionists from credentials
        	List<DoctorAndStaffLoginCredentials> credentialUsers =
        	        credentialsRepository.findByHospitalIdAndBranchId(
        	                clinicId,
        	                branchId
        	        );

        	// Get doctors using Doctors.branches[]
        	List<Doctors> doctors =
        	        doctorsRepository.findByHospitalIdAndBranchIdIncludingBranches(
        	                clinicId,
        	                branchId
        	        );

        	List<DailyAllUsersResponseDTO> result =
        	        new ArrayList<>();
        	
        	// =========================================================
        	// 2. ADD DOCTORS FROM Doctors.branches[]
        	// =========================================================

        	for (Doctors doctor : doctors) {

        	    DailyAllUsersResponseDTO dto =
        	            new DailyAllUsersResponseDTO();

        	    dto.setUserId(doctor.getDoctorId());
        	    dto.setName(doctor.getDoctorName());
        	    dto.setRole(doctor.getRole());
        	    dto.setClinicId(clinicId);
        	    dto.setBranchId(branchId);
        	    dto.setDate(date);

        	    // Default values
        	    dto.setStatus("Not Logged In");
        	    dto.setLogTime(null);
        	    dto.setWorkingHours("00:00");
        	    dto.setIdleTime("00:00");
        	    dto.setLateTime("0m");
        	    dto.setReason(null);
        	    dto.setOvertime("0m");
        	    dto.setLogin(null);
        	    dto.setLogout(null);

        	    // Get attendance for this exact branch
        	    Optional<Attendance> attendanceOpt =
        	            repo.findByClinicIdAndBranchIdAndUserIdAndDate(
        	                    clinicId,
        	                    branchId,
        	                    doctor.getDoctorId(),
        	                    date
        	            );

        	    if (attendanceOpt.isPresent()) {

        	        Attendance entity =
        	                attendanceOpt.get();

        	        dto.setStatus(
        	                entity.getStatus() != null
        	                        ? entity.getStatus()
        	                        : "Not Logged In"
        	        );

        	        dto.setLogTime(entity.getLogTime());

        	        dto.setWorkingHours(
        	                entity.getWorkingHours() != null
        	                        ? entity.getWorkingHours()
        	                        : "00:00"
        	        );

        	        dto.setIdleTime(
        	                entity.getIdleTime() != null
        	                        ? entity.getIdleTime()
        	                        : "00:00"
        	        );

        	        dto.setLateTime(
        	                entity.getLateTime() != null
        	                        ? entity.getLateTime()
        	                        : "0m"
        	        );

        	        if ("ABSENT".equalsIgnoreCase(entity.getStatus())) {
        	            dto.setReason(entity.getReason());
        	        }

        	        dto.setOvertime(
        	                entity.getOvertime() != null
        	                        ? entity.getOvertime()
        	                        : "0m"
        	        );

        	        if (entity.getLogin() != null) {

        	            TimeLocationDTO login =
        	                    new TimeLocationDTO();

        	            login.setTime(entity.getLogin().getTime());
        	            login.setReason(entity.getLogin().getReason());

        	            dto.setLogin(login);
        	        }

        	        if (entity.getLogout() != null) {

        	            TimeLocationDTO logout =
        	                    new TimeLocationDTO();

        	            logout.setTime(entity.getLogout().getTime());
        	            logout.setReason(entity.getLogout().getReason());

        	            dto.setLogout(logout);
        	        }
        	    }

        	    result.add(dto);
        	}
            // =========================================================
            // 2. LOOP THROUGH ALL USERS
            // =========================================================

        	for (DoctorAndStaffLoginCredentials user : credentialUsers) {

                // =====================================================
                // SKIP ADMIN
                // =====================================================

        		if ("ADMIN".equalsIgnoreCase(user.getRole())) {
        		    continue;
        		}

        		if ("DOCTOR".equalsIgnoreCase(user.getRole())
        		        || "PHYSIOTHERAPIST".equalsIgnoreCase(user.getRole())) {
        		    continue;
        		}

                DailyAllUsersResponseDTO dto =
                        new DailyAllUsersResponseDTO();

                dto.setUserId(user.getStaffId());
                dto.setName(user.getStaffName());
                dto.setRole(user.getRole());
                dto.setClinicId(clinicId);
                dto.setBranchId(branchId);
                dto.setDate(date);

                // =====================================================
                // DEFAULT VALUES
                // =====================================================

                dto.setStatus("Not Logged In");

                dto.setLogTime(null);

                dto.setWorkingHours("00:00");

                dto.setIdleTime("00:00");

                dto.setLateTime("0m");

                // Top-level reason is ONLY for ABSENT
                dto.setReason(null);

                dto.setOvertime("0m");

                dto.setLogin(null);

                dto.setLogout(null);

                // =====================================================
                // 3. GET ATTENDANCE
                //
                // DOCTOR
                // PHYSIOTHERAPIST
                // RECEPTIONIST
                //
                // ALL ARE STORED IN Attendance COLLECTION
                // =====================================================

                Optional<Attendance> attendanceOpt =
                        repo.findByClinicIdAndBranchIdAndUserIdAndDate(
                                clinicId,
                                branchId,
                                user.getStaffId(),
                                date
                        );

               

                // =====================================================
                // 5. ATTENDANCE FOUND
                // =====================================================

                if (attendanceOpt.isPresent()) {

                    Attendance entity =
                            attendanceOpt.get();

                    // =============================================
                    // STATUS
                    // =============================================

                    dto.setStatus(
                            entity.getStatus() != null
                                    ? entity.getStatus()
                                    : "Not Logged In"
                    );

                    // =============================================
                    // LOG TIME
                    // =============================================

                    dto.setLogTime(
                            entity.getLogTime()
                    );

                    // =============================================
                    // WORKING HOURS
                    // =============================================

                    dto.setWorkingHours(
                            entity.getWorkingHours() != null
                                    ? entity.getWorkingHours()
                                    : "00:00"
                    );

                    // =============================================
                    // IDLE TIME
                    // =============================================

                    dto.setIdleTime(
                            entity.getIdleTime() != null
                                    ? entity.getIdleTime()
                                    : "00:00"
                    );

                    // =============================================
                    // LATE TIME
                    // =============================================

                    dto.setLateTime(
                            entity.getLateTime() != null
                                    ? entity.getLateTime()
                                    : "0m"
                    );

                    // =============================================
                    // ABSENT REASON
                    //
                    // Top-level reason is ONLY for ABSENT
                    // =============================================

                    if ("ABSENT".equalsIgnoreCase(
                            entity.getStatus())) {

                        dto.setReason(
                                entity.getReason()
                        );

                    } else {

                        dto.setReason(null);
                    }

                    // =============================================
                    // OVERTIME
                    // =============================================

                    dto.setOvertime(
                            entity.getOvertime() != null
                                    ? entity.getOvertime()
                                    : "0m"
                    );

                    // =============================================
                    // LOGIN
                    //
                    // Login reason = Late login reason
                    // =============================================

                    if (entity.getLogin() != null) {

                        TimeLocationDTO login =
                                new TimeLocationDTO();

                        login.setTime(
                                entity.getLogin().getTime()
                        );

                        login.setReason(
                                entity.getLogin().getReason()
                        );

                        dto.setLogin(login);
                    }

                    // =============================================
                    // LOGOUT
                    //
                    // Logout reason = Early logout reason
                    // =============================================

                    if (entity.getLogout() != null) {

                        TimeLocationDTO logout =
                                new TimeLocationDTO();

                        logout.setTime(
                                entity.getLogout().getTime()
                        );

                        logout.setReason(
                                entity.getLogout().getReason()
                        );

                        dto.setLogout(logout);
                    }

                    // =============================================
                    // SELF-HEAL CLINIC / BRANCH
                    // =============================================

                    boolean needsUpdate = false;

                    if (entity.getClinicId() == null
                            || entity.getClinicId().isBlank()) {

                        entity.setClinicId(clinicId);

                        needsUpdate = true;
                    }

                    if (entity.getBranchId() == null
                            || entity.getBranchId().isBlank()) {

                        entity.setBranchId(branchId);

                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        repo.save(entity);
                    }
                }

                // =====================================================
                // 6. ADD TO RESULT
                // =====================================================

                result.add(dto);
            }

            // =========================================================
            // 7. RESPONSE
            // =========================================================

            response.setSuccess(true);

            response.setMessage(
                    "Today's attendance for all users fetched successfully"
            );

            response.setData(result);

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(404);
        }

        return response;
    }
    @Override
    public Response getMonthlyByClinicAndBranch(
            String clinicId,
            String branchId,
            String userId,
            String startDate,
            String endDate) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATION
            // =====================================================

            if (clinicId == null || clinicId.isBlank()
                    || branchId == null || branchId.isBlank()
                    || userId == null || userId.isBlank()
                    || startDate == null || startDate.isBlank()
                    || endDate == null || endDate.isBlank()) {

                throw new RuntimeException(
                        "clinicId, branchId, userId, startDate and endDate are required"
                );
            }

            if (startDate.compareTo(endDate) > 0) {

                throw new RuntimeException(
                        "startDate must not be after endDate"
                );
            }

            // =====================================================
            // 2. GET ATTENDANCE
            // =====================================================

            List<Attendance> attendanceList =
                    repo.findByClinicIdAndBranchIdAndUserIdAndDateBetween(
                            clinicId,
                            branchId,
                            userId,
                            startDate,
                            endDate
                    );

//            if (attendanceList == null
//                    || attendanceList.isEmpty()) {
//
//                attendanceList =
//                        repo.findByUserIdAndDateBetween(
//                                userId,
//                                startDate,
//                                endDate
//                        );
//            }

            // =====================================================
            // 3. GET USER ROLE
            // =====================================================

            String role = null;

            if (attendanceList != null
                    && !attendanceList.isEmpty()) {

                Attendance firstAttendance =
                        attendanceList.get(0);

                role = firstAttendance.getRole();
            }

            // =====================================================
            // 4. GET SHIFT
            //
            // DOCTOR / PHYSIOTHERAPIST
            //     -> Doctors.availableTimes
            //
            // RECEPTIONIST
            //     -> Clinic openingTime / closingTime
            // =====================================================

            String shiftStartTime =
                    getShiftStartTime(
                            userId,
                            role,
                            clinicId,
                            branchId
                    );

            String shiftEndTime =
                    getShiftEndTime(
                            userId,
                            role,
                            clinicId,
                            branchId
                    );

            // =====================================================
            // 5. CALCULATE SHIFT DURATION
            // =====================================================

            String shift =
                    calculateShiftDuration(
                            shiftStartTime,
                            shiftEndTime
                    );

            // =====================================================
            // 6. CREATE RESULT
            // =====================================================

            List<MonthlyAttendanceResponseDTO> result =
                    new ArrayList<>();

            // =====================================================
            // 7. DAY-BY-DAY ATTENDANCE
            // =====================================================

            for (Attendance att : attendanceList) {

                MonthlyAttendanceResponseDTO dto =
                        new MonthlyAttendanceResponseDTO();

                // =================================================
                // DATE
                // =================================================

                dto.setDate(
                        att.getDate()
                );

                // =================================================
                // ASSIGNED SHIFT
                // =================================================

                dto.setShift(
                        shift
                );

                // =================================================
                // LOGIN
                // =================================================

                if (att.getLogin() != null) {

                    dto.setInTime(
                            att.getLogin().getTime()
                    );
                }

                // =================================================
                // LOGOUT
                // =================================================

                if (att.getLogout() != null) {

                    dto.setOutTime(
                            att.getLogout().getTime()
                    );
                }

                // =================================================
                // LOG TIME
                // =================================================

                dto.setLogTime(
                        att.getLogTime()
                );

                // =================================================
                // ACTUAL WORKING HOURS
                // =================================================

                dto.setWorkingHours(
                        att.getWorkingHours()
                );

                // =================================================
                // IDLE TIME
                // =================================================

                dto.setIdleTime(
                        att.getIdleTime()
                );

                result.add(dto);
            }

            // =====================================================
            // 8. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    "Attendance report for user "
                            + userId
                            + " from "
                            + startDate
                            + " to "
                            + endDate
                            + " fetched successfully"
            );

            response.setData(result);

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(400);
        }

        return response;
    }
    @Override
    public Response getUserDetailsByMobile(String mobileNumber) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATE MOBILE NUMBER
            // =====================================================

            if (mobileNumber == null || mobileNumber.isBlank()) {
                throw new RuntimeException("Mobile number is required");
            }

            // =====================================================
            // 2. GET USER FROM LOGIN CREDENTIALS
            // =====================================================

            Optional<DoctorAndStaffLoginCredentials> optional =
                    credentialsRepository.findByMobilenumber(mobileNumber);

            if (optional.isEmpty()) {
                throw new RuntimeException(
                        "User not found with mobile number: " + mobileNumber
                );
            }

            DoctorAndStaffLoginCredentials user = optional.get();

            // =====================================================
            // 3. CREATE DTO
            // =====================================================

            UserLoginDetailsDTO dto = new UserLoginDetailsDTO();

            // =====================================================
            // CLINIC DETAILS
            // =====================================================

            dto.setClinicId(user.getHospitalId());

            // =====================================================
            // BRANCH DETAILS
            // branchName = branch location
            // =====================================================

            dto.setBranchId(user.getBranchId());
            dto.setBranchLocation(user.getBranchName());

            // =====================================================
            // USER DETAILS
            // =====================================================

            dto.setUserId(user.getStaffId());

            // staffName should be returned
            dto.setUsername(user.getStaffName());

            dto.setRole(user.getRole());
            dto.setMobileNumber(user.getMobilenumber());

            // =====================================================
            // 4. GET CLINIC NAME USING HOSPITAL ID
            // =====================================================

            ResponseEntity<Response> clinicResponse =
                    adminServiceClient.getClinicById(user.getHospitalId());

            if (clinicResponse == null
                    || clinicResponse.getBody() == null
                    || clinicResponse.getBody().getData() == null) {

                throw new RuntimeException(
                        "Clinic not found for clinicId: "
                                + user.getHospitalId()
                );
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> clinic =
                    (Map<String, Object>) clinicResponse
                            .getBody()
                            .getData();

            // =====================================================
            // CLINIC COLLECTION FIELD IS "name"
            // =====================================================

            Object clinicName = clinic.get("name");

            if (clinicName == null
                    || String.valueOf(clinicName).isBlank()) {

                throw new RuntimeException(
                        "Clinic name not found for clinicId: "
                                + user.getHospitalId()
                );
            }

            dto.setClinicName(String.valueOf(clinicName));

            // =====================================================
            // 5. SET RESPONSE
            // =====================================================

            response.setSuccess(true);
            response.setData(dto);
            response.setMessage("User details fetched successfully");
            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);
            response.setData(null);
            response.setMessage(e.getMessage());
            response.setStatus(404);
        }

        return response;
    }
    
    @Override
    public Response updateStatus(
            String userId,
            String clinicId,
            String branchId,
            String status,
            String reason) {

        Response response = new Response();

        try {

            // =====================================================
            // 1. VALIDATION
            // =====================================================

            if (userId == null || userId.isBlank()) {
                throw new RuntimeException(
                        "userId is required"
                );
            }

            if (clinicId == null || clinicId.isBlank()) {
                throw new RuntimeException(
                        "clinicId is required"
                );
            }

            if (branchId == null || branchId.isBlank()) {
                throw new RuntimeException(
                        "branchId is required"
                );
            }

            if (status == null || status.isBlank()) {
                throw new RuntimeException(
                        "status is required"
                );
            }

            // =====================================================
            // 2. TODAY'S DATE
            // =====================================================

            String today =
                    java.time.LocalDate.now().toString();

            // =====================================================
            // 3. NORMALIZE STATUS
            // =====================================================

            String updatedStatus =
                    status.trim().toUpperCase();

            // =====================================================
            // 4. FIND TODAY'S ATTENDANCE
            // =====================================================

            Optional<Attendance> optional =
                    repo.findByClinicIdAndBranchIdAndUserIdAndDate(
                            clinicId,
                            branchId,
                            userId,
                            today
                    );

            // =====================================================
            // 5. FALLBACK
            // =====================================================

            if (optional.isEmpty()) {

                optional =
                        repo.findByUserIdAndDate(
                                userId,
                                today
                        );
            }

            Attendance attendance;

            // =====================================================
            // 6. ATTENDANCE EXISTS
            // =====================================================

            if (optional.isPresent()) {

                attendance = optional.get();

            } else {

                // =================================================
                // 7. NO ATTENDANCE
                //
                // For ABSENT we create a new attendance record.
                // =================================================

                if ("ABSENT".equalsIgnoreCase(updatedStatus)) {

                    attendance = new Attendance();

                    attendance.setUserId(userId);

                    attendance.setClinicId(clinicId);

                    attendance.setBranchId(branchId);

                    attendance.setDate(today);

                    // =================================================
                    // NO LOGIN / LOGOUT FOR ABSENT
                    // =================================================

                    attendance.setLogin(null);

                    attendance.setLogout(null);

                    attendance.setLogTime(null);

                    attendance.setWorkingHours("0h 0m");

                    attendance.setIdleTime("0h 0m");

                    attendance.setLateTime("0m");

                    attendance.setOvertime("0m");

                } else {

                    // =================================================
                    // OTHER STATUS
                    //
                    // If employee has not logged in, don't create
                    // attendance for statuses that require attendance.
                    // =================================================

                    throw new RuntimeException(
                            "Attendance not found for userId: "
                                    + userId
                    );
                }
            }

            // =====================================================
            // 8. UPDATE STATUS
            // =====================================================

            attendance.setStatus(
                    updatedStatus
            );

            // =====================================================
            // 9. UPDATE REASON
            // =====================================================

            if (reason != null
                    && !reason.isBlank()) {

                attendance.setReason(
                        reason.trim()
                );

            } else {

                attendance.setReason(null);
            }

            // =====================================================
            // 10. SAVE ATTENDANCE
            // =====================================================

            Attendance saved =
                    repo.save(attendance);

            // =====================================================
            // 11. RESPONSE
            // =====================================================

            response.setSuccess(true);

            response.setMessage(
                    "Attendance status and reason updated successfully"
            );

            response.setData(saved);

            response.setStatus(200);

        } catch (Exception e) {

            response.setSuccess(false);

            response.setMessage(
                    e.getMessage()
            );

            response.setData(null);

            response.setStatus(400);
        }

        return response;
    }
    private String getShiftStartTime(
            String userId,
            String role,
            String clinicId,
            String branchId) {

        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("User ID is required");
        }

        // =====================================================
        // DOCTOR / PHYSIOTHERAPIST
        // Both are stored in Doctors collection
        // =====================================================

        if ("DOCTOR".equalsIgnoreCase(role)
                || "PHYSIOTHERAPIST".equalsIgnoreCase(role)) {

            Optional<Doctors> doctorOpt =
                    doctorsRepository.findByDoctorId(userId);

            if (doctorOpt.isEmpty()) {
                throw new RuntimeException(
                        "Doctor/Physiotherapist not found for doctorId: "
                                + userId
                );
            }

            Doctors doctor = doctorOpt.get();

            // =====================================================
            // CHECK CLINIC
            // =====================================================

            if (clinicId != null
                    && !clinicId.isBlank()
                    && !clinicId.equals(doctor.getHospitalId())) {

                throw new RuntimeException(
                        "User does not belong to clinicId: "
                                + clinicId
                );
            }

            // =====================================================
            // CHECK BRANCH
            // =====================================================

            if (branchId != null && !branchId.isBlank()) {

                boolean branchExists = doctor.getBranches() != null
                        && doctor.getBranches().stream()
                                .anyMatch(branch ->
                                        branchId.equals(branch.getBranchId())
                                );

                if (!branchExists) {
                    throw new RuntimeException(
                            "Doctor does not belong to branchId: "
                                    + branchId
                    );
                }
            }

            // =====================================================
            // GET SHIFT FROM DOCTOR
            // =====================================================

            String availableTimes =
                    doctor.getAvailableTimes();

            if (availableTimes == null
                    || availableTimes.isBlank()) {

                throw new RuntimeException(
                        "Shift timing not configured for userId: "
                                + userId
                );
            }

            if (!availableTimes.contains("-")) {

                throw new RuntimeException(
                        "Invalid shift timing: "
                                + availableTimes
                );
            }

            // Example:
            // 07:00 AM - 10:00 PM
            //
            // Returns:
            // 07:00 AM

            return availableTimes
                    .split("-", 2)[0]
                    .trim();
        }

        // =====================================================
        // RECEPTIONIST
        //
        // Receptionist shift comes from Clinic Onboarding
        //
        // openingTime = shift start
        // =====================================================

        if ("RECEPTIONIST".equalsIgnoreCase(role)) {

            if (clinicId == null || clinicId.isBlank()) {

                throw new RuntimeException(
                        "clinicId is required for receptionist shift"
                );
            }

            // =================================================
            // GET CLINIC FROM CLINIC ONBOARDING
            // =================================================

            ResponseEntity<Response> clinicResponse =
                    adminServiceClient.getClinicById(clinicId);

            if (clinicResponse == null
                    || clinicResponse.getBody() == null
                    || clinicResponse.getBody().getData() == null) {

                throw new RuntimeException(
                        "Clinic not found for clinicId: "
                                + clinicId
                );
            }

            // =================================================
            // GET CLINIC DATA
            // =================================================

            @SuppressWarnings("unchecked")
            Map<String, Object> clinic =
                    (Map<String, Object>)
                            clinicResponse.getBody().getData();

            // =================================================
            // GET OPENING TIME
            // =================================================

            Object openingTime =
                    clinic.get("openingTime");

            if (openingTime == null
                    || String.valueOf(openingTime).isBlank()) {

                throw new RuntimeException(
                        "Clinic openingTime not configured for clinicId: "
                                + clinicId
                );
            }

            // =================================================
            // RETURN CLINIC OPENING TIME
            // =================================================

            return String.valueOf(openingTime).trim();
        }

        // =====================================================
        // UNSUPPORTED ROLE
        // =====================================================

        throw new RuntimeException(
                "Shift timing not supported for role: "
                        + role
        );
    }
    }