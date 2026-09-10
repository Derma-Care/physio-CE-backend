package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.clinicadmin.dto.RecoverySupportDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.RecoverySupportService;

@RestController
@RequestMapping("/clinic-admin")
public class RecoverySupportController {

    @Autowired
    private RecoverySupportService recoverySupportService;

    @PostMapping("/saveRecoverySupport")
    public Response saveRecoverySupport(@RequestBody RecoverySupportDTO dto) {
        return recoverySupportService.saveRecoverySupport(dto);
    }

    @GetMapping("/getAllRecoverySupports")
    public Response getAllRecoverySupports() {
        return recoverySupportService.getAllRecoverySupports();
    }

    @GetMapping("/getRecoverySupportById/{id}")
    public Response getRecoverySupportById(@PathVariable String id) {
        return recoverySupportService.getRecoverySupportById(id);
    }

    @GetMapping("/getRecoverySupportByClinicIdAndId/{clinicId}/{id}")
    public Response getRecoverySupportByClinicIdAndId(
            @PathVariable String clinicId,
            @PathVariable String id) {

        return recoverySupportService.getRecoverySupportByClinicIdAndId(clinicId, id);
    }
    
    @GetMapping("/getAllRecoverySupportsByClinicId/{clinicId}")
    public Response getAllRecoverySupportsByClinicId(
            @PathVariable String clinicId) {

        return recoverySupportService.getRecoverySupportsByClinicId(clinicId);
    }

    @PutMapping("/updateRecoverySupportById/{id}")
    public Response updateRecoverySupport(
            @PathVariable String id,
            @RequestBody RecoverySupportDTO dto) {

        return recoverySupportService.updateRecoverySupport(id, dto);
    }

    @DeleteMapping("/deleteRecoverySupportById/{id}")
    public Response deleteRecoverySupport(@PathVariable String id) {
        return recoverySupportService.deleteRecoverySupport(id);
    }
}