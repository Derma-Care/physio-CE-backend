package com.clinicadmin.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.BookingResponse;
import com.clinicadmin.dto.ClinicDTO;
import com.clinicadmin.dto.PatientConsentFormDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.Doctors;
import com.clinicadmin.entity.PatientConsentForm;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.feignclient.BookingFeign;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.repository.PatientConsentFormRepository;
import com.clinicadmin.service.PatientConsentFormService;
import com.clinicadmin.utils.Base64CompressionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PatientConsentFormServiceImpl implements PatientConsentFormService {
	@Autowired
	PatientConsentFormRepository patientConsentFormRepository;

	@Autowired
	DoctorsRepository doctorsRepository;

	@Autowired
	BookingFeign bookingFeign;

	@Autowired
	AdminServiceClient adminServiceClient;

	@Autowired
	ObjectMapper objectMapper;

	@Override
	public Response getPatientDetailsForFormUsingBooking(String bookingId, String patientId, String mobileNumber) {
		Response response = new Response();
		ResponseEntity<Response> responseEntity = bookingFeign.getPatientDetailsForConsentForm(bookingId, patientId,
				mobileNumber);
		Response resData = responseEntity.getBody();

		if (resData == null || resData.getData() == null) {
			response.setSuccess(false);
			response.setMessage("Patient details not found");
			response.setStatus(404);

			return response;
		}
		
		BookingResponse bookingDto = objectMapper.convertValue(resData.getData(), BookingResponse.class);

		Doctors doctordata = doctorsRepository.findByDoctorId(bookingDto.getDoctorId()).orElse(null);
		if (doctordata == null) {
			response.setSuccess(false);
			response.setMessage("Doctor not found");
			response.setStatus(404);
			return response;

		}

 ResponseEntity<Response> clinicData =	adminServiceClient.getClinicById(bookingDto.getClinicId());
 ClinicDTO clinics = objectMapper.convertValue(clinicData.getBody().getData(), ClinicDTO.class);
	if (clinics == null) {
		response.setSuccess(false);
		response.setMessage("Clinic not found with this hospitalId"+bookingDto.getClinicId());
		response.setStatus(404);
		return response;

	}
		

		String decompressedSignature = Base64CompressionUtil.decompressBase64(doctordata.getDoctorSignature());

		ResponseEntity<Response> adminRes = adminServiceClient.getClinicById(bookingDto.getClinicId());
		Response clinicRes = adminRes.getBody();
        ClinicDTO clincDTO = objectMapper.convertValue(clinicRes.getData(), ClinicDTO.class);
        String decompressedLogo =  Base64CompressionUtil.decompressBase64(clinics.getHospitalLogo());
        
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String currentDate = LocalDate.now().format(formatter);

		// Create entity object
		PatientConsentForm formdata = new PatientConsentForm();
		formdata.setFullName(bookingDto.getName());
		formdata.setDateOfBirth(bookingDto.getAge());
		formdata.setContactNumber(bookingDto.getMobileNumber());
		formdata.setAddress(bookingDto.getPatientAddress());
		formdata.setProcedureName(null);
		formdata.setProcedureDate(bookingDto.getServiceDate());
		formdata.setPhysicianName(bookingDto.getDoctorName());
		formdata.setInformedAboutProcedure(true);
		formdata.setUnderstandsRisks(true);
		formdata.setInformedOfAlternatives(true);
		formdata.setHadQuestionsAnswered(true);
		formdata.setNoGuaranteesGiven(true);
		formdata.setAnesthesiaConsent(true);
		formdata.setUnderstandsAnesthesiaRisks(true);
		formdata.setConsentForRecording(true);
		formdata.setNoConsentForRecording(true);
		formdata.setUnderstandsWithdrawalRight(true);
		formdata.setConsentGiven(true);
		formdata.setPhysicianSignature(Base64CompressionUtil.compressBase64(decompressedSignature));
		formdata.setPhysicianSignedDate(currentDate);
		formdata.setHospitalId(bookingDto.getClinicId());
		formdata.setHospitalName(bookingDto.getClinicName());
		formdata.setHospitalLogo(decompressedLogo);

		// Save entity
		PatientConsentForm savedForm = patientConsentFormRepository.save(formdata);

		// Convert saved entity to DTO
		PatientConsentFormDTO formDTO = objectMapper.convertValue(savedForm, PatientConsentFormDTO.class);
		response.setSuccess(false);
		response.setData(formDTO);
		response.setMessage("Consent form saved and returned successfully");
		response.setStatus(200);
		return response;
	}

	@Override
	public Response updatePatientConsentForm(String id, PatientConsentFormDTO dto) {
		Response response = new Response();

		if (id == null || id.isEmpty()) {
			response.setSuccess(false);
			response.setMessage("Consent form ID is required for update.");
			response.setStatus(400);
			return response;
		}

		// Fetch existing form
		PatientConsentForm existingForm = patientConsentFormRepository.findById(id).get();
		if (existingForm == null) {
			response.setSuccess(false);
			response.setMessage("Patient consent form not found for ID: " + id);
			response.setStatus(404);
			return response;
		}

		// Update only non-null fields from DTO
		if (dto.getFullName() != null)
			existingForm.setFullName(dto.getFullName());
		if (dto.getDateOfBirth() != null)
			existingForm.setDateOfBirth(dto.getDateOfBirth());
		if (dto.getContactNumber() != null)
			existingForm.setContactNumber(dto.getContactNumber());
		if (dto.getAddress() != null)
			existingForm.setAddress(dto.getAddress());
		if (dto.getMedicalRecordNumber() != null)
			existingForm.setMedicalRecordNumber(dto.getMedicalRecordNumber());

		if (dto.getProcedureName() != null)
			existingForm.setProcedureName(dto.getProcedureName());
		if (dto.getProcedureDate() != null)
			existingForm.setProcedureDate(dto.getProcedureDate());
		if (dto.getPhysicianName() != null)
			existingForm.setPhysicianName(dto.getPhysicianName());

		existingForm.setInformedAboutProcedure(dto.isInformedAboutProcedure());
		existingForm.setUnderstandsRisks(dto.isUnderstandsRisks());
		existingForm.setInformedOfAlternatives(dto.isInformedOfAlternatives());
		existingForm.setHadQuestionsAnswered(dto.isHadQuestionsAnswered());
		existingForm.setNoGuaranteesGiven(dto.isNoGuaranteesGiven());

		existingForm.setAnesthesiaConsent(dto.isAnesthesiaConsent());
		existingForm.setUnderstandsAnesthesiaRisks(dto.isUnderstandsAnesthesiaRisks());

		existingForm.setConsentForRecording(dto.isConsentForRecording());
		existingForm.setNoConsentForRecording(dto.isNoConsentForRecording());

		existingForm.setUnderstandsWithdrawalRight(dto.isUnderstandsWithdrawalRight());
		existingForm.setConsentGiven(dto.isConsentGiven());

		if (dto.getPatientSignature() != null)
			existingForm.setPatientSignature(dto.getPatientSignature());
		if (dto.getPatientSignedDate() != null)
			existingForm.setPatientSignedDate(dto.getPatientSignedDate());
		if (dto.getWitnessSignature() != null)
			existingForm.setWitnessSignature(dto.getWitnessSignature());
		if (dto.getWitnessSignedDate() != null)
			existingForm.setWitnessSignedDate(dto.getWitnessSignedDate());
		if (dto.getPhysicianSignature() != null)
			existingForm.setPhysicianSignature(dto.getPhysicianSignature());
		if (dto.getPhysicianSignedDate() != null)
			existingForm.setPhysicianSignedDate(dto.getPhysicianSignedDate());
		if (dto.getHospitalId() != null)
			existingForm.setHospitalId(dto.getHospitalId());
		if (dto.getHospitalName() != null)
			existingForm.setHospitalName(dto.getHospitalName());
		if (dto.getHospitalLogo() != null)
			existingForm.setHospitalLogo(Base64CompressionUtil.compressBase64(dto.getHospitalLogo()));
			

		// Save the updated form
		PatientConsentForm updatedForm = patientConsentFormRepository.save(existingForm);

		// Convert entity to DTO
		PatientConsentFormDTO updatedDTO = objectMapper.convertValue(updatedForm, PatientConsentFormDTO.class);
		response.setSuccess(true);
		response.setMessage("Consent form updated successfully.");
		response.setStatus(200);
		response.setData(updatedDTO);

		return response;
	}

}