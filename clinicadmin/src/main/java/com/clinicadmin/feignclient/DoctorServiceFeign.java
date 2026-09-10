//package com.clinicadmin.feignclient;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import com.clinicadmin.dto.DoctorNotesDTO;
//import com.clinicadmin.dto.DoctorPrescriptionDTO;
//import com.clinicadmin.dto.MedicineDTO;
//import com.clinicadmin.dto.MedicineTypeDTO;
//import com.clinicadmin.dto.Response;
//
//@FeignClient(name = "doctor-service")
//public interface DoctorServiceFeign {
//	@PostMapping("/api/doctor-notes/add-doctornotes")
//	public ResponseEntity<Response> addNote(@RequestBody DoctorNotesDTO dto);
//
//	@GetMapping("/api/doctor-notes/get-all-doctor-notes")
//	public ResponseEntity<Response> getAllNotes();
//
//	@GetMapping("/api/appointments/patient/{patientId}")
//	public ResponseEntity<?> getAppointmentsByPatientId(@PathVariable String patientId);
//
//	// Prescription APIs for medicine template
//
//	@PostMapping("/api/doctors/createPrescription")
//	ResponseEntity<Response> createPrescription(@RequestBody DoctorPrescriptionDTO dto);
//
//	@GetMapping("/api/doctors/getAllPrescriptions")
//	ResponseEntity<Response> getAllPrescriptions();
//
//	@GetMapping("/api/doctors/getPrescriptionById/{id}")
//	ResponseEntity<Response> getPrescriptionById(@PathVariable String id);
//
//	@GetMapping("/api/doctors/getMedicineById/{medicineId}")
//	ResponseEntity<Response> getMedicineById(@PathVariable String medicineId);
//
//	@GetMapping("/api/doctors/searchMedicines/{keyword}")
//	ResponseEntity<Response> searchMedicines(@PathVariable String keyword);
//
//	@DeleteMapping("/api/doctors/deletePrescription/{id}")
//	ResponseEntity<Response> deletePrescription(@PathVariable String id);
//
//	@DeleteMapping("/api/doctors/deleteMedicine/{medicineId}")
//	ResponseEntity<Response> deleteMedicine(@PathVariable String medicineId);
//
//	@GetMapping("/api/doctors/getPrescriptionsByClinicId/{clinicId}")
//	ResponseEntity<Response> getPrescriptionsByClinicId(@PathVariable String clinicId);
//
//	@PutMapping("/api/doctors/updateMedicine/{medicineId}")
//	public ResponseEntity<Response> updateMedicine(@PathVariable String medicineId, @RequestBody MedicineDTO dto);
//
////    -------------------------------------MedicineType ----------------------------------
//	@PostMapping("/api/doctors/search-or-add")
//	public ResponseEntity<Response> searchOrAddMedicineType(@RequestBody MedicineTypeDTO dto);
//
//	@GetMapping("/api/doctors/getMedicineTypes/{clinicId}")
//	public ResponseEntity<Response> getMedicineTypes(@PathVariable String clinicId);
//	
//	 @GetMapping("/api/doctors/getMedicineTypes/{id}")
//	 public ResponseEntity<Response> getMedicineType(@PathVariable String id);
//	 
//	 @GetMapping("/api/doctors/getAllMedicineTypes")
//	    public ResponseEntity<Response> getAllMedicineTypes();
//
//// ------------- ------ getVisitHistoryByPatient ---------------------------
//	@GetMapping("/api/doctors/visitHistory/{patientId}")
//	public ResponseEntity<Response> getVisitHistoryByPatientId(@PathVariable String patientId);
//
//}
