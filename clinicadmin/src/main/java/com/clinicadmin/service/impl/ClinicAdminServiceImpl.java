package com.clinicadmin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.ClinicDTO;
import com.clinicadmin.dto.ClinicLoginRequestDTO;
import com.clinicadmin.dto.ResetPasswordDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.StaffInfoDTO;
import com.clinicadmin.dto.UpdateClinicLoginCredentialsDTO;
import com.clinicadmin.entity.ClinicAdminDeviceTokenEntity;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.AdministratorRepository;
import com.clinicadmin.repository.ClinicAdminWebFcmTokenRepository;
import com.clinicadmin.repository.DoctorsRepository;
import com.clinicadmin.repository.ReceptionistRepository;
import com.clinicadmin.repository.SecurityStaffRepository;
import com.clinicadmin.repository.TherapistRepository;
import com.clinicadmin.repository.WardBoyRepository;
import com.clinicadmin.service.ClinicAdminService;
import com.clinicadmin.utils.ExtractFeignMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;

@Service
public class ClinicAdminServiceImpl implements ClinicAdminService {
	
    @Autowired
    private AdminServiceClient adminServiceClient;
    
    @Autowired
    private  AdministratorRepository administratorRepository;
    
    @Autowired
    private DoctorsRepository doctorsRepository;
    
    @Autowired
    private ReceptionistRepository receptionistRepository;
    
    @Autowired
    private SecurityStaffRepository securityStaffRepository;
    
    @Autowired
    private TherapistRepository therapistRepository;
    
    @Autowired
    private WardBoyRepository wardBoyRepository;
    
    @Autowired
	private ClinicAdminWebFcmTokenRepository clinicAdminWebFcmTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ClinicAdminWebFcmTokenRepository deviceIdRepo;
  
  

    @Override
    public Response login(ClinicLoginRequestDTO credentials) {
    	try {   		
    	Response response=adminServiceClient.login(credentials);
    	try{
    	ClinicAdminDeviceTokenEntity c = new ClinicAdminDeviceTokenEntity();
    	Optional<ClinicAdminDeviceTokenEntity> obj = clinicAdminWebFcmTokenRepository.findByUsername(credentials.getUserName());
    		 if(obj.isPresent()) {
			   if(credentials.getFcmToken() != null) {
				   if(!credentials.getFcmToken().equals(obj.get().getClinicAdminWebFcmToken())) {
				   obj.get().setClinicAdminWebFcmToken(credentials.getFcmToken());
				   clinicAdminWebFcmTokenRepository.save(obj.get());}}
		   }else{
		  c.setUsername(credentials.getUserName());
		   if(credentials.getFcmToken() != null) {
		   c.setClinicAdminWebFcmToken(credentials.getFcmToken());}
		   clinicAdminWebFcmTokenRepository.save(c);}
    	 }catch(Exception e) {}
    	return response;
    	}catch(FeignException e) {
    	Response res = new Response();
    	res.setStatus(e.status());
    	res.setMessage(ExtractFeignMessage.clearMessage(e));
    	res.setSuccess(false);
       return res;
       }
    }

    @Override
    public Response updateClinicCredentials(UpdateClinicLoginCredentialsDTO updatedCredentials, String userName) {
    	try {
        	Response response=adminServiceClient.updateClinicCredentials(updatedCredentials, userName);
        	return response;
        	}catch(FeignException e) {
        	Response res = new Response();
        	res.setStatus(e.status());
        	res.setMessage(ExtractFeignMessage.clearMessage(e));
        	res.setSuccess(false);
           return res;}
        }

    @Override
    public Response getClinicById(String hospitalId) {
    	try {
        	ResponseEntity<Response> response=adminServiceClient.getClinicById(hospitalId);
        	return response.getBody();
        	}catch(FeignException e) {
        	Response res = new Response();
        	res.setStatus(e.status());
        	res.setMessage(ExtractFeignMessage.clearMessage(e));
        	res.setSuccess(false);
           return res;
           }
        }

    @Override
    public Response updateClinic(String hospitalId, ClinicDTO dto) {
    	try {
        	Response response=adminServiceClient.updateClinic(hospitalId, dto);
        	return response;
        	}catch(FeignException e) {
        	Response res = new Response();
        	res.setStatus(e.status());
        	res.setMessage(ExtractFeignMessage.clearMessage(e));
        	res.setSuccess(false);
           return res;}
        }

    @Override
    public Response deleteClinic(String hospitalId) {
    	try {
        	Response response=adminServiceClient.deleteClinic(hospitalId);
        	return response;
        	}catch(FeignException e) {
        	Response res = new Response();
        	res.setStatus(e.status());
        	res.setMessage(ExtractFeignMessage.clearMessage(e));
        	res.setSuccess(false);
           return res;
           
        	
        	
        	}
        }


    @Override
    public ResponseEntity<?> getBranchesByClinicId(String clinicId) {
        try {
          
            return adminServiceClient.getBranchByClinicId(clinicId);

        } catch (FeignException e) {
            try {
                String errorJson = e.contentUTF8();
                Response response = objectMapper.readValue(errorJson, Response.class);

  
                return ResponseEntity.status(e.status()).body(response);

            } catch (Exception ex) {
                Response fallback = new Response();
                fallback.setSuccess(false);
                fallback.setMessage("Error parsing AdminService response");
                fallback.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fallback);
            }
        }
    }
    
    
    @Override
    public Response getStaffInfo(String hospitalId, String branchId) {

        Response response = new Response();

        try {

            Map<String, List<StaffInfoDTO>> staffMap = new HashMap<>();

            // Administrators
            List<StaffInfoDTO> admins = new ArrayList<>();
            administratorRepository.findByClinicIdAndBranchId(hospitalId, branchId)
                    .forEach(admin -> admins.add(
                            new StaffInfoDTO(
                                    admin.getAdminId(),
                                    admin.getFullName(),
                                    admin.getRole()
                            )));
            staffMap.put("ADMINISTRATOR", admins);

            // Doctors
            List<StaffInfoDTO> doctors = new ArrayList<>();
            doctorsRepository.findByHospitalIdAndBranchId(hospitalId, branchId)
                    .forEach(doc -> doctors.add(
                            new StaffInfoDTO(
                                    doc.getDoctorId(),
                                    doc.getDoctorName(),
                                    doc.getRole()
                            )));
            staffMap.put("DOCTOR", doctors);

            // Receptionists
            List<StaffInfoDTO> receptionists = new ArrayList<>();
            receptionistRepository.findByClinicIdAndBranchId(hospitalId, branchId)
                    .forEach(rec -> receptionists.add(
                            new StaffInfoDTO(
                                    rec.getId(),
                                    rec.getFullName(),
                                    rec.getRole()
                            )));
            staffMap.put("RECEPTIONIST", receptionists);

            // Security Staff
            List<StaffInfoDTO> securityStaffs = new ArrayList<>();
            securityStaffRepository.findByClinicIdAndBranchId(hospitalId, branchId)
                    .forEach(sec -> securityStaffs.add(
                            new StaffInfoDTO(
                                    sec.getSecurityStaffId(),
                                    sec.getFullName(),
                                    sec.getRole()
                            )));
            staffMap.put("SECURITY_STAFF", securityStaffs);

            // Therapists
            List<StaffInfoDTO> therapists = new ArrayList<>();
            therapistRepository.findByClinicIdAndBranchId(hospitalId, branchId)
                    .forEach(therapist -> therapists.add(
                            new StaffInfoDTO(
                                    therapist.getTherapistId(),
                                    therapist.getFullName(),
                                    therapist.getRole()
                            )));
            staffMap.put("THERAPIST", therapists);

            // Ward Boys
            List<StaffInfoDTO> wardBoys = new ArrayList<>();
            wardBoyRepository.findByClinicIdAndBranchId(hospitalId, branchId)
                    .forEach(wardBoy -> wardBoys.add(
                            new StaffInfoDTO(
                                    wardBoy.getWardBoyId(),
                                    wardBoy.getFullName(),
                                    wardBoy.getRole()
                            )));
            staffMap.put("WARD_BOY", wardBoys);

            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Staff information fetched successfully");
            response.setData(staffMap);

        } catch (Exception e) {

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage(e.getMessage());
        }

        return response;
    }
    
    @Override
    public String getDeviceId(String clinicId,String branchId) {
    	Optional<ClinicAdminDeviceTokenEntity> obj = null;
    	String deviceId = null;
    	try {         
        	obj =  deviceIdRepo.findByUsername(branchId);
        	if(obj.isPresent()) {
        		 deviceId = obj.get().getClinicAdminWebFcmToken();
        	}else {
        		obj =  deviceIdRepo.findByUsername(clinicId);	
        		if(obj.isPresent()) {
        			 deviceId = obj.get().getClinicAdminWebFcmToken();
            	}}} catch (Exception e) {
            		System.out.println(e.getMessage());
        		return null;
        	}
    	///System.out.println(deviceId);
    	return  deviceId;
        }
//    ----------forgot password-------------------------------
    @Override
    public Response forgotPassword(String mobileNumber, String role) {
        try {
            ResponseEntity<Response> response = adminServiceClient.forgotPassword(mobileNumber, role);
            return response.getBody();
        } catch (FeignException e) {
            Response res = new Response();
            res.setStatus(e.status());
            res.setMessage(ExtractFeignMessage.clearMessage(e));
            res.setSuccess(false);
            return res;
        }
    }
    @Override
    public Response verifyOtp(String mobileNumber, String role, String otp) {
        try {
            ResponseEntity<Response> response =
                    adminServiceClient.verifyOtp(mobileNumber, role, otp);
            return response.getBody();
        } catch (FeignException e) {
            Response res = new Response();
            res.setStatus(e.status());
            res.setMessage(ExtractFeignMessage.clearMessage(e));
            res.setSuccess(false);
            return res;
        }
    }
    @Override
    public Response resetPassword(String mobileNumber, String role, ResetPasswordDTO dto) {
        try {
            dto.setMobileNumber(mobileNumber);
            dto.setRole(role);

            ResponseEntity<Response> response =
                    adminServiceClient.resetPassword(role, mobileNumber, dto);

            return response.getBody();
        } catch (FeignException e) {
            Response res = new Response();
            res.setStatus(e.status());
            res.setMessage(ExtractFeignMessage.clearMessage(e));
            res.setSuccess(false);
            return res;
        }
    }
}
