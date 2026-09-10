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

import com.clinicadmin.dto.LabTestDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.LabTestService;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class LabTestController {

    @Autowired
    private LabTestService labTestService;

    @PostMapping("/labtest/addLabTest")
    public ResponseEntity<Response> addLabTest(@RequestBody LabTestDTO dto) {
        Response response = labTestService.addLabTest(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/labtest/getAllLabTests")
    public ResponseEntity<Response> getAllLabTests() {
        Response response = labTestService.getAllLabTests();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/labtest/getLabTestById/{id}/{hospitalId}")
    public ResponseEntity<Response> getLabTestById(@PathVariable String id, @PathVariable String hospitalId) {
        Response response = labTestService.getLabTestById(id, hospitalId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/labtest/deleteLabTest/{id}/{hospitalId}")
    public ResponseEntity<Response> deleteLabTest(@PathVariable String id,@PathVariable String hospitalId) {
        Response response = labTestService.deleteLabTestById(id,hospitalId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/labtest/updateLabTest/{id}/{hospitalId}")
    public ResponseEntity<Response> updateLabTest(@PathVariable String id,@PathVariable String hospitalId, @RequestBody LabTestDTO dto) {
        Response response = labTestService.updateLabTestById(id,hospitalId, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/labtests/{hospitalId}")
    public ResponseEntity<Response> getLabTestsByHospitalId(@PathVariable String hospitalId) {
    	 Response response = labTestService.getAllLabTestsByHospitalId(hospitalId);
         return ResponseEntity.status(response.getStatus()).body(response);
       
    }
    @PostMapping("/addOrGetTest")
    public ResponseEntity<Response> addOrGetLabTest(@RequestBody LabTestDTO dto) {
        Response response = labTestService.addOrGetLabTest(dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


}
