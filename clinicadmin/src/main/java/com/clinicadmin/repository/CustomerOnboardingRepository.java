package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.dto.CustomerOnbordingDTO;
import com.clinicadmin.entity.CustomerOnbording;

public interface CustomerOnboardingRepository extends MongoRepository<CustomerOnbording, String> {
	Optional<CustomerOnbording> findByCustomerId(String customerId); // ✅ works because field exists
	List<CustomerOnbording> findByMobileNumber(String mbilenumber); // ✅ works because field exists

	void deleteByCustomerId(String customerId);

	List<CustomerOnbording> findByHospitalId(String hospitalId);
	

	List<CustomerOnbording> findByBranchId(String branchId);
	
	CustomerOnbording findByPatientIdAndHospitalId(String patientId,String clinicId);

	List<CustomerOnbording> findByMobileNumberAndHospitalId(String mobilenumber,String hospitalId);
	
	List<CustomerOnbordingDTO> findByFullNameIgnoreCaseAndHospitalId(String fullName,String hospitalId);
	
	List<CustomerOnbording> findByHospitalIdAndBranchId(String hospitalId, String branchId);

	CustomerOnbording findByDeviceId(String token);
	
	Optional<CustomerOnbording>	findByMobileNumberAndFullName(String mobileNumber,String name);
	List<CustomerOnbordingDTO> findByFullNameContainingIgnoreCaseAndHospitalId(String trim, String clinicId);
	
	 Optional<CustomerOnbording>
	    findByPatientIdAndHospitalIdAndBranchId(
	            String patientId,
	            String hospitalId,
	            String branchId);
	 
	CustomerOnbording findByPatientId(String patientId);

	List<CustomerOnbording> findByMobileNumberStartingWithAndHospitalId(String mobileNumber, String hospitalId);
}
