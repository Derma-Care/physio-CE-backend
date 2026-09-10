package com.clinicadmin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.clinicadmin.entity.Attendance;

import java.util.Optional;
import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {

    // 🔹 Daily 
    Optional<Attendance> findByUserIdAndDate(String userId, String date);

    List<Attendance> findByUserId(String userId);

    // 🔹 Monthly 
    List<Attendance> findByUserIdAndDateBetween(
            String userId,
            String startDate,
            String endDate
    );

    // 🔹 Clinic + Branch + Date
    List<Attendance> findByClinicIdAndBranchIdAndDate(
            String clinicId,
            String branchId,
            String date
    );

//    // 🔹 Clinic + Branch Monthly
//    List<Attendance> findByClinicIdAndBranchIdAndDateBetween(
//            String clinicId,
//            String branchId,
//            String startDate,
//            String endDate
//    );

    // 🔹 Optional: Role-based filtering
    List<Attendance> findByRole(String role);

    // 🔹 Optional: Role + Date
    List<Attendance> findByRoleAndDate(String role, String date);

	List<Attendance> findByUserIdAndDateStartingWith(String userId, String month);

//	Optional<Attendance> findByClinicIdAndBranchIdAndUserIdAndDate(String clinicId, String branchId, String staffId,
//			String today);

//	Optional<Attendance> findByClinicIdAndBranchIdAndUserIdAndDate(String clinicId, String branchId, String staffId);

	Optional<Attendance> findByClinicIdAndBranchIdAndUserIdAndDate(String clinicId, String branchId, String staffId,
			String date);



	List<Attendance> findByClinicIdAndBranchIdAndUserIdAndDateBetween(String clinicId, String branchId, String staffId,
			String startDate, String endDate);
}