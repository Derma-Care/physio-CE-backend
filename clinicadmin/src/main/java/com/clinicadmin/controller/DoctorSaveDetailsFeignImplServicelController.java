//package com.clinicadmin.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.service.DoctorSaveDetailsFeignImplService;
//
//@RestController
//@RequestMapping("/clinic-admin")
////@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
//public class DoctorSaveDetailsFeignImplServicelController {
//	  @Autowired
//	    private DoctorSaveDetailsFeignImplService doctorSaveDetailsFeignImplService;
//
//	  
//	    @GetMapping("/visitHistory/{patientId}")
//	    public ResponseEntity<Response> getVisitHistoryByPatientId(@PathVariable String patientId) {
//	    	Response res = doctorSaveDetailsFeignImplService.getVisitHistoryByPatientId(patientId);
//	        return ResponseEntity.status(res.getStatus()).body(res);
//	    }
//}
