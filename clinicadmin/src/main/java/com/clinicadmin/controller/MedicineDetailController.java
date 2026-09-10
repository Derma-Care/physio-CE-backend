//package com.clinicadmin.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.clinicadmin.dto.MedicineDetailDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.service.MedicineDetailService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/clinic-admin")
//@RequiredArgsConstructor
//public class MedicineDetailController {
//
//    private final MedicineDetailService medicineDetailService;
//
//    @PostMapping("/api/pharmacy/addMedcineDetails")
//    public ResponseEntity<Response> addMedicine(@RequestBody MedicineDetailDTO dto) {
//        Response response = medicineDetailService.addMedicine(dto);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @GetMapping("/api/pharmacy/getMedicineById/{id}")
//    public ResponseEntity<Response> getMedicineById(@PathVariable String id) {
//        Response response = medicineDetailService.getMedicineById(id);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @GetMapping("/api/pharmacy/getAllMedicines")
//    public ResponseEntity<Response> getAllMedicines() {
//        Response response = medicineDetailService.getAllMedicines();
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @PutMapping("/api/pharmacy/updateMedicine/{id}")
//    public ResponseEntity<Response> updateMedicine(@PathVariable String id,
//                                                   @RequestBody MedicineDetailDTO dto) {
//        Response response = medicineDetailService.updateMedicine(id, dto);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//
//    @DeleteMapping("/api/pharmacy/deleteMedicine/{id}")
//    public ResponseEntity<Response> deleteMedicine(@PathVariable String id) {
//        Response response = medicineDetailService.deleteMedicine(id);
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }
//    
//    
//    @GetMapping("/api/pharmacy/getMedicineInventory/{medicineId}")
//    public ResponseEntity<Response> getMedicineInventory(String medicineId) {
//        return medicineDetailService.fetchMedicineInventory(medicineId);
//    }
//    
//}
