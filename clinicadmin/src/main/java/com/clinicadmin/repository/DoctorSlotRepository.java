package com.clinicadmin.repository;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.clinicadmin.entity.DoctorSlot;

public interface DoctorSlotRepository extends MongoRepository<DoctorSlot, String> {
//	DoctorSlot findByDoctorId(String doctorId);
	 
	DoctorSlot findByDoctorIdAndDate(String doctorId, String date);  // keep old one for existing code

	List<DoctorSlot> findAllByDoctorIdAndDate(String doctorId, String date);  // new one for conflict checking

	List<DoctorSlot> findByHospitalIdAndDoctorId(String hospitalId, String doctorId);

	DoctorSlot findByDoctorIdAndDateAndBranchId(String doctorId, String date, String branchId);

	List<DoctorSlot> findByHospitalIdAndDoctorIdAndBranchId(String hospitalId, String doctorId, String branchId);

	DoctorSlot findByDoctorIdAndBranchIdAndDate(String doctorId, String branchId, String date);

	List<DoctorSlot> findByHospitalIdAndBranchIdAndDoctorId(String hospitalId, String branchId, String doctorId);

	List<DoctorSlot> findByBranchId(String existingBranchId);
	
}
