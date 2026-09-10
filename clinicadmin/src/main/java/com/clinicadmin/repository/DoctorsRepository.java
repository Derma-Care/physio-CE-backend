package com.clinicadmin.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.clinicadmin.entity.Doctors;

public interface DoctorsRepository extends MongoRepository<Doctors, ObjectId> {

	boolean existsByDoctorMobileNumber(String mobileNumber);

	Optional<Doctors> findByDoctorId(String doctorId);

	List<Doctors> findByHospitalId(String hospitalId);

	Optional<Doctors> findByHospitalIdAndDoctorId(String clinicId, String doctorId);

	// -------------------- STRICT BRANCH METHOD --------------------
	// Fetch only doctors whose branchId exactly matches
	List<Doctors> findByHospitalIdAndBranchId(String hospitalId, String branchId);

	@Query("{ 'hospitalId': ?0, 'branchId': ?1 }")
	List<Doctors> findByHospitalIdAndBranchIdStrict(String hospitalId, String branchId);

	// -------------------- CROSS-BRANCH METHOD --------------------
	// Includes doctors of the branch and doctors in its sub-branches
	@Query("{ 'hospitalId': ?0, $or: [ { 'branchId': ?1 }, { 'branches.branchId': ?1 } ] }")
	List<Doctors> findByHospitalIdAndBranchIdIncludingBranches(String hospitalId, String branchId);

	// -------------------- DUPLICATE CHECKS (CREATE) --------------------
	boolean existsByDoctorEmail(String doctorEmail);

	boolean existsByDoctorLicence(String doctorLicence);

	boolean existsByAadharID(String aadharID);

	// -------------------- DUPLICATE CHECKS (UPDATE - EXCLUDE SELF) --------------------
	boolean existsByDoctorMobileNumberAndDoctorIdNot(String mobileNumber, String doctorId);

	boolean existsByDoctorEmailAndDoctorIdNot(String doctorEmail, String doctorId);

	boolean existsByDoctorLicenceAndDoctorIdNot(String doctorLicence, String doctorId);

	boolean existsByAadharIDAndDoctorIdNot(String aadharID, String doctorId);
}