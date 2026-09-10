package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.ReportsDtoList;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.ReportsService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @PostMapping("/savereports")
    public ResponseEntity<Response> saveReports(@RequestBody ReportsDtoList dto) {
        Response response = reportsService.saveReports(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

 
     @GetMapping("/getallreports")
     public ResponseEntity<Response> getAllReports() {
    	 Response response = reportsService.getAllReports();
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     @GetMapping("/getReportByBookingId/{bookingId}")
     public ResponseEntity<Response> getReportsByBookingId(@PathVariable String bookingId) {
    	 Response response = reportsService.getReportsByBookingId(bookingId);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     
     @GetMapping("/getReportsBycustomerId/{customerId}")
     public ResponseEntity<Response> getReportsBycustomerId(@PathVariable String customerId) {
    	 Response response = reportsService.getReportsByCustomerId(customerId);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     
     @GetMapping("/getReportsByPatientIdAndBookingId/{patientId}/{bookingId}")
     public ResponseEntity<Response> getReportsByPatientIdAndBookingId(
             @PathVariable String patientId,
             @PathVariable String bookingId) {
         Response response = reportsService.getReportsByPatientIdAndBookingId(patientId, bookingId);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     
     @PutMapping("/updateReport/{reportId}")
     public ResponseEntity<Response> updateReport(@PathVariable String reportId, @RequestBody ReportsDtoList dto) {
         Response response = reportsService.updateReport(reportId, dto);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     
   
     @DeleteMapping("/deleteReport/{reportId}")
     public ResponseEntity<Response> deleteReport(@PathVariable String reportId) {
         Response response = reportsService.deleteReport(reportId);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
     @DeleteMapping("/deleteReportFile/{reportId}/{bookingId}/{fileIndex}")
     public ResponseEntity<Response> deleteReportFile(
             @PathVariable String reportId,
             @PathVariable String bookingId,
             @PathVariable int fileIndex) {
         
         Response response = reportsService.deleteReportFile(reportId, bookingId, fileIndex);
         return ResponseEntity.status(response.getStatus()).body(response);
     }


}
