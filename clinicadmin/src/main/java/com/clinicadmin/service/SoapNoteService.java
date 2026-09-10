package com.clinicadmin.service;

import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.SoapNoteDTO;

public interface SoapNoteService {
	Response createSoapNote(SoapNoteDTO dto);
	Response updateSoapNote(String id, SoapNoteDTO dto);
	Response getSoapNoteById(String id);
	Response getAllSoapNotes();
	Response getSoapNotesByPatientId(String patientId);
	Response getSoapNotesByDoctorId(String doctorId);
	Response getSoapNotesByBooking(String bookingId);
	Response deleteSoapNoteById(String id);

	// -------------------- New: clinic/branch combined lookups --------------------
	Response getSoapNotesByClinicId(String clinicId);
	Response getSoapNotesByClinicIdAndBranchId(String clinicId, String branchId);
	Response getSoapNotesByClinicBranchAndDoctor(String clinicId, String branchId, String doctorId);
	Response getSoapNotesByClinicBranchAndPatient(String clinicId, String branchId, String patientId);
	Response getSoapNotesByClinicBranchAndBooking(String clinicId, String branchId, String bookingId);
	Response getSoapNotesByClinicBranchAndPatient(String clinicId, String branchId, String bookingId, String patientId); // ← updated signature

}