package com.AdminService.feign;

import java.util.List;

import com.AdminService.dto.DoctorsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.AdminService.dto.ReceptionistRequestDTO;
//import com.AdminService.dto.SecurityStaffDTO;
//import com.AdminService.dto.SubServicesDto;
import com.AdminService.util.Response;
import com.AdminService.util.ResponseStructure;


@FeignClient(value = "clinicadmin")
public interface ClinicAdminFeign {

//    // ---------------------- Sub-Service APIs ----------------------
//    @GetMapping("/clinic-admin/subService/getAllSubServies")
//    ResponseEntity<ResponseStructure<List<SubServicesDto>>> getAllSubServices();

    // ---------------- Doctor CRUD ---------------- //
    @PostMapping("/clinic-admin/addDoctor")
    ResponseEntity<Response> addDoctor(@RequestBody DoctorsDTO dto);

    @GetMapping("/clinic-admin/doctors")
    ResponseEntity<Response> getAllDoctors();

    @GetMapping("/clinic-admin/doctor/{id}")
    ResponseEntity<Response> getDoctorById(@PathVariable("id") String id);

    @PutMapping("/clinic-admin/updateDoctor/{doctorId}")
    ResponseEntity<Response> updateDoctorById(@PathVariable("doctorId") String doctorId,
                                              @RequestBody DoctorsDTO dto);

    @DeleteMapping("/clinic-admin/delete-doctor/{doctorId}")
    ResponseEntity<Response> deleteDoctorById(@PathVariable("doctorId") String doctorId);

    @DeleteMapping("/clinic-admin/delete-doctors-by-clinic/{clinicId}")
    ResponseEntity<Response> deleteDoctorsByClinic(@PathVariable("clinicId") String clinicId);

    // ---------------- Additional Filters ---------------- //
    @GetMapping("/clinic-admin/clinic/{clinicId}/doctor/{doctorId}")
    ResponseEntity<Response> getDoctorByClinicAndDoctorId(@PathVariable("clinicId") String clinicId,
                                                          @PathVariable("doctorId") String doctorId);

    @GetMapping("/clinic-admin/doctors/hospitalById/{hospitalId}")
    ResponseEntity<Response> getDoctorsByHospitalId(@PathVariable("hospitalId") String hospitalId);

    @GetMapping("/clinic-admin/getDoctorsByHospitalIdAndBranchId/{hospitalId}/{branchId}")
    ResponseEntity<Response> getDoctorsByHospitalIdAndBranchId(@PathVariable("hospitalId") String hospitalId,
                                                               @PathVariable("branchId") String branchId);


    // ---------------------- Doctor Slot APIs (Added Last) ----------------------

    @GetMapping("/clinic-admin/getDoctorSlots/{hospitalId}/{branchId}/{doctorId}")
    ResponseEntity<Response> getDoctorSlots(@PathVariable("hospitalId") String hospitalId,
                                            @PathVariable("branchId") String branchId,
                                            @PathVariable("doctorId") String doctorId);


    @GetMapping("/clinic-admin/getDoctorslots/{hospitalId}/{doctorId}")
    public ResponseEntity<Response> getDoctorSlot(@PathVariable String hospitalId, @PathVariable String doctorId);

    @DeleteMapping("/clinic-admin/doctorId/{doctorId}/branchId/{branchId}/date/{date}/slot/{slot}")
    public ResponseEntity<Response> deleteDoctorSlot(
            @PathVariable String doctorId,
            @PathVariable String branchId,
            @PathVariable String date,
            @PathVariable String slot);


    @DeleteMapping("/clinic-admin/doctorId/{doctorId}/{date}/{slot}/slots")
    public Response deleteDoctorSlot(@PathVariable String doctorId, @PathVariable String date,
                                     @PathVariable String slot);

    @DeleteMapping("/clinic-admin/delete-by-date/{doctorId}/{date}")
    public ResponseEntity<Response> deleteDoctorSlotsByDate(@PathVariable String doctorId, @PathVariable String date);


    @DeleteMapping("/clinic-admin/delete-by-date/{doctorId}/{branchId}/{date}")
    public ResponseEntity<Response> deleteDoctorSlotsByDate(
            @PathVariable String doctorId,
            @PathVariable String branchId,
            @PathVariable String date);


    @PutMapping("/clinic-admin/updateDoctorSlotWhileBooking/{doctorId}/{branchId}/{date}/{time}")
    public boolean updateDoctorSlotWhileBooking(@PathVariable String doctorId, @PathVariable String branchId, @PathVariable String date,
                                                @PathVariable String time);


    @PutMapping("/clinic-admin/makingFalseDoctorSlot/{doctorId}/{branchId}/{date}/{time}")
    public boolean makingFalseDoctorSlot(@PathVariable String doctorId, @PathVariable String branchId, @PathVariable String date,
                                         @PathVariable String time);


    @GetMapping("/clinic-admin/generateDoctorSlots/{doctorId}/{branchId}/{date}/{intervalMinutes}/{openingTime}/{closingTime}")
    public Response generateSlots(
            @PathVariable String doctorId,
            @PathVariable String branchId,
            @PathVariable String date,
            @PathVariable int intervalMinutes,
            @PathVariable String openingTime,
            @PathVariable String closingTime
    );


    @GetMapping("/clinic-admin/getDoctorSlots/{hospitalId}/{branchId}/{doctorId}")
    public ResponseEntity<Response> getDoctorSlot(
            @PathVariable String hospitalId,
            @PathVariable String branchId,
            @PathVariable String doctorId);



    // ✅ Create Receptionist
    @PostMapping("/clinic-admin/createReceptionist")
    ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> createReceptionist(
            @RequestBody ReceptionistRequestDTO dto);

    // ✅ Get Receptionist by ID
    @GetMapping("/clinic-admin/getReceptionistById/{id}")
    ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> getReceptionistById(
            @PathVariable String id);

    // ✅ Get All Receptionists
    @GetMapping("/clinic-admin/getAllReceptionists")
    ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getAllReceptionists();

    // ✅ Update Receptionist by ID
    @PutMapping("/clinic-admin/updateReceptionist/{id}")
    ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> updateReceptionist(
            @PathVariable String id,
            @RequestBody ReceptionistRequestDTO dto);

    // ✅ Delete Receptionist by ID
    @DeleteMapping("/clinic-admin/deleteReceptionist/{id}")
    ResponseEntity<ResponseStructure<String>> deleteReceptionist(@PathVariable String id);

    // ✅ Get Receptionists by Clinic ID
    @GetMapping("/clinic-admin/receptionists/{clinicId}")
    ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinic(
            @PathVariable String clinicId);

    // ✅ Get Receptionist by Clinic ID and Receptionist ID
    @GetMapping("/clinic-admin/{clinicId}/{receptionistId}")
    ResponseEntity<ResponseStructure<ReceptionistRequestDTO>> getReceptionistByClinicAndId(
            @PathVariable String clinicId,
            @PathVariable String receptionistId);

    // ✅ Get Receptionists by Clinic ID and Branch ID
    @GetMapping("/clinic-admin/getReceptionistsByClinicIdAndBranchId/{clinicId}/{branchId}")
    ResponseEntity<ResponseStructure<List<ReceptionistRequestDTO>>> getReceptionistsByClinicAndBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId);



}
    
        
    

