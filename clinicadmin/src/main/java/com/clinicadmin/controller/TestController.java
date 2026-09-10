package com.clinicadmin.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicadmin.dto.TestDTO;
import com.clinicadmin.service.TestService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/clinic-admin")
@RequiredArgsConstructor
public class TestController {
    private final TestService service;

    @PostMapping("/addTestName")
    public ResponseEntity<TestDTO> addTest(@RequestBody TestDTO dto) {

        return ResponseEntity.ok(service.addTest(dto));
    }

    @GetMapping("/getAllTests")
    public ResponseEntity<List<TestDTO>> getAllTests() {

        return ResponseEntity.ok(service.getAllTests());
    }

    @GetMapping("/getByTestsId/{testId}")
    public ResponseEntity<TestDTO> getByTestId(
            @PathVariable String testId) {

        return ResponseEntity.ok(service.getByTestId(testId));
    }

    @PutMapping("/updateByTestId/{testId}")
    public ResponseEntity<TestDTO> updateTest(
            @PathVariable String testId,
            @RequestBody TestDTO dto) {

        return ResponseEntity.ok(service.updateTest(testId, dto));
    }

    @DeleteMapping("/deleteByTestId/{testId}")
    public ResponseEntity<String> deleteTest(
            @PathVariable String testId) {

        return ResponseEntity.ok(service.deleteTest(testId));
    }
    @GetMapping("/getByClinicIdAndBranchIdAndTestId/{clinicId}/{branchId}/{testId}")
    public ResponseEntity<TestDTO> getByClinicIdAndBranchIdAndTestId(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @PathVariable String testId) {

        return ResponseEntity.ok(
                service.getByClinicIdAndBranchIdAndTestId(
                        clinicId,
                        branchId,
                        testId));
    }
    @GetMapping("/getByClinicIdAndBranchId/{clinicId}/{branchId}")
    public ResponseEntity<List<TestDTO>> getByClinicIdAndBranchId(
            @PathVariable String clinicId,
            @PathVariable String branchId) {

        return ResponseEntity.ok(
                service.getByClinicIdAndBranchId(clinicId, branchId));
    }
}