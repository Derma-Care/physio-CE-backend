package com.clinicadmin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.Branch;
import com.clinicadmin.dto.NurseDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.DoctorAndStaffLoginCredentials;
import com.clinicadmin.entity.Nurse;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.DoctorLoginCredentialsRepository;
import com.clinicadmin.repository.NurseRepository;
import com.clinicadmin.service.NurseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class NurseServiceImpl implements NurseService {
	
	private static final Logger log = LoggerFactory.getLogger(NurseServiceImpl.class);

    @Autowired
    NurseRepository nurseRepository;

    @Autowired
    DoctorLoginCredentialsRepository credentialsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AdminServiceClient adminServiceClient;

    @Autowired
    ObjectMapper objectMapper;

    // ------------------- Onboarding ----------------------

    @Override
    public Response nureseOnboarding(NurseDTO dto) {
    	 log.info("Nurse onboarding started for contact number: {}", dto.getNurseContactNumber());
        Response response = new Response();
        dto.trimNurseFields();

        if (nurseRepository.existsByNurseContactNumber(dto.getNurseContactNumber())) {
        	 log.warn("Nurse already exists with contact number: {}", dto.getNurseContactNumber());
            response.setSuccess(false);
            response.setMessage("Nurse with this mobile number already exists");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }

        if (credentialsRepository.existsByUsername(dto.getNurseContactNumber())) {
        	log.warn("Credentials already exist for username: {}", dto.getNurseContactNumber());
            response.setSuccess(false);
            response.setMessage("Login credentials already exist for this mobile number");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
        log.debug("Fetching branch details for branchId: {}", dto.getBranchId());
        ResponseEntity<Response> res = adminServiceClient.getBranchById(dto.getBranchId());
        Branch br = objectMapper.convertValue(res.getBody().getData(), Branch.class);

        log.debug("Mapping NurseDTO to Nurse entity");
        Nurse nurse = mapNurseDtoTONurseEntity(dto);
        nurse.setBranchName(br.getBranchName());

        String nurseId = generateNurseId();
        log.info("Generated Nurse ID: {}", nurseId);
        nurse.setNurseId(nurseId);

        String username = dto.getNurseContactNumber();
        String rawPassword = generateStructuredPassword();
        log.info("Generated password for nurseId: {}", nurseId);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Nurse savedNurse = nurseRepository.save(nurse);
        log.info("Nurse saved successfully with nurseId: {}", savedNurse.getNurseId());
        DoctorAndStaffLoginCredentials credentials = DoctorAndStaffLoginCredentials.builder()
                .staffId(savedNurse.getNurseId())
                .staffName(savedNurse.getFullName())
                .hospitalId(savedNurse.getHospitalId())
                .hospitalName(savedNurse.getHospitalName())
                .branchId(savedNurse.getBranchId())
                .branchName(savedNurse.getBranchName())
                .username(username)
                .password(encodedPassword)
                .emailId(savedNurse.getEmailId())
                .mobilenumber(savedNurse.getNurseContactNumber())
                .role(dto.getRole())
                .permissions(savedNurse.getPermissions())
                .build();
        credentialsRepository.save(credentials);
        log.info("Login credentials created for nurseId: {}", savedNurse.getNurseId());
        NurseDTO savedNurseDTO = mapNurseEntityToNurseDTO(savedNurse);
        savedNurseDTO.setUserName(username);
        savedNurseDTO.setPassword(rawPassword);

        response.setSuccess(true);
        response.setData(savedNurseDTO);
        response.setMessage("Nurse added successfully");
        response.setStatus(HttpStatus.CREATED.value());
        
        log.info("Nurse onboarding completed successfully for nurseId: {}", savedNurse.getNurseId());
        return response;
    }

    // ------------------- Fetch Nurses ----------------------

    @Override
    public Response getAllNursesByHospital(String hospitalId) {
    	log.info("Fetching nurses for hospitalId: {}", hospitalId);
        Response response = new Response();

        if (hospitalId == null || hospitalId.isBlank()) {
        	  log.warn("Hospital ID is empty or null");
            response.setSuccess(false);
            response.setMessage("Hospital ID must not be empty");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }

        List<Nurse> nurses = nurseRepository.findByHospitalId(hospitalId);
        log.info("Total nurses found: {}", nurses.size());
        
        if (nurses.isEmpty()) {
            response.setSuccess(true);
            response.setData(List.of());
            response.setMessage("No nurses found for this hospital");
            response.setStatus(HttpStatus.OK.value());
            return response;
        }

        List<NurseDTO> nurseDTOs = nurses.stream().map(this::mapNurseEntityToNurseDTO).toList();
        response.setSuccess(true);
        response.setData(nurseDTOs);
        response.setMessage("Nurses fetched successfully");
        response.setStatus(HttpStatus.OK.value());
        return response;
    }

    @Override
    public Response getNurseByHospitalAndNurseId(String hospitalId, String nurseId) {
    	 log.info("Fetching nurse with hospitalId: {}, nurseId: {}", hospitalId, nurseId);
        Response response = new Response();

        if (hospitalId == null || hospitalId.isBlank() || nurseId == null || nurseId.isBlank()) {
            response.setSuccess(false);
            response.setMessage("Hospital ID and Nurse ID must not be empty");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }

        return nurseRepository.findByHospitalIdAndNurseId(hospitalId, nurseId).map(nurse -> {
        	log.info("Nurse found with nurseId: {}", nurseId);
            NurseDTO dto = mapNurseEntityToNurseDTO(nurse);
            response.setSuccess(true);
            response.setData(dto);
            response.setMessage("Nurse found");
            response.setStatus(HttpStatus.OK.value());
            return response;
        }).orElseGet(() -> {
        	log.warn("Nurse not found with nurseId: {}", nurseId);
            response.setSuccess(false);
            response.setMessage("Nurse not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        });
    }

    @Override
    public Response updateNurse(String nurseId, NurseDTO dto) {

        log.info("Update nurse request received for nurseId: {}", nurseId);

        Response response = new Response();

        // ✅ Validate nurseId
        if (nurseId == null || nurseId.isBlank()) {

            log.warn("Update nurse failed: nurseId is null or blank");

            response.setSuccess(false);
            response.setMessage("Nurse ID must not be empty");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }

        log.debug("Searching nurse in DB with nurseId: {}", nurseId);

        return nurseRepository.findByNurseId(nurseId).map(existingNurse -> {

            log.info("Nurse found. Updating nurse details for nurseId: {}", nurseId);

            // ✅ Update basic fields safely using Optional
            Optional.ofNullable(dto.getHospitalId()).ifPresent(v -> {
                log.debug("Updating hospitalId");
                existingNurse.setHospitalId(v);
            });

            Optional.ofNullable(dto.getHospitalName()).ifPresent(v -> {
                log.debug("Updating hospitalName");
                existingNurse.setHospitalName(v);
            });

            Optional.ofNullable(dto.getBranchId()).ifPresent(v -> {
                log.debug("Updating branchId");
                existingNurse.setBranchId(v);
            });

            Optional.ofNullable(dto.getRole()).ifPresent(v -> {
                log.debug("Updating role");
                existingNurse.setRole(v);
            });

            Optional.ofNullable(dto.getFullName()).ifPresent(v -> {
                log.debug("Updating fullName");
                existingNurse.setFullName(v);
            });

            Optional.ofNullable(dto.getDateOfBirth()).ifPresent(v -> {
                log.debug("Updating dateOfBirth");
                existingNurse.setDateOfBirth(v);
            });

            Optional.ofNullable(dto.getDepartment()).ifPresent(v -> {
                log.debug("Updating department");
                existingNurse.setDepartment(v);
            });

            Optional.ofNullable(dto.getEmailId()).ifPresent(v -> {
                log.debug("Updating emailId");
                existingNurse.setEmailId(v);
            });

            Optional.ofNullable(dto.getNurseContactNumber()).ifPresent(v -> {
                log.debug("Updating nurseContactNumber");
                existingNurse.setNurseContactNumber(v);
            });

            Optional.ofNullable(dto.getGovernmentId()).ifPresent(v -> {
                log.debug("Updating governmentId");
                existingNurse.setGovernmentId(v);
            });

            Optional.ofNullable(dto.getBankAccountDetails()).ifPresent(v -> {
                log.debug("Updating bankAccountDetails");
                existingNurse.setBankAccountDetails(v);
            });

            Optional.ofNullable(dto.getPreviousEmploymentHistory()).ifPresent(v -> {
                log.debug("Updating previousEmploymentHistory");
                existingNurse.setPreviousEmploymentHistory(v);
            });

            Optional.ofNullable(dto.getAddress()).ifPresent(v -> {
                log.debug("Updating address");
                existingNurse.setAddress(v);
            });

            Optional.ofNullable(dto.getGender()).ifPresent(v -> {
                log.debug("Updating gender");
                existingNurse.setGender(v);
            });

            Optional.ofNullable(dto.getEmergencyContactNumber()).ifPresent(v -> {
                log.debug("Updating emergencyContactNumber");
                existingNurse.setEmergencyContactNumber(v);
            });

            Optional.ofNullable(dto.getYearsOfExperience()).ifPresent(v -> {
                log.debug("Updating yearsOfExperience");
                existingNurse.setYearsOfExperience(v);
            });

            Optional.ofNullable(dto.getQualifications()).ifPresent(v -> {
                log.debug("Updating qualifications");
                existingNurse.setQualifications(v);
            });

            Optional.ofNullable(dto.getShiftTimingOrAvailability()).ifPresent(v -> {
                log.debug("Updating shiftTimingOrAvailability");
                existingNurse.setShiftTimingOrAvailability(v);
            });

            Optional.ofNullable(dto.getPermissions()).ifPresent(v -> {
                log.debug("Updating permissions");
                existingNurse.setPermissions(v);
            });

            Optional.ofNullable(dto.getVaccinationStatus()).ifPresent(v -> {
                log.debug("Updating vaccinationStatus");
                existingNurse.setVaccinationStatus(v);
            });

            // ✅ Update Base64 documents and images
            if (dto.getNursingLicense() != null) {
                log.debug("Updating nursingLicense document");
                existingNurse.setNursingLicense(dto.getNursingLicense());
            }

            if (dto.getNursingCouncilRegistration() != null) {
                log.debug("Updating nursingCouncilRegistration document");
                existingNurse.setNursingCouncilRegistration(dto.getNursingCouncilRegistration());
            }

            if (dto.getNursingDegreeOrDiplomaCertificate() != null) {
                log.debug("Updating nursingDegreeOrDiplomaCertificate document");
                existingNurse.setNursingDegreeOrDiplomaCertificate(dto.getNursingDegreeOrDiplomaCertificate());
            }

            if (dto.getMedicalFitnessCertificate() != null) {
                log.debug("Updating medicalFitnessCertificate document");
                existingNurse.setMedicalFitnessCertificate(dto.getMedicalFitnessCertificate());
            }

            if (dto.getProfilePicture() != null) {
                log.debug("Updating profilePicture");
                existingNurse.setProfilePicture(dto.getProfilePicture());
            }

            // ✅ Save updated nurse entity
            log.info("Saving updated nurse entity for nurseId: {}", nurseId);
            Nurse updated = nurseRepository.save(existingNurse);

            // ✅ Sync Nurse details & permissions to DoctorLoginCredentials
            log.debug("Syncing login credentials for nurseId: {}", updated.getNurseId());
            Optional<DoctorAndStaffLoginCredentials> credsOpt =
                    credentialsRepository.findByStaffId(updated.getNurseId());

            if (credsOpt.isPresent()) {
                log.info("Updating login credentials for nurseId: {}", updated.getNurseId());
                DoctorAndStaffLoginCredentials creds = credsOpt.get();
                if (updated.getFullName() != null)
                    creds.setStaffName(updated.getFullName());

                if (updated.getBranchId() != null)
                    creds.setBranchId(updated.getBranchId());

                if (updated.getBranchName() != null)
                    creds.setBranchName(updated.getBranchName());

                if (updated.getHospitalId() != null)
                    creds.setHospitalId(updated.getHospitalId());

                if (updated.getHospitalName() != null)
                    creds.setHospitalName(updated.getHospitalName());

                if (updated.getRole() != null)
                    creds.setRole(updated.getRole());

                if (updated.getPermissions() != null)
                    creds.setPermissions(updated.getPermissions());

                if (updated.getNurseContactNumber() != null)
                    creds.setMobilenumber(updated.getNurseContactNumber());

                if (updated.getEmailId() != null)
                    creds.setEmailId(updated.getEmailId());
                credentialsRepository.save(creds);
                
            } else {
                log.warn("No login credentials found for nurseId: {}", updated.getNurseId());
            }

            NurseDTO updatedDTO = mapNurseEntityToNurseDTO(updated);

            log.info("Nurse updated successfully for nurseId: {}", nurseId);

            response.setSuccess(true);
            response.setData(updatedDTO);
            response.setMessage("Nurse updated successfully");
            response.setStatus(HttpStatus.OK.value());
            return response;

        }).orElseGet(() -> {

            log.warn("Update failed: Nurse not found with nurseId: {}", nurseId);

            response.setSuccess(false);
            response.setMessage("Nurse not found for update");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return response;
        });
    }

//    @Override
//    public Response updateNurse(String nurseId, NurseDTO dto) {
//        log.info("Update nurse request received for nurseId: {}", nurseId);
//        Response response = new Response();
//
//        // ✅ Validate nurseId
//        if (nurseId == null || nurseId.isBlank()) {
//        	log.warn("Update nurse failed: nurseId is null or blank");
//            response.setSuccess(false);
//            response.setMessage("Nurse ID must not be empty");
//            response.setStatus(HttpStatus.BAD_REQUEST.value());
//            return response;
//        }
//        log.debug("Searching nurse in DB with nurseId: {}", nurseId);
//        return nurseRepository.findByNurseId(nurseId).map(existingNurse -> {
//        	log.info("Nurse found. Updating nurse details for nurseId: {}", nurseId);
//            // ✅ Update basic fields safely using Optional
//            Optional.ofNullable(dto.getHospitalId()).ifPresent(existingNurse::setHospitalId);
//            Optional.ofNullable(dto.getHospitalName()).ifPresent(existingNurse::setHospitalName);
//            Optional.ofNullable(dto.getBranchId()).ifPresent(existingNurse::setBranchId);
//            Optional.ofNullable(dto.getRole()).ifPresent(existingNurse::setRole);
//            Optional.ofNullable(dto.getFullName()).ifPresent(existingNurse::setFullName);
//            Optional.ofNullable(dto.getDateOfBirth()).ifPresent(existingNurse::setDateOfBirth);
//            Optional.ofNullable(dto.getDepartment()).ifPresent(existingNurse::setDepartment);
//            Optional.ofNullable(dto.getEmailId()).ifPresent(existingNurse::setEmailId);
//            Optional.ofNullable(dto.getNurseContactNumber()).ifPresent(existingNurse::setNurseContactNumber);
//            Optional.ofNullable(dto.getGovernmentId()).ifPresent(existingNurse::setGovernmentId);
//            Optional.ofNullable(dto.getBankAccountDetails()).ifPresent(existingNurse::setBankAccountDetails);
//            Optional.ofNullable(dto.getPreviousEmploymentHistory()).ifPresent(existingNurse::setPreviousEmploymentHistory);
//            Optional.ofNullable(dto.getAddress()).ifPresent(existingNurse::setAddress);
//            Optional.ofNullable(dto.getGender()).ifPresent(existingNurse::setGender);
//            Optional.ofNullable(dto.getEmergencyContactNumber()).ifPresent(existingNurse::setEmergencyContactNumber);
//            Optional.ofNullable(dto.getYearsOfExperience()).ifPresent(existingNurse::setYearsOfExperience);
//            Optional.ofNullable(dto.getQualifications()).ifPresent(existingNurse::setQualifications);
//            Optional.ofNullable(dto.getShiftTimingOrAvailability()).ifPresent(existingNurse::setShiftTimingOrAvailability);
//            Optional.ofNullable(dto.getPermissions()).ifPresent(existingNurse::setPermissions);
//            Optional.ofNullable(dto.getVaccinationStatus()).ifPresent(existingNurse::setVaccinationStatus);
//
//            // ✅ Update Base64 documents and images
//            if (dto.getNursingLicense() != null)
//                existingNurse.setNursingLicense(dto.getNursingLicense());
//
//            if (dto.getNursingCouncilRegistration() != null)
//                existingNurse.setNursingCouncilRegistration(dto.getNursingCouncilRegistration());
//
//            if (dto.getNursingDegreeOrDiplomaCertificate() != null)
//                existingNurse.setNursingDegreeOrDiplomaCertificate(dto.getNursingDegreeOrDiplomaCertificate());
//
//            if (dto.getMedicalFitnessCertificate() != null)
//                existingNurse.setMedicalFitnessCertificate(dto.getMedicalFitnessCertificate());
//
//            if (dto.getProfilePicture() != null)
//                existingNurse.setProfilePicture(dto.getProfilePicture());
//
//            // ✅ Save updated nurse entity
//            Nurse updated = nurseRepository.save(existingNurse);
//
//            // ✅ Sync Nurse details & permissions to DoctorLoginCredentials
//            Optional<DoctorLoginCredentials> credsOpt = credentialsRepository.findByStaffId(updated.getNurseId());
//            if (credsOpt.isPresent()) {
//                DoctorLoginCredentials creds = credsOpt.get();
//
//                creds.setStaffName(updated.getFullName());
//                creds.setBranchId(updated.getBranchId());
//                creds.setBranchName(updated.getBranchName());
//                creds.setHospitalId(updated.getHospitalId());
//                creds.setHospitalName(updated.getHospitalName());
//                creds.setRole(updated.getRole());
//                creds.setPermissions(updated.getPermissions()); // ✅ key sync
//                creds.setUsername(updated.getNurseContactNumber()); // optional if username = phone
//                credentialsRepository.save(creds);
//            }
//
//            // ✅ Map to DTO for response
//            NurseDTO updatedDTO = mapNurseEntityToNurseDTO(updated);
//
//            response.setSuccess(true);
//            response.setData(updatedDTO);
//            response.setMessage("Nurse updated successfully");
//            response.setStatus(HttpStatus.OK.value());
//            return response;
//
//        }).orElseGet(() -> {
//            response.setSuccess(false);
//            response.setMessage("Nurse not found for update");
//            response.setStatus(HttpStatus.NOT_FOUND.value());
//            return response;
//        });
//    }


    // ------------------- Delete ----------------------

    @Override
    public Response deleteNurse(String hospitalId, String nurseId) {

        log.info("Delete nurse request received. hospitalId: {}, nurseId: {}", hospitalId, nurseId);

        Response response = new Response();

        try {
            // 1️⃣ Validate inputs
            if (hospitalId == null || hospitalId.isBlank() || nurseId == null || nurseId.isBlank()) {

                log.warn("Delete nurse failed: hospitalId or nurseId is null/blank");

                response.setSuccess(false);
                response.setMessage("Hospital ID and Nurse ID must not be empty");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            // 2️⃣ Normalize inputs (trim whitespace)
            String normalizedHospitalId = hospitalId.trim();
            String normalizedNurseId = nurseId.trim();

            log.debug("Normalized inputs. hospitalId: {}, nurseId: {}", normalizedHospitalId, normalizedNurseId);

            // 3️⃣ Find nurse by nurseId (and optionally hospitalId)
            log.debug("Searching nurse with nurseId: {}", normalizedNurseId);
            Optional<Nurse> existingNurse = nurseRepository.findByNurseId(normalizedNurseId);

            if (existingNurse.isEmpty()) {

                log.warn("Delete nurse failed: Nurse not found with nurseId: {}", normalizedNurseId);

                response.setSuccess(false);
                response.setMessage("Nurse not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return response;
            }

            // Optional: check hospitalId matches
            if (!existingNurse.get().getHospitalId().equalsIgnoreCase(normalizedHospitalId)) {

                log.warn(
                    "Delete nurse failed: Nurse hospital mismatch. Expected hospitalId: {}, Actual hospitalId: {}",
                    normalizedHospitalId,
                    existingNurse.get().getHospitalId()
                );

                response.setSuccess(false);
                response.setMessage("Nurse does not belong to this hospital");
                response.setStatus(HttpStatus.CONFLICT.value());
                return response;
            }

            // 4️⃣ Delete nurse record
            log.info("Deleting nurse record for nurseId: {}", normalizedNurseId);
            nurseRepository.deleteById(existingNurse.get().getId());

            // 5️⃣ Delete corresponding login credentials safely
            log.debug("Checking login credentials for nurseId: {}", normalizedNurseId);
            credentialsRepository.findByStaffId(normalizedNurseId)
                .filter(cred -> "Nurse".equalsIgnoreCase(cred.getRole()))
                .ifPresent(cred -> {
                    log.info("Deleting login credentials for nurseId: {}", normalizedNurseId);
                    credentialsRepository.deleteById(cred.getId());
                });

            // 6️⃣ Return success response
            log.info("Nurse and credentials deleted successfully for nurseId: {}", normalizedNurseId);

            response.setSuccess(true);
            response.setMessage("Nurse and credentials deleted successfully");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            log.error("Exception occurred while deleting nurse. hospitalId: {}, nurseId: {}",
                      hospitalId, nurseId, e);

            response.setSuccess(false);
            response.setMessage("Error deleting nurse: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }



    // ------------------- Mapper Methods ----------------------

    private Nurse mapNurseDtoTONurseEntity(NurseDTO dto) {
        Nurse nurse = new Nurse();
        nurse.setHospitalId(dto.getHospitalId());
        nurse.setHospitalName(dto.getHospitalName());
        nurse.setBranchId(dto.getBranchId());
        nurse.setBranchName(dto.getBranchName());
        nurse.setRole(dto.getRole());
        nurse.setFullName(dto.getFullName());
        nurse.setDateOfBirth(dto.getDateOfBirth());
        nurse.setNurseContactNumber(dto.getNurseContactNumber());
        nurse.setGovernmentId(dto.getGovernmentId());

        nurse.setNursingLicense(dto.getNursingLicense());
        nurse.setNursingCouncilRegistration(dto.getNursingCouncilRegistration());
        nurse.setNursingDegreeOrDiplomaCertificate(dto.getNursingDegreeOrDiplomaCertificate());
        nurse.setDateOfJoining(dto.getDateOfJoining());
        nurse.setDepartment(dto.getDepartment());
        nurse.setBankAccountDetails(dto.getBankAccountDetails());
        nurse.setMedicalFitnessCertificate(dto.getMedicalFitnessCertificate());
        nurse.setEmailId(dto.getEmailId());
        nurse.setPreviousEmploymentHistory(dto.getPreviousEmploymentHistory());
//        nurse.setExperienceCertificates(dto.getExperienceCertificates());
        nurse.setProfilePicture(dto.getProfilePicture());
        nurse.setVaccinationStatus(dto.getVaccinationStatus());
//        nurse.setInsuranceOrESIdetails(dto.getInsuranceOrESIdetails());

        nurse.setAddress(dto.getAddress());
        nurse.setGender(dto.getGender());
        nurse.setQualifications(dto.getQualifications());
        nurse.setShiftTimingOrAvailability(dto.getShiftTimingOrAvailability());
        nurse.setYearsOfExperience(dto.getYearsOfExperience());
        nurse.setEmergencyContactNumber(dto.getEmergencyContactNumber());
        nurse.setPermissions(dto.getPermissions());
        nurse.setCreatedBy(dto.getCreatedBy());
        nurse.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());

        return nurse;
    }

    private NurseDTO mapNurseEntityToNurseDTO(Nurse nurse) {
        NurseDTO dto = new NurseDTO();
        dto.setId(nurse.getId().toString());
        dto.setNurseId(nurse.getNurseId());
        dto.setHospitalId(nurse.getHospitalId());
        dto.setHospitalName(nurse.getHospitalName());
        dto.setBranchId(nurse.getBranchId());
        dto.setBranchName(nurse.getBranchName());
        dto.setRole(nurse.getRole());
        dto.setFullName(nurse.getFullName());
        dto.setDateOfBirth(nurse.getDateOfBirth());
        dto.setNurseContactNumber(nurse.getNurseContactNumber());
        dto.setGovernmentId(nurse.getGovernmentId());

        dto.setNursingLicense(nurse.getNursingLicense());
        dto.setNursingDegreeOrDiplomaCertificate(nurse.getNursingDegreeOrDiplomaCertificate());
        dto.setNursingCouncilRegistration(nurse.getNursingCouncilRegistration());
        dto.setDateOfJoining(nurse.getDateOfJoining());
        dto.setDepartment(nurse.getDepartment());
        dto.setBankAccountDetails(nurse.getBankAccountDetails());
        dto.setMedicalFitnessCertificate(nurse.getMedicalFitnessCertificate());
        dto.setEmailId(nurse.getEmailId());
        dto.setPreviousEmploymentHistory(nurse.getPreviousEmploymentHistory());
        dto.setVaccinationStatus(nurse.getVaccinationStatus());
//        dto.setInsuranceOrESIdetails(nurse.getInsuranceOrESIdetails());
//        dto.setExperienceCertificates(nurse.getExperienceCertificates());
        dto.setProfilePicture(nurse.getProfilePicture());

        dto.setAddress(nurse.getAddress());
        dto.setGender(nurse.getGender());
        dto.setQualifications(nurse.getQualifications());
        dto.setShiftTimingOrAvailability(nurse.getShiftTimingOrAvailability());
        dto.setEmergencyContactNumber(nurse.getEmergencyContactNumber());
        dto.setYearsOfExperience(nurse.getYearsOfExperience());
        dto.setPermissions(nurse.getPermissions());
        dto.setCreatedAt(nurse.getCreatedAt());
        dto.setCreatedBy(nurse.getCreatedBy());
        dto.setUpdatedDate(nurse.getUpdatedDate());

        return dto;
    }
    
    @Override
    public Response getAllNursesByBranchIdAndHospitalId(String hospitalId, String branchId) {

        log.info("Fetch nurses request received. hospitalId: {}, branchId: {}", hospitalId, branchId);

        Response response = new Response();
        try {
            // 🔍 Validate input
            if (hospitalId == null || hospitalId.isBlank() || branchId == null || branchId.isBlank()) {

                log.warn("Fetch nurses failed: hospitalId or branchId is null/blank");

                response.setSuccess(false);
                response.setMessage("Hospital ID and Branch ID must not be empty");
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            log.debug("Fetching nurses from DB for hospitalId: {}, branchId: {}", hospitalId, branchId);

            // 🔍 Fetch nurses
            List<Nurse> nurses = nurseRepository.findByHospitalIdAndBranchId(hospitalId, branchId);

            if (nurses == null || nurses.isEmpty()) {

                log.info("No nurses found for hospitalId: {}, branchId: {}", hospitalId, branchId);

                response.setSuccess(true);
                response.setData(List.of());
                response.setMessage("No nurses found for hospitalId: " + hospitalId + " and branchId: " + branchId);
                response.setStatus(HttpStatus.OK.value());
                return response;
            }

            log.info("Total nurses found: {}", nurses.size());

            // 🔄 Convert to DTO
            log.debug("Mapping Nurse entities to NurseDTOs");
            List<NurseDTO> nurseDTOs = nurses.stream()
                    .map(this::mapNurseEntityToNurseDTO)
                    .toList();

            // ✅ Prepare success response
            log.info("Nurses fetched successfully for hospitalId: {}, branchId: {}", hospitalId, branchId);

            response.setSuccess(true);
            response.setData(nurseDTOs);
            response.setMessage("Nurses fetched successfully");
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {

            log.error("Exception occurred while fetching nurses. hospitalId: {}, branchId: {}",
                      hospitalId, branchId, e);

            response.setSuccess(false);
            response.setMessage("Error fetching nurses: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    // ------------------- Helper Methods ----------------------

    private String generateNurseId() {
        return "NR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateStructuredPassword() {
        String[] words = {"Nurse"};
        String specialChars = "@#$%&*!?";
        String digits = "0123456789";
        SecureRandom random = new SecureRandom();

        String word = words[random.nextInt(words.length)];
        String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);

        char specialChar = specialChars.charAt(random.nextInt(specialChars.length()));

        StringBuilder numberPart = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            numberPart.append(digits.charAt(random.nextInt(digits.length())));
        }

        return capitalizedWord + specialChar + numberPart;
    }

    // Encode/Decode helpers if you ever need raw bytes
    public static String encodeBytesToBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64ToBytes(String base64) {
        if (base64 == null || base64.isBlank()) return null;
        return Base64.getDecoder().decode(base64);
    }




	
//---------------------------------------------------------------------------------------------------
//--------------------------------- NurseLogin-------------------------------------------------------
//---------------------------------------------------------------------------------------------------

//	------------------Nurse login---------------------------------------------------------
//	@Override
//	public Response nurseLogin(NurseLoginDTO loginDTO) {
//		Response response = new Response();
//		Optional<Nurse> nurseFromDB = nurseRepository.findByUserName(loginDTO.getUserName());
//
//		if (nurseFromDB.isPresent()) {
//			Nurse nurse = nurseFromDB.get();
//
//			// Compare passwords
//			if (nurse.getPassword().equals(loginDTO.getPassword())) {
//				response.setSuccess(true);
//				response.setMessage("Login Successful");
//				response.setStatus(HttpStatus.OK.value());
//			} else {
//				response.setSuccess(false);
//				response.setMessage("Invalid password");
//				response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 is better than 409
//			}
//		} else {
//			response.setSuccess(false);
//			response.setMessage("Invalid Username");
//			response.setStatus(HttpStatus.NOT_FOUND.value()); // 404 is better than 409
//		}
//
//		return response;
//	}

//-----------------------reset nurse login password-------------------------------------------
//	@Override
//	public Response resetLoginPassword(ResetNurseLoginPasswordDTO dto) {
//		Response response = new Response();
//		Optional<Nurse> nurseFromDB = nurseRepository.findByUserName(dto.getUserName());
//		if(nurseFromDB.isPresent()) {
//		Nurse nurse = nurseFromDB.get();
//			if(nurse.getPassword().equals(dto.getCurrentPassword())) {
//				if(dto.getNewPassword().equals(dto.getConfirmPassword())) {
//					nurse.setPassword(dto.getNewPassword());
//					nurseRepository.save(nurse);
//					response.setSuccess(true);
//					response.setMessage("Password updated Successfully");
//					response.setStatus(HttpStatus.OK.value());
//
//					
//				}
//				else {
//					response.setSuccess(false);
//					response.setMessage("New password and confirm password are not matched");
//					response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 is better
//				}
//				
//			}
//			else {
//				response.setSuccess(false);
//				response.setMessage("Invalid password");
//				response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 is better than 409
//			}
//		}
//		else {
//			response.setSuccess(false);
//			response.setMessage("Invalid Username");
//			response.setStatus(HttpStatus.NOT_FOUND.value()); // 404 is better than 409
//		}
//		return response;
//	}

}
