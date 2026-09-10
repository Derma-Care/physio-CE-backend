package com.clinicadmin.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.clinicadmin.entity.*;

public interface TherapistAttendanceRepository
extends MongoRepository<TherapistAttendance, String> {

TherapistAttendance findByTherapistIdAndDate(String therapistId, String date);

List<TherapistAttendance> findByTherapistIdAndDateStartingWith(String therapistId, String month);

List<TherapistAttendance> findByTherapistId(String therapistId);

List<TherapistAttendance> findByClinicIdAndBranchIdAndTherapistId(String clinicId, String branchId, String therapistId);

void deleteByTherapistId(String therapistId);

TherapistAttendance findByClinicIdAndBranchIdAndTherapistIdAndDate(String clinicId, String branchId, String therapistId,
		String date);


}