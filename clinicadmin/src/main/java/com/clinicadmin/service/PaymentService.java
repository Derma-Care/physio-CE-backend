package com.clinicadmin.service;

import java.util.Map;

import com.clinicadmin.dto.PaymentDTO;
import com.clinicadmin.dto.Response;

public interface PaymentService {

    Response createPayment(PaymentDTO dto);

    Response getAllPayments(String clinicId, String branchId);

	Response getAllPayments();

	Response getPaymentByBillingId(String clinicId, String branchId, String billingId);

	Response updatePayment(String billingId, PaymentDTO dto);

	Response deletePayment(String billingId);


   public Map<String,Double> retriveInfoBasedOnBookingId(String id);

	Response getPaymentByPatientAndBooking(String clinicId, String branchId, String patientId, String bookingId);

	Response getTreatmentScheduleWithPayment(String clinicId, String branchId, String bookingId, String patientId);







}