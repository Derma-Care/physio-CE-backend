package com.dermacare.bookingService.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.dermacare.bookingService.entity.Booking;

@Repository
public interface BookingServiceRepository extends MongoRepository<Booking, String> {

    // ✅ Uses mobileNumber index
    List<Booking> findByMobileNumber(String mobileNumber);

    // ✅ Uses doctorId index
    List<Booking> findByDoctorId(String doctorId);

    // ✅ Uses clinicId_branchId_idx compound index
    List<Booking> findByBranchId(String branchId);

    // ✅ Uses clinicId index
    List<Booking> findByClinicId(String clinicId);

    // ✅ bookingId is the @Id field
    Optional<Booking> findByBookingId(String bookingId);

    // ⚠️ patientId and bookingId are indexed, but regex on name is slower
    @Query("{$or: [ { 'name': ?0 }, { 'bookingId': ?0 }, { 'patientId': ?0 } ]}")
    List<Booking> findByNameIgnoreCaseOrBookingIdOrPatientId(String input);

    // ✅ Uses clinicId_doctorId_serviceDate_idx compound index
    List<Booking> findByClinicIdAndDoctorId(String clinicId, String doctorId);

    // ✅ Uses patientId index
    List<Booking> findByPatientId(String patientId);

    // ✅ Uses customerId_status_idx compound index
    List<Booking> findByRelationIgnoreCaseAndCustomerIdAndNameIgnoreCase(String relation, String customerId, String name);

    Optional<Booking> findByBookingIdAndPatientIdAndMobileNumber(String bookingId, String patientId, String mobileNumber);

    Booking findByMobileNumberAndPatientIdAndBookingId(String mobileNumber, String patientId, String bid);

    @Query("{ 'clinicId': ?0, 'branchId': ?1 }")
    List<Booking> findByClinicIdAndBranchId(String clinicId, String branchId);
    // ✅ Uses clinicId_branchId_idx + serviceDate index
    List<Booking> findByClinicIdAndBranchIdAndServiceDateOrderByServicetimeAsc(String clinicId, String branchId, String serviceDate);

    // ✅ Uses customerId_status_idx + branchId
    List<Booking> findByCustomerIdAndBranchId(String customerId, String branchId);

    Booking findByPatientIdAndFollowupDate(String pId, String followupdate);

    // ⚠️ Regex on name → consider text index if frequent
    List<Booking> findByNameIgnoreCase(String input);

    // ✅ Uses clinicId_doctorId_serviceDate_idx compound index
    Booking findByServiceDateAndServicetimeAndDoctorId(String date, String time, String doctorId);

    // ✅ Uses customerId_status_idx + clinicId
    List<Booking> findByCustomerIdAndClinicId(String customerId, String clinicId);

    List<Booking> findByNameContainingIgnoreCaseAndClinicId(String input, String clinicId);

    List<Booking> findByPatientIdAndClinicId(String patientId, String clinicId);

    List<Booking> findByMobileNumberAndClinicId(String mobileNumber, String clinicId);

    // ✅ Uses customerId_status_idx compound index
    List<Booking> findByCustomerIdAndStatusIgnoreCase(String customerId, String string);

    // ✅ Uses serviceDate index
    List<Booking> findByServiceDate(String today);

    // ✅ Uses clinicId_branchId_idx + serviceDate index
    List<Booking> findByClinicIdAndBranchIdAndServiceDate(String cId, String bId, String today);

    // ✅ Uses clinicId_branchId_idx + serviceDate index
    List<Booking> findByClinicIdAndBranchIdAndServiceDateBetween(String clinicId, String branchId, String format, String format2);

    List<Booking> findByClinicIdAndBranchIdAndServiceDateAndFollowupStatusIn(String clinicId, String branchId, String today, List<String> validStatus);

    List<Booking> findByPatientIdAndBookingId(String patientId, String bookingId);

    List<Booking> findByBookingIdIn(List<String> bookingIds);

    // ✅ Uses clinicId_branchId_idx + doctorId + status/followupStatus
    @Query("{ 'clinicId': ?0, 'branchId': ?1, 'doctorId': ?2, "
         + "'$or': [ { 'status': ?3 }, { 'followupStatus': ?3 } ] }")
    List<Booking> findByStatusOrFollowupStatus(String clinicId, String branchId, String doctorId, String status);

    List<Booking> findByClinicIdAndBranchIdAndDoctorIdAndStatusIgnoreCase(String clinicId, String branchId, String doctorId, String requiredStatus);

    List<Booking> findByClinicIdAndBranchIdAndDoctorIdAndFollowupStatusIgnoreCase(String clinicId, String branchId, String doctorId, String status);

    List<Booking> findByClinicIdAndDoctorIdAndStatusIgnoreCase(String clinicId, String doctorId, String requiredStatus);

    List<Booking> findByClinicIdAndDoctorIdAndFollowupStatusIgnoreCase(String clinicId, String doctorId, String status);

    // ✅ Uses customerId_status_idx
    List<Booking> findByCustomerId(String customerId);

    Page<Booking> findByCustomerId(String customerId, Pageable pageable);

    Optional<Booking> findByBookingIdIgnoreCase(String bookingId);

    List<Booking> findByBookingIdInAndClinicIdAndBranchId(List<String> followupIds, String clinicId, String branchId);

    // ✅ Uses clinicId + patientId/mobileNumber/patientMobileNumber indexes
    @Query("{ 'clinicId': ?0, '$or': [ "
         + "{ 'patientId': ?1 }, "
         + "{ 'mobileNumber': ?1 }, "
         + "{ 'patientMobileNumber': ?1 }, "
         + "{ 'name': { $regex: ?2, $options: 'i' } } ] }")
    List<Booking> searchBookings(String clinicId, String exactValue, String nameValue);

    List<Booking> findByPatientMobileNumberAndClinicId(String input, String clinicId);

	List<Booking> findByPatientIdIgnoreCase(String patientId);


    List<Booking> findByClinicIdAndBranchIdAndVisitTypeAndServiceDateBetween(String clinicId, String branchId, String sitting, String string, String string1);

    List<Booking> findByServiceDateAndServicetimeAfter(String string, String currentTime);
}
