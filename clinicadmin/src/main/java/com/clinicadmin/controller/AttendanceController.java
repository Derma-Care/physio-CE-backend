package com.clinicadmin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.AttendanceDTO;
import com.clinicadmin.dto.AttendanceStatusUpdateDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService service;

    // ✅ SAVE
    @PostMapping("/saveUserAttendence")
    public ResponseEntity<Response> save(@RequestBody AttendanceDTO dto) {

        Response response = service.save(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PutMapping("/updateUserAttendence")
    public ResponseEntity<Response> updateActivity(@RequestBody AttendanceDTO dto) {

        Response response = service.updateActivity(dto);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    // ✅ DAILY 
    @GetMapping("/getUserDailyAttendence/{userId}/{date}")
    public ResponseEntity<Response> getDaily(
            @PathVariable String userId,
            @PathVariable String date) {

        Response response = service.getDaily(userId, date);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/getUserMonthlyAttendence/{userId}/{month}")
    public ResponseEntity<Response> getMonthlyReport(
            @PathVariable String userId,
            @PathVariable String month) {

        Response response = service.getMonthlyReport(userId, month);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

//    // ✅ CLINIC + BRANCH
//    @GetMapping("/getClinic/{clinicId}/{branchId}/{date}")
//    public ResponseEntity<Response> getByClinicBranch(
//            @PathVariable String clinicId,
//            @PathVariable String branchId,
//            @PathVariable String date) {
//
//        Response response = service.getByClinicBranch(clinicId, branchId, date);
//
//        return ResponseEntity
//                .status(response.getStatus())
//                .body(response);
//    }
    
    @GetMapping("/getAllUsersDailyByClinicAndBranch/{clinicId}/{branchId}/{date}")
    public ResponseEntity<Response> getDailyByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String date) {

        // =========================================================
        // DATE IS STILL PASSED IN THE URL TO KEEP THE ORIGINAL API.
        // HOWEVER, INSIDE THE SERVICE METHOD, THIS DATE WILL BE
        // OVERRIDDEN WITH TODAY'S DATE USING LocalDate.now().
        // =========================================================
        Response response = service.getDailyByClinicAndBranch(
                clinicId,
                branchId,
                date
        );

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
    @GetMapping("/getMonthlyReportforUserStartDateToEndDate/{clinicId}/{branchId}/{userId}/{startDate}/{endDate}")
    public ResponseEntity<Response> getMonthlyByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String userId,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        Response response = service.getMonthlyByClinicAndBranch(
                clinicId, branchId, userId, startDate, endDate
        );

        return ResponseEntity.ok(response);
    }
    
  
 // =========================================================
 // STATIC ATTENDANCE PORTAL URL
 // =========================================================
 @GetMapping("/attendance-url")
 public ResponseEntity<Map<String, String>> getAttendanceUrl() {

     String attendanceUrl =
             "http://localhost:3001/attendance-kiosk"
             + "#/staff-attendance-portal"
             + "?clinicId=&branchId=&date=2026-08-07";

     return ResponseEntity.ok(
             Map.of("url", attendanceUrl)
     );
 }
 
 @GetMapping("/getUserDetailsByMobile/{mobileNumber}")
 public ResponseEntity<Response> getUserDetailsByMobile(
         @PathVariable String mobileNumber) {

     Response response = service.getUserDetailsByMobile(mobileNumber);

     return ResponseEntity
             .status(response.getStatus())
             .body(response);
 }
 
 @PutMapping("/attendanceStatus/{userId}/{clinicId}/{branchId}")
 public ResponseEntity<Response> updateStatus(
         @PathVariable String userId,
         @PathVariable String clinicId,
         @PathVariable String branchId,
         @RequestBody AttendanceStatusUpdateDTO dto) {

     Response response = service.updateStatus(
             userId,
             clinicId,
             branchId,
             dto.getStatus(),
             dto.getReason()
     );

     return ResponseEntity
             .status(response.getStatus())
             .body(response);
 }
}
