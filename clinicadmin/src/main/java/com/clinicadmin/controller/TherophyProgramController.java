package com.clinicadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.TherophyProgramsDTO;
import com.clinicadmin.service.TherophyProgramService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clinic-admin/program")
@RequiredArgsConstructor
public class TherophyProgramController {

    private final TherophyProgramService service;

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<Response> create(@RequestBody TherophyProgramsDTO dto) {
        return service.create(dto);
    }

    // GET BY ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<Response> getById(@PathVariable String id) {
        return service.getById(id);
    }
    
    
    @GetMapping("/getBycIdAndbId/{cId}/{bId}")
    public ResponseEntity<Response> getBycIdAndbId(@PathVariable String cId,@PathVariable String bId) {
        return service.getByclinicAndBranchId(cId, bId);
    }
    
    @PostMapping("/getBycIdAndbIdAndId/{cId}/{bId}/{id}")
    public ResponseEntity<Response> getBycIdAndbIdAndId(@PathVariable String cId,@PathVariable String bId,@PathVariable String id) {
        return service.getByclinicAndBranchIdAndId(cId, bId, id);
    }

    // GET ALL
    @GetMapping("/getAll")
    public ResponseEntity<Response> getAll() {
        return service.getAll();
    }
    //get by clinic id
    @GetMapping("/getByclinicId/{clinicId}")
    public ResponseEntity<Response> getByClinicId(@PathVariable String clinicId) {
        return service.getByClinicId(clinicId);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<Response> update(
            @PathVariable String id,
            @RequestBody TherophyProgramsDTO dto) {
        return service.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> delete(@PathVariable String id) {
        return service.delete(id);
    }
    
}
