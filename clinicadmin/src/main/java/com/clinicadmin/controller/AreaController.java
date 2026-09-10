//package com.clinicadmin.controller;
//import com.clinicadmin.dto.AreaDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.service.AreaService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/clinic-admin/area")
//@RequiredArgsConstructor
//public class AreaController {
//    private final AreaService areaService;
//
//    @PostMapping("/api/pharmacy/area/save")
//    public Response addArea(@RequestBody AreaDTO dto) {
//        return areaService.saveArea(dto);
//    }
//
//    @PutMapping("/api/pharmacy/area/update/{id}")
//    public Response updateAreaById(@PathVariable String id,
//                               @RequestBody AreaDTO dto) {
//        return areaService.updateArea(id, dto);
//    }
//
//    @GetMapping("/api/pharmacy/area/getById/{id}")
//    public Response getAreaById(@PathVariable String id) {
//        return areaService.getAreaById(id);
//    }
//
//    @GetMapping("/api/pharmacy/area/all")
//    public Response getAllAreas() {
//        return areaService.getAllAreas();
//    }
//
//    @DeleteMapping("/api/pharmacy/area/delete/{id}")
//    public Response deleteAreaById(@PathVariable String id) {
//        return areaService.deleteArea(id);
//    }
//
//    @GetMapping("/api/pharmacy/area/city/{cityId}")
//    public Response getAreasByCity(@PathVariable String cityId) {
//        return areaService.getAreasByCity(cityId);
//    }
//}
