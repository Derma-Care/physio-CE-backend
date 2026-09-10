package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicadmin.dto.DoctorPrescriptionDTO;
import com.clinicadmin.dto.MedicineDTO;
import com.clinicadmin.dto.MedicineTypeDTO;
import com.clinicadmin.dto.PharmacistDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.service.PharmacistService;
import com.clinicadmin.validations.RequiredChecks;

@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PharmacistController {

	@Autowired
	private PharmacistService pharmacistService;

	// ------------------- Add Pharmacist -------------------
	@PostMapping("/addPharmacist")
	public ResponseEntity<Response> pharmacistOnBoarding(
			@Validated(RequiredChecks.class) @RequestBody PharmacistDTO dto) {
		Response response = pharmacistService.pharmacistOnboarding(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get All Pharmacists by Department -------------------
	@GetMapping("/getAllPharmacists/{hospitalId}")
	public ResponseEntity<Response> getAllByDepartment(@PathVariable String hospitalId) {
		Response response = pharmacistService.getAllPharmacistsByHospitalId(hospitalId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Get Single Pharmacist -------------------
	@GetMapping("/getPharmacist/{pharmacistId}")
	public ResponseEntity<Response> getPharmacist(@PathVariable String pharmacistId) {
		Response response = pharmacistService.getPharmacistById(pharmacistId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Update Pharmacist -------------------
	@PutMapping("/updatePharmacist/{pharmacistId}")
	public ResponseEntity<Response> updatePharmacist(@PathVariable String pharmacistId,
			@RequestBody PharmacistDTO dto) {
		Response response = pharmacistService.updatePharmacist(pharmacistId, dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	// ------------------- Delete Pharmacist -------------------
	@DeleteMapping("/deletePharmacist/{pharmacistId}")
	public ResponseEntity<Response> deletePharmacist(@PathVariable String pharmacistId) {
		Response response = pharmacistService.deletePharmacist(pharmacistId);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	@GetMapping("/getPharmacistsByHospitalIdAndBranchId/{hospitalId}/{branchId}")
	public ResponseEntity<Response> getPharmacistsByHospitalIdAndBranchId(
	        @PathVariable String hospitalId,
	        @PathVariable String branchId) {

	    Response response = pharmacistService.getPharmacistsByClinicIdAndBranchId(hospitalId, branchId);
	    return ResponseEntity.status(response.getStatus()).body(response);
	}




	// ---------------------------------------------------------------------------------------------------
	// --------------------------------- Pharmacist Login
	// ------------------------------------------------
	// ---------------------------------------------------------------------------------------------------

//	@PostMapping("/pharmacistLogin")
//	public ResponseEntity<Response> pharmacistLogin(@RequestBody PharmacistLoginDTO dto) {
//		Response response = pharmacistService.pharmacistLogin(dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
//
//	@PostMapping("/resetPharmacistLogin")
//	public ResponseEntity<Response> resetPharmacistLogin(@RequestBody ResetPharmacistLoginPasswordDTO dto) {
//		Response response = pharmacistService.resetLoginPassword(dto);
//		return ResponseEntity.status(response.getStatus()).body(response);
//	}
	
	// ---------------- PRESCRIPTION APIs ----------------

//    @PostMapping("/createPrescription")
//    public ResponseEntity<Response> createPrescription(@RequestBody DoctorPrescriptionDTO dto) {
//        return pharmacistService.createPrescription(dto);
//    }
//
//    @GetMapping("/getAllPrescriptions")
//    public ResponseEntity<Response> getAllPrescriptions() {
//        return pharmacistService.getAllPrescriptions();
//    }
//
//    @GetMapping("/getPrescriptionById/{id}")
//    public ResponseEntity<Response> getPrescriptionById(@PathVariable String id) {
//        return pharmacistService.getPrescriptionById(id);
//    }
//
//    @GetMapping("/getMedicineById/{medicineId}")
//    public ResponseEntity<Response> getMedicineById(@PathVariable String medicineId) {
//        return pharmacistService.getMedicineById(medicineId);
//    }
//
//    @GetMapping("/searchMedicines/{keyword}")
//    public ResponseEntity<Response> searchMedicines(@PathVariable String keyword) {
//        return pharmacistService.searchMedicines(keyword);
//    }
//
//    @DeleteMapping("/deletePrescription/{id}")
//    public ResponseEntity<Response> deletePrescription(@PathVariable String id) {
//        return pharmacistService.deletePrescription(id);
//    }
//
//    @DeleteMapping("/deleteMedicine/{medicineId}")
//    public ResponseEntity<Response> deleteMedicine(@PathVariable String medicineId) {
//        return pharmacistService.deleteMedicine(medicineId);
//    }
//
//    @GetMapping("/getPrescriptionsByClinicId/{clinicId}")
//    public ResponseEntity<Response> getPrescriptionsByClinicId(@PathVariable String clinicId) {
//        return pharmacistService.getPrescriptionsByClinicId(clinicId);
//    }
//    
//    @PutMapping("/updateMedicine/{medicineId}")
//    public ResponseEntity<Response> updateMedicine(@PathVariable String medicineId,
//                                                   @RequestBody MedicineDTO dto) {
//        return pharmacistService.updateMedicine(medicineId, dto);
//    }
//
//    
//    // ---------------- MEDICINE TYPE APIs ----------------
//    @PostMapping("/search-or-add")
//    public ResponseEntity<Response> searchOrAddMedicineType(@RequestBody MedicineTypeDTO dto) {
//        return pharmacistService.searchOrAddMedicineType(dto);
//    }
//
//    @GetMapping("/getMedicineTypes/{clinicId}")
//    public ResponseEntity<Response> getMedicineType(@PathVariable String clinicId) {
//        return pharmacistService.getMedicineTypes(clinicId);
//    }
//    
//    @GetMapping("/getMedicineTypesById/{Id}")
//    public ResponseEntity<Response> getMedicineTypes(@PathVariable String Id) {
//        return pharmacistService.getMedicineType(Id);
//    }
//    
//    @GetMapping("/getAllMedicineTypes")
//    public ResponseEntity<Response> getAllMedicineTypes() {
//        return pharmacistService.getAllMedicineTypes();
//    }
}