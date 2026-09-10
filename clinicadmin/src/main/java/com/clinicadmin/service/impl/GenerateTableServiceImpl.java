package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.clinicadmin.dto.ExerciseResponseDTO;
import com.clinicadmin.dto.PhysiotherapyRecordDTO;
import com.clinicadmin.dto.ProgramResponseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.SessionDTO;
import com.clinicadmin.dto.TherapyResponseDTO;
import com.clinicadmin.feignclient.PhysiotherapyFeignClient;
import com.clinicadmin.service.GenerateTableService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateTableServiceImpl implements GenerateTableService {

    private final PhysiotherapyFeignClient feignClient;

    @Override
    @SuppressWarnings("unchecked")
    public Response generateTable(PhysiotherapyRecordDTO request) {

        Response response = new Response();

        try {

            Response doctorResponse = feignClient.getRecord(
                    request.getClinicId(),
                    request.getBranchId(),
                    request.getPatientId(),
                    request.getBookingId(),
                    request.getTherapistRecordId()
            );

            if (!doctorResponse.isSuccess()) {
                return doctorResponse;
            }

            Map<String, Object> record =
                    (Map<String, Object>) doctorResponse.getData();

            List<Map<String, Object>> therapySessions =
                    (List<Map<String, Object>>) record.get("therapySessions");

            if (therapySessions == null || therapySessions.isEmpty()) {
                therapySessions =
                        (List<Map<String, Object>>) record.get("therapyWithSessions");
            }

            if (therapySessions == null || therapySessions.isEmpty()) {
                throw new RuntimeException("No therapySessions found");
            }

            LocalDate startDate = LocalDate.parse(request.getStartDate());

            List<ProgramResponseDTO> result = new ArrayList<>();

            for (Map<String, Object> session : therapySessions) {

                List<Map<String, Object>> programs =
                        (List<Map<String, Object>>) session.get("programs");

                List<Map<String, Object>> packageData =
                        (List<Map<String, Object>>) session.get("packageData");

                List<Map<String, Object>> therapyData =
                        (List<Map<String, Object>>) session.get("therapyData");

                // A session is treated as a PACKAGE session if it has packageData,
                // or a packageId/packageName marker, even when packageData itself
                // is empty and the real data has to be pulled from a fallback source.
                boolean isPackageSession =
                        (packageData != null && !packageData.isEmpty())
                                || session.get("packageId") != null
                                || session.get("packageName") != null;

                // =========================================
                // PACKAGE
                // Mutually exclusive with PROGRAM and THERAPY below -
                // this is what stops "programs" (or a duplicate "therapy"
                // entry) from also showing up when a package is selected.
                // =========================================
                if (isPackageSession) {

                    List<Map<String, Object>> sourceData = packageData;

                    if (sourceData == null || sourceData.isEmpty()) {

                        if (therapyData != null && !therapyData.isEmpty()) {
                            sourceData = therapyData;

                        } else if (programs != null && !programs.isEmpty()) {

                            List<Map<String, Object>> pTherapy =
                                    (List<Map<String, Object>>) programs.get(0).get("therapyData");

                            if (pTherapy != null && !pTherapy.isEmpty()) {
                                sourceData = pTherapy;
                            }
                        }
                    }

                    if (sourceData != null && !sourceData.isEmpty()) {

                        ProgramResponseDTO dto =
                                buildProgramDTO(sourceData, startDate);

                        dto.setSourceType("PACKAGE");
                        dto.setPackageId(
                                session.get("packageId") != null
                                        ? session.get("packageId").toString()
                                        : ""
                        );
                        dto.setPackageName(
                                session.get("packageName") != null
                                        ? session.get("packageName").toString()
                                        : ""
                        );

                        result.add(dto);
                    }

                } else {

                    // =========================================
                    // PROGRAM
                    // =========================================
                    if (programs != null && !programs.isEmpty()) {

                        for (Map<String, Object> program : programs) {

                            List<Map<String, Object>> therapyDataList =
                                    (List<Map<String, Object>>) program.get("therapyData");

                            if (therapyDataList != null && !therapyDataList.isEmpty()) {

                                ProgramResponseDTO dto =
                                        buildProgramDTO(therapyDataList, startDate);

                                dto.setSourceType("PROGRAM");
                                dto.setProgramId(
                                        program.get("programId") != null
                                                ? program.get("programId").toString()
                                                : ""
                                );
                                dto.setProgramName(
                                        program.get("programName") != null
                                                ? program.get("programName").toString()
                                                : ""
                                );

                                result.add(dto);
                            }
                        }
                    }

                    // =========================================
                    // THERAPY
                    // =========================================
                    if (therapyData != null && !therapyData.isEmpty()) {

                        ProgramResponseDTO dto =
                                buildProgramDTO(therapyData, startDate);

                        dto.setSourceType("THERAPY");
                        result.add(dto);
                    }
                }

                // =========================================
                // EXERCISE
                // Stays independent of the branch above - these are
                // ad-hoc exercises attached directly to the session.
                // =========================================
                List<Map<String, Object>> exercises =
                        (List<Map<String, Object>>) session.get("exercises");

                if (exercises != null && !exercises.isEmpty()) {

                    List<Map<String, Object>> singleTherapy = new ArrayList<>();
                    Map<String, Object> therapyMap = new HashMap<>();

                    String therapyId = "";
                    String therapyName = "";

                    if (therapyData != null && !therapyData.isEmpty()) {

                        therapyId = therapyData.get(0).get("therapyId") != null
                                ? therapyData.get(0).get("therapyId").toString()
                                : "";

                        therapyName = therapyData.get(0).get("therapyName") != null
                                ? therapyData.get(0).get("therapyName").toString()
                                : "";

                    } else if (packageData != null && !packageData.isEmpty()) {

                        therapyId = packageData.get(0).get("therapyId") != null
                                ? packageData.get(0).get("therapyId").toString()
                                : "";

                        therapyName = packageData.get(0).get("therapyName") != null
                                ? packageData.get(0).get("therapyName").toString()
                                : "";

                    } else if (programs != null && !programs.isEmpty()) {

                        List<Map<String, Object>> pTherapy =
                                (List<Map<String, Object>>) programs.get(0).get("therapyData");

                        if (pTherapy != null && !pTherapy.isEmpty()) {

                            therapyId = pTherapy.get(0).get("therapyId") != null
                                    ? pTherapy.get(0).get("therapyId").toString()
                                    : "";

                            therapyName = pTherapy.get(0).get("therapyName") != null
                                    ? pTherapy.get(0).get("therapyName").toString()
                                    : "";
                        }
                    }

                    therapyMap.put("therapyId", therapyId);
                    therapyMap.put("therapyName", therapyName);
                    therapyMap.put("exercises", exercises);

                    singleTherapy.add(therapyMap);

                    ProgramResponseDTO dto =
                            buildProgramDTO(singleTherapy, startDate);

                    dto.setSourceType("EXERCISE");
                    result.add(dto);
                }
            }

            response.setSuccess(true);
            response.setData(result);
            response.setStatus(200);
            response.setMessage("Table generated successfully");

        } catch (Exception e) {

            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setStatus(500);
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private ProgramResponseDTO buildProgramDTO(
            List<Map<String, Object>> therapyDataList,
            LocalDate startDate) {

        ProgramResponseDTO programDTO = new ProgramResponseDTO();
        programDTO.setTherapyData(new ArrayList<>());

        for (Map<String, Object> therapyData : therapyDataList) {

            TherapyResponseDTO therapyDTO = new TherapyResponseDTO();

            therapyDTO.setTherapyId(
                    therapyData.get("therapyId") != null
                            ? therapyData.get("therapyId").toString()
                            : ""
            );

            therapyDTO.setTherapyName(
                    therapyData.get("therapyName") != null
                            ? therapyData.get("therapyName").toString()
                            : ""
            );

            List<Map<String, Object>> exercises =
                    (List<Map<String, Object>>) therapyData.get("exercises");

            List<ExerciseResponseDTO> exerciseList = new ArrayList<>();

            if (exercises != null && !exercises.isEmpty()) {

                for (Map<String, Object> ex : exercises) {

                    ExerciseResponseDTO exDTO = new ExerciseResponseDTO();

                    exDTO.setExerciseId(
                            ex.get("exerciseId") != null
                                    ? ex.get("exerciseId").toString()
                                    : "EX"
                    );

                    exDTO.setExerciseName(
                            ex.get("exerciseName") != null
                                    ? ex.get("exerciseName").toString()
                                    : ""
                    );

                    exDTO.setSets(
                            ex.get("sets") != null
                                    ? ((Number) ex.get("sets")).intValue()
                                    : 0
                    );

                    exDTO.setRepetitions(
                            ex.get("repetitions") != null
                                    ? ((Number) ex.get("repetitions")).intValue()
                                    : 0
                    );

                    String frequency =
                            ex.get("frequency") != null
                                    ? ex.get("frequency").toString()
                                    : "1";

                    frequency = frequency.trim();

                    if (frequency.matches("\\d+")) {
                        frequency = frequency + "day";
                    }

                    frequency = frequency.toLowerCase().replace(" ", "");

                    exDTO.setFrequency(frequency);

                    int totalSessions =
                            ex.get("noOfSessions") != null
                                    ? Integer.parseInt(
                                    ex.get("noOfSessions").toString())
                                    : 1;

                    exDTO.setNoOfSessions(totalSessions);

                    exDTO.setPricePerSession(
                            ex.get("pricePerSession") != null
                                    ? Double.parseDouble(ex.get("pricePerSession").toString())
                                    : 0.0
                    );

                    exDTO.setTotalPricePerSession(
                            ex.get("totalPrice") != null
                                    ? Double.parseDouble(ex.get("totalPrice").toString())
                                    : 0.0
                    );

                    List<SessionDTO> sessions = new ArrayList<>();

                    List<Map<String, Object>> existingSessions =
                            (List<Map<String, Object>>) ex.get("sessions");

                    if (existingSessions != null && !existingSessions.isEmpty()) {

                        for (Map<String, Object> s : existingSessions) {

                            SessionDTO sessionDTO = new SessionDTO();

                            sessionDTO.setSessionId(
                                    s.get("sessionId") != null
                                            ? s.get("sessionId").toString()
                                            : ""
                            );

                            sessionDTO.setDate(
                                    s.get("date") != null
                                            ? s.get("date").toString()
                                            : ""
                            );

                            sessionDTO.setStatus(
                                    s.get("status") != null
                                            ? s.get("status").toString()
                                            : "Pending"
                            );

                            sessionDTO.setPaymentStatus(
                                    s.get("paymentStatus") != null
                                            ? s.get("paymentStatus").toString()
                                            : "unpaid"
                            );

                            sessions.add(sessionDTO);
                        }

                    } else {

                        sessions = generateSessions(
                                startDate,
                                frequency,
                                totalSessions
                        );
                    }

                    exDTO.setSessions(sessions);
                    exerciseList.add(exDTO);
                }
            }

            therapyDTO.setExercises(exerciseList);
            programDTO.getTherapyData().add(therapyDTO);
        }

        return programDTO;
    }

    // ==================================================
    // SESSION GENERATION
    // ==================================================
    private List<SessionDTO> generateSessions(
            LocalDate startDate,
            String frequency,
            int total
    ) {

        List<SessionDTO> list = new ArrayList<>();

        // Normalize frequency
        // Examples:
        // 2times/week
        // 3times/day
        // 1time/month
        frequency = frequency.toLowerCase().replace(" ", "");

        int times = 1;          // Number of sessions in one period
        String period = "day";  // day / week / month

        // ==========================================
        // Parse frequency
        // ==========================================
        if (frequency.matches("\\d+times?/\\w+")) {
            // Example: 2times/week, 1time/day
            String[] parts = frequency.split("times?/");
            times = Integer.parseInt(parts[0]);
            period = parts[1];

        } else if (frequency.matches("\\d+/\\w+")) {
            // Example: 2/week
            String[] parts = frequency.split("/");
            times = Integer.parseInt(parts[0]);
            period = parts[1];

        } else {
            // Fallback to old logic
            String number = frequency.replaceAll("[^0-9]", "");
            period = frequency.replaceAll("[0-9]", "");

            times = number.isEmpty() ? 1 : Integer.parseInt(number);

            period = period.replace("times/", "")
                    .replace("time/", "")
                    .replace("/", "");

            if (period.isEmpty()) {
                period = "day";
            }
        }

        LocalDate date = startDate;

        for (int i = 1; i <= total; i++) {

            SessionDTO s = new SessionDTO();

            s.setDate(
                    date.getMonthValue() + "/"
                            + date.getDayOfMonth() + "/"
                            + date.getYear()
            );

            s.setStatus("Pending");
            s.setPaymentStatus("unpaid");
            s.setSessionId(generateSessionId(date, i));

            list.add(s);

            // ==========================================
            // Date calculation based on frequency
            // ==========================================

            if (period.equalsIgnoreCase("day")
                    || period.equalsIgnoreCase("days")) {

                // For 2times/day with total=2:
                // Session 1 -> 16-May
                // Session 2 -> 17-May
                // Session 3 -> 18-May
                date = date.plusDays(1);

            } else if (period.equalsIgnoreCase("week")
                    || period.equalsIgnoreCase("weeks")) {

                // Example:
                // 2times/week -> every 3 days
                // 3times/week -> every 2 days
                int gap = Math.max(1, 7 / times);
                date = date.plusDays(gap);

            } else if (period.equalsIgnoreCase("month")
                    || period.equalsIgnoreCase("months")) {

                // Example:
                // 2times/month -> every 15 days
                int gap = Math.max(1, 30 / times);
                date = date.plusDays(gap);

            } else {
                // Default: next day
                date = date.plusDays(1);
            }
        }

        return list;
    }

    // ==================================================
    // SESSION ID GENERATOR
    // ==================================================
    private String generateSessionId(LocalDate date, int index) {

        String month =
                date.getMonth().toString().substring(0, 3);

        String day =
                String.format("%02d", date.getDayOfMonth());

        return "S"
                + String.format("%02d", index)
                + "-"
                + day
                + month
                + "-"
                + generateShortCode();
    }

    private String generateShortCode() {

        String chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 4; i++) {

            int idx =
                    (int) (Math.random() * chars.length());

            code.append(chars.charAt(idx));
        }

        return code.toString();
    }
}