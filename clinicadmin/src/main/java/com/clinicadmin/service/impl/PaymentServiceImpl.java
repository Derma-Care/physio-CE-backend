package com.clinicadmin.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicadmin.dto.PaymentDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.Sittings;
import com.clinicadmin.entity.CustomerOnbording;
import com.clinicadmin.entity.Payment;
import com.clinicadmin.entity.Sitting;
import com.clinicadmin.entity.SoapNote;
import com.clinicadmin.entity.TransactionHistory;
import com.clinicadmin.entity.TreatmentSchedule;
import com.clinicadmin.feignclient.AdminServiceClient;
import com.clinicadmin.repository.CustomerOnboardingRepository;
import com.clinicadmin.repository.PaymentRepository;
import com.clinicadmin.repository.SoapNoteRepository;
import com.clinicadmin.repository.TreatmentScheduleRepository;
import com.clinicadmin.service.PaymentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private PaymentRepository repository;

	@Autowired
	private AdminServiceClient adminServiceClient;

	@Autowired
	private CustomerOnboardingRepository customerRepository;

	@Autowired
	private TreatmentScheduleRepository treatmentScheduleRepository;

	@Autowired
	private SoapNoteRepository soapNoteRepository;

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	@Transactional
	public Response createPayment(PaymentDTO dto) {

		Response response = new Response();

		try {

			// ==========================
			// Fetch Clinic Details
			// ==========================

			ResponseEntity<Response> clinicResponse = adminServiceClient.getClinicById(dto.getClinicId());

			if (clinicResponse == null || clinicResponse.getBody() == null) {
				response.setSuccess(false);
				response.setStatus(500);
				response.setMessage("Unable to fetch clinic details.");
				return response;
			}

			Response clinicApiResponse = clinicResponse.getBody();

			if (!clinicApiResponse.isSuccess() || clinicApiResponse.getData() == null) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage(
						clinicApiResponse.getMessage() != null ? clinicApiResponse.getMessage() : "Clinic not found");
				return response;
			}

			// ==========================
			// Get Loyalty Percentage Only
			// ==========================

			@SuppressWarnings("unchecked")
			Map<String, Object> clinicMap = (Map<String, Object>) clinicApiResponse.getData();

			double loyaltyPercentage = parseLoyaltyPercentage(clinicMap.get("loyaltyPoints"));

			// ==========================
			// Fetch Customer
			// ==========================

			CustomerOnbording customer = customerRepository.findByPatientId(dto.getPatientId());

			if (customer == null) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Customer not found");
				return response;
			}

			// ==========================
			// Available Loyalty Points
			// ==========================

			double availablePatientPoints = customer.getAvailableRoyaltyPoints() == null ? 0.0
					: customer.getAvailableRoyaltyPoints();

			// ==========================
			// Used Loyalty Points
			// ==========================

			double usedPoints = dto.getRoyaltyPointsUsed() == null ? 0.0 : dto.getRoyaltyPointsUsed();

			if (usedPoints > availablePatientPoints) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Insufficient Loyalty Points");
				return response;
			}

			double amountPaying = dto.getAmountPaying() == null ? 0.0 : dto.getAmountPaying();

			// ==========================
			// Deduct Used Loyalty Points
			// ==========================

			double balanceAfterDeduction = availablePatientPoints - usedPoints;

			// ==========================
			// Earned Points
			// ==========================

			double earnedPoints = (amountPaying * loyaltyPercentage) / 100;

			// ==========================
			// Remaining Loyalty Points
			// ==========================

			double remainingPoints = balanceAfterDeduction + earnedPoints;

			// ==========================
			// Loyalty Snapshot
			// ==========================

			dto.setAvailableRoyaltyPoints(availablePatientPoints);
			dto.setRoyaltyPointsUsed(usedPoints);
			dto.setEarnedRoyaltyPoints(earnedPoints);
			dto.setRemainingRoyaltyPoints(remainingPoints);
			dto.setRoyaltyDeduction(usedPoints);

			// ==========================
			// Fetch Treatment Schedule
			// ==========================

			Optional<TreatmentSchedule> treatmentOptional = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(dto.getClinicId(), dto.getBranchId(),
							dto.getBookingId(), dto.getPatientId());

			if (treatmentOptional.isEmpty()) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Treatment Schedule Not Found");
				return response;
			}

			TreatmentSchedule treatment = treatmentOptional.get();
			// ==========================
			// Final Amount
			// ==========================

			// Amount After Discount
			double packageAmount = treatment.getPackagePrice() == null ? 0.0 : treatment.getPackagePrice();

			dto.setPackageAmount(round(packageAmount));

			// discount: percentage drives the calculation when provided,
			// otherwise falls back to the raw discountAmount sent in the request
			double discountPercentage = dto.getDiscountPercentage() == null ? 0.0 : dto.getDiscountPercentage();

			double discountAmount;

			if (discountPercentage > 0.0 && discountPercentage < 100.0) {
				// Gross-up from amountPaying (net-of-discount), consistent with updatePayment's model.
				discountAmount = (amountPaying * discountPercentage) / (100 - discountPercentage);
			} else {
				discountAmount = dto.getDiscountAmount() == null ? 0.0 : dto.getDiscountAmount();
			}

			// keep dto in sync with whichever value was actually applied
			dto.setDiscountPercentage(discountPercentage);
			dto.setDiscountAmount(round(discountAmount));

			double amountAfterDiscount = packageAmount - discountAmount;

			// 1 Loyalty Point = ₹1
			double finalAmount = amountAfterDiscount - usedPoints;

			dto.setFinalAmount(round(finalAmount));

			double remainingDue = finalAmount - amountPaying;

			if (remainingDue < 0) {
				remainingDue = 0.0;
			}

			dto.setRemainingDue(round(remainingDue));

			// ==========================
			// Recompute currentDue/remainingDue for EVERY sitting, not just
			// the one being paid right now. finalAmount/totalSittings is the
			// single source of truth for per-sitting cost; we don't trust
			// whatever remainingDue values arrived on the DTO.
			// ==========================
			if (dto.getSittings() != null) {

				int totalSittings = dto.getSittings().size();
				double perSittingAmount = totalSittings > 0 ? finalAmount / totalSittings : 0.0;

				final double EPSILON = 0.01;
				double remainingToDistribute = amountPaying;

				for (Sitting s : dto.getSittings()) {

					double sittingPaidNow;
					double sittingRemainingDue;

					if (perSittingAmount <= 0.0) {

						sittingPaidNow = 0.0;
						sittingRemainingDue = perSittingAmount;

					} else if (remainingToDistribute >= perSittingAmount - EPSILON) {

						sittingPaidNow = perSittingAmount;
						sittingRemainingDue = 0.0;

						remainingToDistribute -= perSittingAmount;
						if (remainingToDistribute < 0) {
							remainingToDistribute = 0.0;
						}

					} else if (remainingToDistribute > EPSILON) {

						sittingPaidNow = remainingToDistribute;
						sittingRemainingDue = perSittingAmount - remainingToDistribute;

						remainingToDistribute = 0.0;

					} else {

						sittingPaidNow = 0.0;
						sittingRemainingDue = perSittingAmount;
					}

					s.setCurrentDue(round(perSittingAmount));
					s.setAmountPayingNow(round(sittingPaidNow));
					s.setRemainingDue(round(sittingRemainingDue));
					s.setAvailableRoyaltyPoints(remainingPoints);
				}
			}

			// ==========================
			// Convert DTO to Entity
			// ==========================

			Payment payment = convertToEntity(dto);

			payment.setAvailableRoyaltyPoints(remainingPoints);
			payment.setRoyaltyPointsUsed(usedPoints);
			payment.setEarnedRoyaltyPoints(earnedPoints);
			payment.setRemainingRoyaltyPoints(remainingPoints);
			payment.setRoyaltyDeduction(usedPoints);
			payment.setFinalAmount(round(finalAmount));
			payment.setRemainingDue(round(remainingDue));
			payment.setAmountPaying(round(amountPaying));
			payment.setDiscountPercentage(discountPercentage);
			payment.setDiscountAmount(round(discountAmount));

			// ==========================
			// Editing-lock flag: true once any payment has been made
			// (covers single-sitting case), OR any sitting from #2 onward is
			// already fully booked (covers package case). Sitting #1 is
			// skipped because it auto-carries the SOAP note's session start
			// date/slot and isn't a deliberate booking action by itself.
			// ==========================
			boolean editingDisabled = computeEditingDisabledFlag(amountPaying, treatment.getSittings());
			payment.setEditingDisabled(editingDisabled);
			syncEditingDisabledToSoapNote(dto.getClinicId(), dto.getBranchId(), dto.getBookingId(), dto.getPatientId(),
					editingDisabled);
			// ==========================
			// Transaction History
			// ==========================

			TransactionHistory history = new TransactionHistory();

			history.setReceiptNumber(generateReceiptNumber());
			history.setTransactionId(dto.getTransactionId());
			history.setDate(
			        LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
			);
			
			history.setPaidAt(
			        dto.getPaidAt() != null
			                ? dto.getPaidAt()
			                : LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
			);
			history.setSittingNo(dto.getSittingNo());
			history.setPaymentMode(dto.getPaymentMode());
			history.setAmount(round(amountPaying));
			history.setDiscount(round(discountAmount));
			history.setRoyaltyDeduction(usedPoints);
			history.setEarnedRoyaltyPoints(earnedPoints);
			history.setRemainingRoyaltyPoints(remainingPoints);
			history.setFinalPaid(round(amountPaying));
			// Add transaction history to payment
			if (payment.getTransactionHistory() == null) {
				payment.setTransactionHistory(new ArrayList<>());
			}

			payment.getTransactionHistory().add(history);
			// ==========================
			// Save Payment
			// ==========================

			Payment savedPayment = repository.save(payment);

			// ==========================
			// Update Customer Loyalty
			// ==========================

			customer.setAvailableRoyaltyPoints(remainingPoints);
			customerRepository.save(customer);

			response.setSuccess(true);
			response.setStatus(201);
			response.setMessage("Payment Created Successfully");
			response.setData(convertToDTO(savedPayment));

		} catch (Exception e) {

			e.printStackTrace();

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Payment Creation Failed : " + e.getMessage());
		}

		return response;
	}

	@Override
	@Transactional
	public Response updatePayment(String billingId, PaymentDTO dto) {

		Response response = new Response();

		try {

			Optional<Payment> optional = repository.findByBillingId(billingId);

			if (optional.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Payment Not Found");
				return response;
			}

			Payment payment = optional.get();

			// Fetch Clinic Details
			ResponseEntity<Response> clinicResponse = adminServiceClient.getClinicById(dto.getClinicId());

			if (clinicResponse == null || clinicResponse.getBody() == null) {
				response.setSuccess(false);
				response.setStatus(500);
				response.setMessage("Unable to fetch clinic details.");
				return response;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> clinicMap = (Map<String, Object>) clinicResponse.getBody().getData();
			double loyaltyPercentage = parseLoyaltyPercentage(clinicMap.get("loyaltyPoints"));
			// Fetch Customer
			CustomerOnbording customer = customerRepository.findByPatientId(dto.getPatientId());

			if (customer == null) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Customer not found");
				return response;
			}

			// ==========================
			// Fetch Treatment Schedule (needed to evaluate the editing-lock
			// flag from the sittings' booking state, and to sync sitting count)
			// ==========================
			Optional<TreatmentSchedule> treatmentOptional = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(dto.getClinicId(), dto.getBranchId(),
							dto.getBookingId(), dto.getPatientId());

			if (treatmentOptional.isEmpty()) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Treatment Schedule Not Found");
				return response;
			}

			TreatmentSchedule treatment = treatmentOptional.get();
			// NOTE: this is com.clinicadmin.dto.Sittings (plural) — schedule/booking shape
			List<Sittings> scheduleSittings = treatment.getSittings();

			// Available Loyalty Points
			double availablePatientPoints = customer.getAvailableRoyaltyPoints() == null ? 0.0
					: customer.getAvailableRoyaltyPoints();

			// Redeemed Points (this installment only)
			double usedPoints = dto.getRoyaltyPointsUsed() == null ? 0.0 : dto.getRoyaltyPointsUsed();

			if (usedPoints > availablePatientPoints) {
				response.setSuccess(false);
				response.setStatus(400);
				response.setMessage("Insufficient Loyalty Points");
				return response;
			}

			// Cumulative loyalty points used across ALL installments on this booking
			double previousUsedPoints = payment.getRoyaltyPointsUsed() == null ? 0.0 : payment.getRoyaltyPointsUsed();

			double totalUsedPoints = previousUsedPoints + usedPoints;

			// Current Payment
			double currentPaid = dto.getAmountPaying() == null ? 0.0 : dto.getAmountPaying();

			// Deduct Used Loyalty Points (this installment)
			double balanceAfterDeduction = availablePatientPoints - usedPoints;

			// Earned Points on Current Payment
			double earnedPoints = (currentPaid * loyaltyPercentage) / 100;

			// Remaining Loyalty Points
			double remainingPoints = balanceAfterDeduction + earnedPoints;

			// ==========================
			// Package Amount
			// Get latest package price from Treatment Schedule
			// ==========================

			double packageAmount = treatment.getPackagePrice() == null
			        ? 0.0
			        : treatment.getPackagePrice();

			// Update Payment with latest package amount
			payment.setPackageAmount(round(packageAmount));

			// ==========================
			// Discount: only override the saved discount if THIS call
			// explicitly sends a new non-zero value. Otherwise preserve
			// whatever discount was already applied on a prior installment —
			// discount is a one-time package-level value, not something that
			// needs to be resent on every payment call. Without this, a second
			// installment that doesn't resend the discount silently wipes it
			// out, inflating finalAmount/perSittingAmount back to full price.
			// ==========================
			double currentInstallmentDiscount;

			if (dto.getDiscountPercentage() != null && dto.getDiscountPercentage() > 0.0
			        && dto.getDiscountPercentage() < 100.0) {

			    // amountPaying (currentPaid) is assumed to be NET of the discount —
			    // i.e. what the patient actually hands over after the discount is
			    // taken off. Grossing up recovers the true rupee discount instead of
			    // taking the percentage of the already-discounted amount, which would
			    // understate it (e.g. 10% of ₹1800 = ₹180, not the intended ₹200).
			    currentInstallmentDiscount =
			            (currentPaid * dto.getDiscountPercentage()) / (100 - dto.getDiscountPercentage());

			} else if (dto.getDiscountAmount() != null && dto.getDiscountAmount() > 0.0) {
			    currentInstallmentDiscount = dto.getDiscountAmount();
			} else {
			    currentInstallmentDiscount = 0.0;
			}

			double previousDiscountAmount = payment.getDiscountAmount() == null ? 0.0 : payment.getDiscountAmount();
			double discountAmount = previousDiscountAmount + currentInstallmentDiscount;

			// Percentage is stored purely for reference on THIS installment — it is
			// NOT reused to recompute discountAmount against the full package on a
			// later call (that would double-count).
			double discountPercentage = dto.getDiscountPercentage() == null ? 0.0 : dto.getDiscountPercentage();

			double amountAfterDiscount = packageAmount - discountAmount;

			// Patient Details
			if (dto.getPatientId() != null) {
				payment.setPatientId(dto.getPatientId());
			}

			if (dto.getPatientName() != null && !dto.getPatientName().isEmpty()) {
				payment.setPatientName(dto.getPatientName());
			}

			if (dto.getMobileNumber() != null && !dto.getMobileNumber().isEmpty()) {
				payment.setMobileNumber(dto.getMobileNumber());
			}

			// Clinic Details
			if (dto.getClinicId() != null) {
				payment.setClinicId(dto.getClinicId());
			}

			if (dto.getBranchId() != null) {
				payment.setBranchId(dto.getBranchId());
			}

			// Booking Details
			if (dto.getBookingId() != null) {
				payment.setBookingId(dto.getBookingId());
			}

			// Sitting
			if (dto.getSittingNo() != null) {
				payment.setSittingNo(dto.getSittingNo());
			}

			// Persist the current discount values onto the payment entity
			payment.setDiscountPercentage(discountPercentage);
			payment.setDiscountAmount(round(discountAmount));

			// Loyalty Snapshot
			payment.setAvailableRoyaltyPoints(remainingPoints);
			payment.setRoyaltyPointsUsed(totalUsedPoints);
			payment.setEarnedRoyaltyPoints(earnedPoints);
			payment.setRemainingRoyaltyPoints(remainingPoints);
			payment.setRoyaltyDeduction(totalUsedPoints);

			// Final Amount
			// 1 Loyalty Point = ₹1, use cumulative totalUsedPoints (not just this call's
			// usedPoints)
			double finalAmount = amountAfterDiscount - totalUsedPoints;

			payment.setFinalAmount(round(finalAmount));

			// Previous Paid + Current Paid
			double previousPaid = payment.getAmountPaying() == null ? 0.0 : payment.getAmountPaying();

			double totalPaid = previousPaid + currentPaid;

			payment.setAmountPaying(round(totalPaid));

			// Remaining Due after loyalty deduction
			double remainingDue = finalAmount - totalPaid;

			if (remainingDue < 0) {
				remainingDue = 0.0;
			}

			payment.setRemainingDue(round(remainingDue));

			// ==========================
			// Sync payment.sittings (com.clinicadmin.entity.Sitting — payment progress)
			// with the CURRENT treatment schedule sittings
			// (com.clinicadmin.dto.Sittings — booking shape). Handles sittings added
			// to the schedule after payment was first created — e.g. patient starts
			// with 4 sittings, pays for all 4, then a 5th sitting gets added later.
			// ==========================
			if (scheduleSittings != null) {

				List<Sitting> currentPaymentSittings = payment.getSittings() == null
						? new ArrayList<>()
						: payment.getSittings();

				Map<String, Sitting> existingBySittingNo = currentPaymentSittings.stream()
						.collect(Collectors.toMap(
								Sitting::getSittingNo,
								s -> s,
								(a, b) -> a));

				List<Sitting> syncedSittings = new ArrayList<>();

				for (Sittings scheduleSitting : scheduleSittings) {

					String key = String.valueOf(scheduleSitting.getSittingNumber());
					Sitting existing = existingBySittingNo.get(key);

					if (existing != null) {
						// keep existing payment progress for this sitting
						syncedSittings.add(existing);
					} else {
						// new sitting added to the schedule after payment creation — fresh entry
						Sitting freshSitting = new Sitting();
						freshSitting.setSittingNo(key);
						freshSitting.setAmountPayingNow(0.0);
						freshSitting.setCurrentDue(0.0);
						freshSitting.setRemainingDue(0.0);
						syncedSittings.add(freshSitting);
					}
				}

				payment.setSittings(syncedSittings);
			}

			// ==========================
			// Persist a WATERFALL of the cumulative totalPaid across all
			// NOT-already-locked sittings, in order, so the ledger permanently
			// records however many sittings this booking's total payment
			// genuinely covers — not just the single dto.getSittingNo() target.
			// This keeps the persisted ledger in sync with what the GET endpoints
			// already display live, so the lock survives future changes (like a
			// new sitting being added later inflating perSittingAmount).
			// ==========================
			if (payment.getSittings() != null) {

				int totalSittings = payment.getSittings().size();

				double perSittingAmount = totalSittings > 0 ? finalAmount / totalSittings : 0.0;

				final double EPSILON = 0.01;

				// Sum what's already locked in as permanently Paid, so we don't
				// double-credit it from the waterfall below.
				// Sum what's already locked in as permanently Paid, so we don't
				// double-credit it from the waterfall below. Use the CURRENT
				// perSittingAmount, not each sitting's stale stored `due` —
				// otherwise a discount applied later leaves locked sittings
				// counted at their old (higher) rate, understating how much
				// of totalPaid is actually free to waterfall to later sittings.
				double totalAlreadyLockedPaid = 0.0;
				for (Sitting s : payment.getSittings()) {
					double paid = s.getAmountPayingNow() == null ? 0.0 : s.getAmountPayingNow();
					double due = s.getCurrentDue() == null ? 0.0 : s.getCurrentDue();
					if (due > 0 && paid >= due - EPSILON) {
						totalAlreadyLockedPaid += perSittingAmount;
					}
				}

				double remainingPaidForUnsettled = totalPaid - totalAlreadyLockedPaid;
				if (remainingPaidForUnsettled < 0) {
					remainingPaidForUnsettled = 0.0;
				}

				for (Sitting s : payment.getSittings()) {

					double existingPaid = s.getAmountPayingNow() == null ? 0.0 : s.getAmountPayingNow();
					double existingDue = s.getCurrentDue() == null ? 0.0 : s.getCurrentDue();

					boolean alreadyLockedPaid = existingDue > 0 && existingPaid >= existingDue - EPSILON;

					if (alreadyLockedPaid) {
						// Frozen — untouched, regardless of how perSittingAmount shifts.
						s.setAvailableRoyaltyPoints(remainingPoints);
						continue;
					}

					double sittingPaidNow;
					double sittingRemainingDue;

					if (perSittingAmount <= 0.0) {

						sittingPaidNow = 0.0;
						sittingRemainingDue = perSittingAmount;

					} else if (remainingPaidForUnsettled >= perSittingAmount - EPSILON) {

						sittingPaidNow = perSittingAmount;
						sittingRemainingDue = 0.0;

						remainingPaidForUnsettled -= perSittingAmount;
						if (remainingPaidForUnsettled < 0) {
							remainingPaidForUnsettled = 0.0;
						}

					} else if (remainingPaidForUnsettled > EPSILON) {

						sittingPaidNow = remainingPaidForUnsettled;
						sittingRemainingDue = perSittingAmount - remainingPaidForUnsettled;

						remainingPaidForUnsettled = 0.0;

					} else {

						sittingPaidNow = 0.0;
						sittingRemainingDue = perSittingAmount;
					}

					s.setCurrentDue(round(perSittingAmount));
					s.setAmountPayingNow(round(sittingPaidNow));
					s.setRemainingDue(round(sittingRemainingDue));
					s.setAvailableRoyaltyPoints(remainingPoints);
				}
			}

			// ==========================
			// Editing-lock flag: recompute on every update too, using
			// cumulative totalPaid (not just this call's currentPaid) and
			// the live treatment schedule's sitting booking state.
			// ==========================
			boolean editingDisabled = computeEditingDisabledFlag(totalPaid, scheduleSittings);
			payment.setEditingDisabled(editingDisabled);
			syncEditingDisabledToSoapNote(dto.getClinicId(), dto.getBranchId(), dto.getBookingId(), dto.getPatientId(),
					editingDisabled);

			// Payment Details
			if (dto.getPaymentMode() != null && !dto.getPaymentMode().isEmpty()) {
				payment.setPaymentMode(dto.getPaymentMode());
			}

			if (dto.getTransactionId() != null && !dto.getTransactionId().isEmpty()) {
				payment.setTransactionId(dto.getTransactionId());
			}

			if (dto.getPaidAt() != null) {
				payment.setPaidAt(dto.getPaidAt());
			}

			// Transaction History
			if (payment.getTransactionHistory() == null) {
				payment.setTransactionHistory(new ArrayList<>());
			}

			TransactionHistory history = new TransactionHistory();

			history.setReceiptNumber(generateReceiptNumber());
			history.setTransactionId(dto.getTransactionId());
			history.setDate(
			        LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
			);
			history.setPaidAt(
			        dto.getPaidAt() != null
			                ? dto.getPaidAt()
			                : LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
			);
			history.setSittingNo(dto.getSittingNo());
			history.setPaymentMode(dto.getPaymentMode());
			history.setAmount(round(currentPaid));
			history.setDiscount(round(currentInstallmentDiscount)); // only THIS installment's discount, not the cumulative total // the effective discount applied on THIS payment's package
			history.setRoyaltyDeduction(usedPoints); // this installment's redemption only
			history.setEarnedRoyaltyPoints(earnedPoints);
			history.setRemainingRoyaltyPoints(remainingPoints);
			history.setFinalPaid(round(currentPaid));
			payment.getTransactionHistory().add(history);

			// Save Payment
			Payment updatedPayment = repository.save(payment);

			// Update Customer Loyalty
			customer.setAvailableRoyaltyPoints(remainingPoints);
			customerRepository.save(customer);

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Payment Updated Successfully");
			response.setData(convertToDTO(updatedPayment));

		} catch (Exception e) {

			log.error("updatePayment failed for billingId={}", billingId, e);

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}
	@Override
	public Response getPaymentByBillingId(String clinicId, String branchId, String billingId) {

		Response response = new Response();

		try {

			Optional<Payment> optional = repository.findByClinicIdAndBranchIdAndBillingId(clinicId, branchId,
					billingId);

			if (optional.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Payment Not Found");

				return response;
			}

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Payment Retrieved Successfully");
			response.setData(convertToDTO(optional.get()));

		} catch (Exception e) {

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}

	@Override
	public Response getAllPayments() {

		Response response = new Response();

		try {

			List<Payment> payments = repository.findAll();

			if (payments.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("No Payments Found");

				return response;
			}

			List<PaymentDTO> dtoList = payments.stream().map(this::convertToDTO).toList();

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Payments Retrieved Successfully");
			response.setData(dtoList);

		} catch (Exception e) {

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}

	@Override
	public Response getAllPayments(String clinicId, String branchId) {

		Response response = new Response();

		try {

			List<Payment> payments = repository.findByClinicIdAndBranchId(clinicId, branchId);

			if (payments.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("No Payments Found");

				return response;
			}

			List<PaymentDTO> dtoList = payments.stream().map(this::convertToDTO).toList();

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Payments Retrieved Successfully");
			response.setData(dtoList);

		} catch (Exception e) {

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}

	@Override
	public Response deletePayment(String billingId) {

		Response response = new Response();

		try {

			Optional<Payment> optional = repository.findByBillingId(billingId);

			if (optional.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Payment Not Found");

				return response;
			}

			repository.delete(optional.get());

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Payment Deleted Successfully");

		} catch (Exception e) {

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}

	@Override
	public Response getPaymentByPatientAndBooking(String clinicId, String branchId, String patientId,
			String bookingId) {

		Response response = new Response();

		try {

			List<Payment> payments = repository.findByClinicIdAndBranchIdAndPatientIdAndBookingId(clinicId, branchId,
					patientId, bookingId);

			if (!payments.isEmpty()) {

			    // Pick the most recently created/updated payment
			    Payment payment = payments.get(payments.size() - 1);

			    // ==========================================
			    // Get latest Treatment Schedule
			    // ==========================================

			    Optional<TreatmentSchedule> treatmentOptional =
			            treatmentScheduleRepository
			                    .findByClinicIdAndBranchIdAndBookingIdAndPatientId(
			                            clinicId,
			                            branchId,
			                            bookingId,
			                            patientId
			                    );

			    if (treatmentOptional.isEmpty()) {
			        response.setSuccess(false);
			        response.setStatus(404);
			        response.setMessage("Treatment Schedule Not Found");
			        return response;
			    }

			    TreatmentSchedule treatment = treatmentOptional.get();
			    List<Sittings> scheduleSittings = treatment.getSittings();

			    // ==========================================
			    // Get latest package amount
			    // ==========================================

			    double packageAmount =
			            treatment.getPackagePrice() == null
			                    ? 0.0
			                    : treatment.getPackagePrice();

			    // ==========================================
			    // Get existing discount
			    // ==========================================

			    // discountPercentage is kept only for DISPLAY (whatever
			    // percentage was sent on the most recent installment) — it
			    // must NOT be used to recompute discountAmount against the
			    // full packageAmount.
			    double discountPercentage =
			            payment.getDiscountPercentage() == null
			                    ? 0.0
			                    : payment.getDiscountPercentage();

			    // Use the stored CUMULATIVE discountAmount directly — same reasoning
			    // as getTreatmentScheduleWithPayment: discountPercentage only reflects
			    // the last installment's percentage and must never be reapplied to the
			    // whole package here.
			    double discountAmount =
			            payment.getDiscountAmount() == null
			                    ? 0.0
			                    : payment.getDiscountAmount();

			    // ==========================================
			    // Loyalty points already used
			    // ==========================================

			    double royaltyPointsUsed =
			            payment.getRoyaltyPointsUsed() == null
			                    ? 0.0
			                    : payment.getRoyaltyPointsUsed();

			    // ==========================================
			    // Calculate latest final amount
			    // ==========================================

			    double amountAfterDiscount =
			            packageAmount - discountAmount;

			    double finalAmount =
			            amountAfterDiscount - royaltyPointsUsed;

			    if (finalAmount < 0) {
			        finalAmount = 0.0;
			    }

			    // ==========================================
			    // Already paid amount
			    // ==========================================

			    double totalPaid =
			            payment.getAmountPaying() == null
			                    ? 0.0
			                    : payment.getAmountPaying();

			    // ==========================================
			    // Remaining amount
			    // ==========================================

			    double remainingDue =
			            finalAmount - totalPaid;

			    if (remainingDue < 0) {
			        remainingDue = 0.0;
			    }

			 // ==========================================
			    // Sync sitting breakdown (read-only, NOT saved) so it reflects
			    // any sittings added to the schedule after payment creation.
			    //
			    // Uses the SAME live cumulative-waterfall logic as
			    // getTreatmentScheduleWithPayment — NOT the stale
			    // per-sitting amountPayingNow ledger. That ledger only ever
			    // gets credited to whichever ONE sitting dto.getSittingNo()
			    // pointed at on a given updatePayment call, so if a single
			    // payment covers multiple remaining sittings at once (e.g.
			    // paying for both S4 and S5 in one call), the ledger would
			    // wrongly show S5 as still unpaid even though the total
			    // amount paid actually covers it. Recomputing live from
			    // totalPaid keeps this endpoint consistent with
			    // getTreatmentScheduleWithPayment.
			    // ==========================================
			 // ==========================================
			 // Sync sitting breakdown (read-only, NOT saved) so it reflects
			 // any sittings added to the schedule after payment creation.
			 //
			 // Uses the SAME frozen per-sitting ledger approach as
			 // getTreatmentScheduleWithPayment: a sitting already marked Paid
			 // (amountPayingNow >= its recorded currentDue) stays permanently
			 // Paid, even if perSittingAmount later shifts because a new
			 // sitting was added. Only genuinely unsettled money (paid beyond
			 // what's already locked into Paid sittings) gets waterfalled
			 // across sittings that are still Pending/Partial.
			 // ==========================================
			 List<Sitting> syncedSittings = null;

			 if (scheduleSittings != null) {

			     int totalSittings = scheduleSittings.size();
			     double perSittingAmount = totalSittings > 0 ? finalAmount / totalSittings : 0.0;

			     final double EPSILON = 0.01;

			     Map<String, Sitting> existingBySittingNo = payment.getSittings() == null
			             ? new LinkedHashMap<>()
			             : payment.getSittings().stream()
			                     .filter(s -> s.getSittingNo() != null)
			                     .collect(Collectors.toMap(
			                             Sitting::getSittingNo,
			                             s -> s,
			                             (a, b) -> a,
			                             LinkedHashMap::new));

			     double totalAlreadyLockedPaid = 0.0;
			     for (Sitting s : existingBySittingNo.values()) {
			         double paid = s.getAmountPayingNow() == null ? 0.0 : s.getAmountPayingNow();
			         double due = s.getCurrentDue() == null ? 0.0 : s.getCurrentDue();
			         if (due > 0 && paid >= due - EPSILON) {
			             // Use the CURRENT perSittingAmount, not the stale stored `due` —
			             // otherwise a discount applied on a later installment leaves
			             // locked sittings counted at their old (higher) rate here,
			             // understating how much of totalPaid is free to waterfall to
			             // the remaining sittings.
			             totalAlreadyLockedPaid += perSittingAmount;
			         }
			     }

			     double remainingPaidForUnsettled = totalPaid - totalAlreadyLockedPaid;
			     if (remainingPaidForUnsettled < 0) {
			         remainingPaidForUnsettled = 0.0;
			     }

			     syncedSittings = new ArrayList<>();

			     for (Sittings scheduleSitting : scheduleSittings) {

			         String key = String.valueOf(scheduleSitting.getSittingNumber());
			         Sitting existing = existingBySittingNo.get(key);

			         boolean permanentlyPaid = false;
			         if (existing != null) {
			             double paid = existing.getAmountPayingNow() == null ? 0.0 : existing.getAmountPayingNow();
			             double due = existing.getCurrentDue() == null ? 0.0 : existing.getCurrentDue();
			             if (due > 0 && paid >= due - EPSILON) {
			                 permanentlyPaid = true;
			             }
			         }

			         Sitting sittingView = new Sitting();
			         sittingView.setSittingNo(key);

			         double amountPaidForSitting;
			         double remainingForSitting;

			         if (permanentlyPaid) {
			             // Locked in — stays fully paid regardless of perSittingAmount shifts.
			             amountPaidForSitting = perSittingAmount;
			             remainingForSitting = 0.0;

			         } else if (perSittingAmount <= 0.0) {

			             amountPaidForSitting = 0.0;
			             remainingForSitting = perSittingAmount;

			         } else if (remainingPaidForUnsettled >= perSittingAmount - EPSILON) {

			             amountPaidForSitting = perSittingAmount;
			             remainingForSitting = 0.0;

			             remainingPaidForUnsettled -= perSittingAmount;
			             if (remainingPaidForUnsettled < 0) {
			                 remainingPaidForUnsettled = 0.0;
			             }

			         } else if (remainingPaidForUnsettled > EPSILON) {

			             amountPaidForSitting = remainingPaidForUnsettled;
			             remainingForSitting = perSittingAmount - remainingPaidForUnsettled;

			             remainingPaidForUnsettled = 0;

			         } else {

			             amountPaidForSitting = 0.0;
			             remainingForSitting = perSittingAmount;
			         }

			         sittingView.setAmountPayingNow(round(amountPaidForSitting));
			         sittingView.setCurrentDue(round(perSittingAmount));
			         sittingView.setRemainingDue(round(remainingForSitting));
			         sittingView.setAvailableRoyaltyPoints(payment.getAvailableRoyaltyPoints());

			         syncedSittings.add(sittingView);
			     }
			 }

			    // ==========================================
			    // Convert existing payment to DTO
			    // ==========================================

			    PaymentDTO dto = convertToDTO(payment);

			    // Override with latest live calculations (preview — not persisted)
			    dto.setPackageAmount(round(packageAmount));
			    dto.setDiscountPercentage(discountPercentage);
			    dto.setDiscountAmount(round(discountAmount));
			    dto.setFinalAmount(round(finalAmount));
			    dto.setAmountPaying(round(totalPaid));
			    dto.setRemainingDue(round(remainingDue));

			    if (syncedSittings != null) {
			        dto.setSittings(syncedSittings);
			    }

			    response.setSuccess(true);
			    response.setStatus(200);
			    response.setMessage("Payment Retrieved Successfully (live package amount, not yet saved)");
			    response.setData(dto);

			    return response;
			}
			// ==========================
			// No Payment Yet - Fallback to Treatment Schedule
			// ==========================

			Optional<TreatmentSchedule> treatmentOptional = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (treatmentOptional.isEmpty()) {

				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Payment Not Found");

				return response;
			}

			TreatmentSchedule treatment = treatmentOptional.get();

			double packageAmount = treatment.getPackagePrice() == null ? 0.0 : treatment.getPackagePrice();

			PaymentDTO dto = new PaymentDTO();

			dto.setBillingId(null);
			dto.setPatientId(treatment.getPatientId());
			dto.setPatientName(treatment.getPatientName());
			dto.setMobileNumber(treatment.getMobileNumber());
			dto.setClinicId(treatment.getClinicId());
			dto.setBranchId(treatment.getBranchId());
			dto.setBookingId(treatment.getBookingId());
			dto.setSittingNo(null);

			dto.setPackageAmount(round(packageAmount));
			dto.setDiscountPercentage(0.0);
			dto.setDiscountAmount(0.0);

			dto.setRoyaltyPointsUsed(0.0);
			dto.setRoyaltyDeduction(0.0);
			dto.setEarnedRoyaltyPoints(0.0);
			dto.setAvailableRoyaltyPoints(0.0);
			dto.setRemainingRoyaltyPoints(0.0);

			dto.setFinalAmount(round(packageAmount));
			dto.setAmountPaying(0.0);
			dto.setRemainingDue(round(packageAmount));

			dto.setPaymentMode(null);
			dto.setTransactionId("");
			dto.setPaidAt(null);

			dto.setSittings(null);
			dto.setTransactionHistory(new ArrayList<>());

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Treatment Schedule Retrieved Successfully (No Payment Yet)");
			response.setData(dto);

		} catch (Exception e) {

			log.error("getPaymentByPatientAndBooking failed for clinicId={}, branchId={}, patientId={}, bookingId={}",
					clinicId, branchId, patientId, bookingId, e);

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage(e.getMessage());
		}

		return response;
	}
	/**
	 * Safely parses the loyaltyPoints percentage value coming from AdminService's
	 * clinic response. AdminService may return this field as null, an empty string,
	 * or an otherwise non-numeric value depending on how the clinic record was
	 * created/edited. Any of those cases should be treated as "no loyalty program"
	 * (0%) rather than blowing up the whole payment transaction.
	 */
	private double parseLoyaltyPercentage(Object loyaltyPointsRaw) {

		if (loyaltyPointsRaw == null) {
			return 0.0;
		}

		String loyalty = loyaltyPointsRaw.toString().trim();

		if (loyalty.isEmpty()) {
			return 0.0;
		}

		try {
			double parsed = Double.parseDouble(loyalty);
			return Math.max(parsed, 0.0);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	/**
	 * Editing-lock flag logic.
	 *
	 * True once: - any amount has actually been paid so far (covers the
	 * single-sitting case, where there's nothing else to check), OR - any sitting
	 * numbered 2+ in the treatment schedule has been fully booked (doctorId,
	 * doctorName, date, and slot all present).
	 *
	 * Sitting #1 is intentionally skipped in the booking check — it defaults to the
	 * SOAP note's session start date/slot at schedule-creation time, so its
	 * presence alone doesn't represent a deliberate booking action and shouldn't
	 * lock editing by itself.
	 */
	private boolean computeEditingDisabledFlag(double amountPaidSoFar, List<Sittings> scheduleSittings) {
		if (amountPaidSoFar > 0) {
			return true;
		}
		if (scheduleSittings == null) {
			return false;
		}
		for (Sittings s : scheduleSittings) {
			Integer num = s.getSittingNumber();
			if (num == null || num == 1) {
				continue; // skip sitting #1
			}
			boolean doctorIdSet = s.getDoctorId() != null && !s.getDoctorId().isBlank();
			boolean doctorNameSet = s.getDoctorName() != null && !s.getDoctorName().isBlank();
			boolean dateSet = s.getDate() != null && !s.getDate().isBlank();
			boolean slotSet = s.getSlot() != null && !s.getSlot().isBlank();
			if (doctorIdSet && doctorNameSet && dateSet && slotSet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * BILL-65AF90B087AC
	 */
	private String generateBillingId() {

		String billingId;

		do {

			StringBuilder builder = new StringBuilder("BILL-");

			for (int i = 0; i < 12; i++) {

				builder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
			}

			billingId = builder.toString();

		} while (repository.existsByBillingId(billingId));

		return billingId;
	}

	private String generateReceiptNumber() {

		String receiptNumber;

		do {

			StringBuilder builder = new StringBuilder("REC-");

			for (int i = 0; i < 10; i++) {
				builder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
			}

			receiptNumber = builder.toString();

		} while (repository.existsByTransactionHistory_ReceiptNumber(receiptNumber));

		return receiptNumber;
	}

	public Map<String, Double> retriveInfoBasedOnBookingId(String id) {

		Map<String, Double> res = new LinkedHashMap<>();

		try {
			Optional<Payment> optional = repository.findByBookingIdIgnoreCase(id);

			if (optional.isPresent()) {
				res.put("finalAmount", optional.get().getFinalAmount());
				res.put("paidAmount", optional.get().getAmountPaying());
				res.put("dueAmount", optional.get().getRemainingDue());
				res.put("discount", optional.get().getDiscountAmount());
				res.put("actualAccount", optional.get().getPackageAmount());
				return res;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * DTO -> Entity
	 */
	private Payment convertToEntity(PaymentDTO dto) {

		Payment payment = new Payment();

		// Billing
		payment.setBillingId(generateBillingId());

		// Patient Details
		payment.setPatientId(dto.getPatientId());
		payment.setPatientName(dto.getPatientName());
		payment.setMobileNumber(dto.getMobileNumber());

		// Clinic Details
		payment.setClinicId(dto.getClinicId());
		payment.setBranchId(dto.getBranchId());

		// Booking
		payment.setBookingId(dto.getBookingId());
		payment.setSittingNo(dto.getSittingNo());

		// Package
		payment.setPackageAmount(dto.getPackageAmount());

		// Discount
		payment.setDiscountPercentage(dto.getDiscountPercentage());
		payment.setDiscountAmount(dto.getDiscountAmount());

		// Loyalty
		payment.setAvailableRoyaltyPoints(dto.getAvailableRoyaltyPoints());
		payment.setRoyaltyPointsUsed(dto.getRoyaltyPointsUsed());
		payment.setEarnedRoyaltyPoints(dto.getEarnedRoyaltyPoints());
		payment.setRemainingRoyaltyPoints(dto.getRemainingRoyaltyPoints());
		payment.setRoyaltyDeduction(dto.getRoyaltyDeduction());

		// Payment
		payment.setFinalAmount(dto.getFinalAmount());
		payment.setAmountPaying(dto.getAmountPaying());
		payment.setRemainingDue(dto.getRemainingDue());

		payment.setPaymentMode(dto.getPaymentMode());
		payment.setTransactionId(dto.getTransactionId());
		payment.setPaidAt(dto.getPaidAt());

		payment.setSittings(dto.getSittings());

		if (dto.getTransactionHistory() != null) {
			payment.setTransactionHistory(dto.getTransactionHistory());
		} else {
			payment.setTransactionHistory(new ArrayList<>());
		}

		return payment;
	}

	/**
	 * Entity -> DTO
	 */
	private PaymentDTO convertToDTO(Payment payment) {

		PaymentDTO dto = new PaymentDTO();

		dto.setBillingId(payment.getBillingId());

		// Patient Details
		dto.setPatientId(payment.getPatientId());
		dto.setPatientName(payment.getPatientName());
		dto.setMobileNumber(payment.getMobileNumber());

		// Clinic Details
		dto.setClinicId(payment.getClinicId());
		dto.setBranchId(payment.getBranchId());

		// Booking Details
		dto.setBookingId(payment.getBookingId());

		// Sitting Details
		dto.setSittingNo(payment.getSittingNo());

		// Package Details
		dto.setPackageAmount(payment.getPackageAmount() == null ? null : round(payment.getPackageAmount()));


		// Discount
		dto.setDiscountPercentage(payment.getDiscountPercentage());
		dto.setDiscountAmount(payment.getDiscountAmount() == null ? null : round(payment.getDiscountAmount()));

		// Loyalty
		dto.setAvailableRoyaltyPoints(payment.getAvailableRoyaltyPoints());
		dto.setRoyaltyPointsUsed(payment.getRoyaltyPointsUsed());
		dto.setEarnedRoyaltyPoints(payment.getEarnedRoyaltyPoints());
		dto.setRemainingRoyaltyPoints(payment.getRemainingRoyaltyPoints());
		dto.setRoyaltyDeduction(payment.getRoyaltyDeduction());

		// Payment Details
		dto.setFinalAmount(payment.getFinalAmount() == null ? null : round(payment.getFinalAmount()));
		dto.setAmountPaying(payment.getAmountPaying() == null ? null : round(payment.getAmountPaying()));
		dto.setRemainingDue(payment.getRemainingDue() == null ? null : round(payment.getRemainingDue()));
		dto.setPaymentMode(payment.getPaymentMode());
		dto.setTransactionId(payment.getTransactionId());
		dto.setPaidAt(payment.getPaidAt());

		// Lists
		dto.setSittings(payment.getSittings());
		dto.setTransactionHistory(payment.getTransactionHistory());

		// NEW: editing-lock flag
		dto.setEditingDisabled(payment.isEditingDisabled());

		return dto;
	}

	@Override
	public Response getTreatmentScheduleWithPayment(String clinicId, String branchId, String bookingId,
			String patientId) {

		Response response = new Response();

		try {

			Optional<TreatmentSchedule> optional = treatmentScheduleRepository
					.findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);

			if (optional.isEmpty()) {
				response.setSuccess(false);
				response.setStatus(404);
				response.setMessage("Treatment Schedule Not Found");
				return response;
			}

			TreatmentSchedule treatmentSchedule = optional.get();

			// Payment
			List<Payment> paymentList = repository.findByClinicIdAndBranchIdAndPatientIdAndBookingId(clinicId, branchId,
					patientId, bookingId);

			Payment payment = paymentList.isEmpty() ? null : paymentList.get(paymentList.size() - 1);

			Map<String, Object> result = new LinkedHashMap<>();
			List<Map<String, Object>> sittingList = new ArrayList<>();

			double finalAmount = 0.0;
			double totalPaid = 0.0;
			double remainingDue = 0.0;

			int totalSittings = treatmentSchedule.getSittings().size();

			// Real per-sitting ledger: once a sitting is marked Paid via
			// updatePayment, it's permanently Paid — even if perSittingAmount
			// later shifts because a new sitting was added to the schedule.
			// Keyed by sittingNo (matches Sitting.sittingNo / Sittings.sittingNumber).
			Map<String, Sitting> paidSittingsByNo = new LinkedHashMap<>();

			if (payment != null) {

			    // ==========================================
			    // Get latest package amount from
			    // TreatmentSchedule
			    // ==========================================

			    double packageAmount = treatmentSchedule.getPackagePrice() == null
			            ? 0.0
			            : treatmentSchedule.getPackagePrice();

			    // ==========================================
			    // Get discount from existing payment
			    // ==========================================

			    // Use the stored CUMULATIVE discountAmount directly — do NOT recompute
			    // from discountPercentage × full packageAmount. discountPercentage only
			    // reflects whatever percentage was sent on the MOST RECENT installment;
			    // reapplying it against the whole package would retroactively discount
			    // money that was already paid at full price on earlier installments.
			    double discountAmount = payment.getDiscountAmount() == null
			            ? 0.0
			            : payment.getDiscountAmount();

			    // ==========================================
			    // Loyalty points already used
			    // ==========================================

			    double usedRoyaltyPoints = payment.getRoyaltyPointsUsed() == null
			            ? 0.0
			            : payment.getRoyaltyPointsUsed();

			    // ==========================================
			    // Calculate latest final amount
			    // ==========================================

			    double amountAfterDiscount =
			            packageAmount - discountAmount;

			    finalAmount =
			            amountAfterDiscount - usedRoyaltyPoints;

			    if (finalAmount < 0) {
			        finalAmount = 0.0;
			    }

			    // ==========================================
			    // Total amount already paid
			    // ==========================================

			    totalPaid = payment.getAmountPaying() == null
			            ? 0.0
			            : payment.getAmountPaying();

			    // ==========================================
			    // Remaining amount
			    // ==========================================

			    remainingDue = finalAmount - totalPaid;

			    if (remainingDue < 0) {
			        remainingDue = 0.0;
			    }

			    // Index the real per-sitting ledger. A sitting present here with
			    // amountPayingNow >= its recorded currentDue is permanently Paid,
			    // regardless of how perSittingAmount recalculates later.
			    if (payment.getSittings() != null) {
			        for (Sitting s : payment.getSittings()) {
			            if (s.getSittingNo() != null) {
			                paidSittingsByNo.put(s.getSittingNo(), s);
			            }
			        }
			    }

			    result.put("clinicId", payment.getClinicId());
			    result.put("branchId", payment.getBranchId());
			    result.put("bookingId", payment.getBookingId());
			    result.put("patientId", payment.getPatientId());
			    result.put("patientName", payment.getPatientName());
			    result.put("mobileNumber", payment.getMobileNumber());

			    // Latest package amount
			    result.put("packageAmount", round(packageAmount));
			    result.put("finalAmount", round(finalAmount));
			    result.put("amountPaid", round(totalPaid));
			    result.put("remainingDue", round(remainingDue));

			    result.put("editingDisabled", payment.isEditingDisabled());
			} else {

				finalAmount = treatmentSchedule.getPackagePrice() == null ? 0.0 : treatmentSchedule.getPackagePrice();

				totalPaid = 0.0;
				remainingDue = finalAmount;

				result.put("clinicId", clinicId);
				result.put("branchId", branchId);
				result.put("bookingId", bookingId);
				result.put("patientId", patientId);
				result.put("patientName", treatmentSchedule.getPatientName());
				result.put("mobileNumber", treatmentSchedule.getMobileNumber());
				result.put("finalAmount", round(finalAmount));
				result.put("amountPaid", round(totalPaid));
				result.put("remainingDue", round(remainingDue));
				result.put("editingDisabled", computeEditingDisabledFlag(0.0, treatmentSchedule.getSittings()));
			}

			double perSittingAmount = totalSittings > 0 ? finalAmount / totalSittings : 0.0;

			final double EPSILON = 0.01;

			// ==========================================
			// Any money paid but not yet attributed to a specific already-Paid
			// sitting (e.g. a lump payment not yet synced per-sitting, or
			// genuinely unallocated overpay) is applied as a waterfall ONLY
			// across sittings that are NOT already permanently Paid in the
			// ledger above — new sittings or ones still owing.
			// ==========================================
			double totalAlreadyLockedPaid = 0.0;
			for (Sitting s : paidSittingsByNo.values()) {
				double paid = s.getAmountPayingNow() == null ? 0.0 : s.getAmountPayingNow();
				double due = s.getCurrentDue() == null ? 0.0 : s.getCurrentDue();
				if (due > 0 && paid >= due - EPSILON) {
					// Use the CURRENT perSittingAmount, not the sitting's stale stored
					// `due` — otherwise a discount applied on a later installment leaves
					// locked sittings counted at their old (higher) rate here, understating
					// how much of totalPaid is free to waterfall to the remaining sittings,
					// which is exactly what caused S16 to show "Partially Paid" with total due = 0.
					totalAlreadyLockedPaid += perSittingAmount;
				}
			}

			double remainingPaidForUnsettled = totalPaid - totalAlreadyLockedPaid;
			if (remainingPaidForUnsettled < 0) {
				remainingPaidForUnsettled = 0.0;
			}

			for (Sittings treatmentSitting : treatmentSchedule.getSittings()) {

				Map<String, Object> sittingMap = new LinkedHashMap<>();

				String key = String.valueOf(treatmentSitting.getSittingNumber());
				Sitting ledgerEntry = paidSittingsByNo.get(key);

				double amountPaidForSitting;
				double remainingForSitting;
				String paymentStatus;

				boolean permanentlyPaid = false;
				if (ledgerEntry != null) {
					double paid = ledgerEntry.getAmountPayingNow() == null ? 0.0 : ledgerEntry.getAmountPayingNow();
					double due = ledgerEntry.getCurrentDue() == null ? 0.0 : ledgerEntry.getCurrentDue();
					if (due > 0 && paid >= due - EPSILON) {
						permanentlyPaid = true;
					}
				}

				if (permanentlyPaid) {
					// Locked in — stays Paid forever, unaffected by perSittingAmount
					// shifting due to new sittings being added later.
					paymentStatus = "Paid";
					amountPaidForSitting = perSittingAmount;
					remainingForSitting = 0.0;

				} else if (perSittingAmount <= 0.0) {

					paymentStatus = "Pending";
					amountPaidForSitting = 0.0;
					remainingForSitting = perSittingAmount;

				} else if (remainingPaidForUnsettled >= perSittingAmount - EPSILON) {

					paymentStatus = "Paid";
					amountPaidForSitting = perSittingAmount;
					remainingForSitting = 0.0;

					remainingPaidForUnsettled -= perSittingAmount;
					if (remainingPaidForUnsettled < 0) {
						remainingPaidForUnsettled = 0.0;
					}

				} else if (remainingPaidForUnsettled > EPSILON) {

					paymentStatus = "Partially Paid";
					amountPaidForSitting = remainingPaidForUnsettled;
					remainingForSitting = perSittingAmount - remainingPaidForUnsettled;

					remainingPaidForUnsettled = 0;

				} else {

					paymentStatus = "Pending";
					amountPaidForSitting = 0.0;
					remainingForSitting = perSittingAmount;
				}

				// doctorName comes ONLY from the sitting itself - SOAP note fallback removed
				String doctorName = treatmentSitting.getDoctorName();
				if (doctorName == null || doctorName.isBlank()) {
					doctorName = "N/A";
				}

				// date comes ONLY from the sitting itself - SOAP note fallback removed
				String date = treatmentSitting.getDate();
				if (date == null || date.isBlank()) {
					date = "N/A";
				}

				String doctorId = treatmentSitting.getDoctorId();
				if (doctorId == null || doctorId.isBlank()) {
					doctorId = "N/A";
				}

				String slot = treatmentSitting.getSlot();
				if (slot == null || slot.isBlank()) {
					slot = "N/A";
				}

				sittingMap.put("sittingsId", treatmentSitting.getSittingsId());
				sittingMap.put("sittingNumber", treatmentSitting.getSittingNumber());
				sittingMap.put("doctorId", doctorId);
				sittingMap.put("doctorName", doctorName);
				sittingMap.put("date", date);
				sittingMap.put("slot", slot);

				sittingMap.put("perSittingAmount", round(perSittingAmount));
				sittingMap.put("amountPaid", round(amountPaidForSitting));
				sittingMap.put("remainingAmount", round(remainingForSitting));
				sittingMap.put("paymentStatus", paymentStatus);

				sittingList.add(sittingMap);
			}

			result.put("sittings", sittingList);

			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Treatment Schedule Retrieved Successfully");
			response.setData(result);

		} catch (Exception e) {

			log.error("getTreatmentScheduleWithPayment failed for clinicId={}, branchId={}, bookingId={}, patientId={}",
					clinicId, branchId, bookingId, patientId, e);

			response.setSuccess(false);
			response.setStatus(500);
			response.setMessage("Failed to retrieve Treatment Schedule : " + e.getMessage());
		}

		return response;
	}
	/**
	 * Mirrors the editing-lock flag onto the linked SoapNote so that
	 * updateSoapNote() can reject edits once treatment has started. Swallows errors
	 * — this is a best-effort sync, must never fail the payment flow.
	 */
	private void syncEditingDisabledToSoapNote(String clinicId, String branchId, String bookingId, String patientId,
	        boolean editingDisabled) {
	    try {
	        Optional<SoapNote> soapNoteOpt = soapNoteRepository
	                .findByClinicIdAndBranchIdAndBookingIdAndPatientId(clinicId, branchId, bookingId, patientId);
	        if (soapNoteOpt.isPresent()) {
	            SoapNote soapNote = soapNoteOpt.get();
	            if (editingDisabled != soapNote.isEditingDisabled()) { 
	                soapNote.setEditingDisabled(editingDisabled);
	                soapNoteRepository.save(soapNote);
	                log.info("editingDisabled synced to SoapNote via payment. soapNoteId={}, value={}",
	                        soapNote.getId(), editingDisabled);
	            }
	        } else {
	            // NEW: log the miss instead of silently swallowing it
	            log.warn("syncEditingDisabledToSoapNote: no SoapNote found for clinicId={}, branchId={}, "
	                    + "bookingId={}, patientId={}", clinicId, branchId, bookingId, patientId);
	        }
	    } catch (Exception e) {
	        log.warn("Failed to sync editingDisabled to SoapNote: {}", e.getMessage());
	    }
	}
	/**
	 * Rounds a monetary value to the nearest whole rupee. Applied at the
	 * point values are stored/returned so persisted and displayed amounts
	 * are always clean round figures — never long floating-point decimals
	 * like 719.9999999998.
	 */
	private double round(double value) {
	    return Math.round(value);
	}
}




//package com.clinicadmin.service.impl;
//
//import java.security.SecureRandom;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.clinicadmin.dto.PaymentDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.Sittings;
//import com.clinicadmin.entity.CustomerOnbording;
//import com.clinicadmin.entity.Payment;
//import com.clinicadmin.entity.TransactionHistory;
//import com.clinicadmin.entity.TreatmentSchedule;
//import com.clinicadmin.feignclient.AdminServiceClient;
//import com.clinicadmin.repository.CustomerOnboardingRepository;
//import com.clinicadmin.repository.PaymentRepository;
//import com.clinicadmin.repository.SoapNoteRepository;
//import com.clinicadmin.repository.TreatmentScheduleRepository;
//import com.clinicadmin.service.PaymentService;
//
//@Service
//public class PaymentServiceImpl implements PaymentService {
//
//    @Autowired
//    private PaymentRepository repository;
//
//    @Autowired
//    private AdminServiceClient adminServiceClient;
//
//    @Autowired
//    private CustomerOnboardingRepository customerRepository;
//
//    @Autowired
//    private TreatmentScheduleRepository treatmentScheduleRepository;
//
//    @Autowired
//    private SoapNoteRepository soapNoteRepository;
//
//    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//
//    private static final SecureRandom RANDOM = new SecureRandom();
//
//    @Override
//    @Transactional
//    public Response createPayment(PaymentDTO dto) {
//
//        Response response = new Response();
//
//        try {
//
//            // ==========================
//            // Fetch Clinic Details
//            // ==========================
//
//            ResponseEntity<Response> clinicResponse =
//                    adminServiceClient.getClinicById(dto.getClinicId());
//
//            if (clinicResponse == null || clinicResponse.getBody() == null) {
//                response.setSuccess(false);
//                response.setStatus(500);
//                response.setMessage("Unable to fetch clinic details.");
//                return response;
//            }
//
//            Response clinicApiResponse = clinicResponse.getBody();
//
//            if (!clinicApiResponse.isSuccess() || clinicApiResponse.getData() == null) {
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage(clinicApiResponse.getMessage() != null
//                        ? clinicApiResponse.getMessage()
//                        : "Clinic not found");
//                return response;
//            }
//
//            // ==========================
//            // Get Loyalty Percentage Only
//            // ==========================
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> clinicMap =
//                    (Map<String, Object>) clinicApiResponse.getData();
//
//            double loyaltyPercentage = parseLoyaltyPercentage(clinicMap.get("loyaltyPoints"));
//
//            // ==========================
//            // Fetch Customer
//            // ==========================
//
//            CustomerOnbording customer =
//                    customerRepository.findByPatientId(dto.getPatientId());
//
//            if (customer == null) {
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Customer not found");
//                return response;
//            }
//
//            // ==========================
//            // Available Loyalty Points
//            // ==========================
//
//            double availablePatientPoints =
//                    customer.getAvailableRoyaltyPoints() == null
//                            ? 0.0
//                            : customer.getAvailableRoyaltyPoints();
//
//            // ==========================
//            // Used Loyalty Points
//            // ==========================
//
//            double usedPoints =
//                    dto.getRoyaltyPointsUsed() == null
//                            ? 0.0
//                            : dto.getRoyaltyPointsUsed();
//
//            if (usedPoints > availablePatientPoints) {
//                response.setSuccess(false);
//                response.setStatus(400);
//                response.setMessage("Insufficient Loyalty Points");
//                return response;
//            }
//
//            double amountPaying =
//                    dto.getAmountPaying() == null
//                            ? 0.0
//                            : dto.getAmountPaying();
//
//         // ==========================
//         // Deduct Used Loyalty Points
//         // ==========================
//
//         double balanceAfterDeduction =
//                 availablePatientPoints - usedPoints;
//
//         // ==========================
//         // Earned Points
//         // ==========================
//
//         double earnedPoints =
//                 (amountPaying * loyaltyPercentage) / 100;
//
//         // ==========================
//         // Remaining Loyalty Points
//         // ==========================
//
//         double remainingPoints =
//                 balanceAfterDeduction + earnedPoints;
//
//            // ==========================
//            // Loyalty Snapshot
//            // ==========================
//
//         dto.setAvailableRoyaltyPoints(availablePatientPoints);
//         dto.setRoyaltyPointsUsed(usedPoints);
//         dto.setEarnedRoyaltyPoints(earnedPoints);
//         dto.setRemainingRoyaltyPoints(remainingPoints);
//         dto.setRoyaltyDeduction(usedPoints);
//         
//         
//      // ==========================
//      // Fetch Treatment Schedule
//      // ==========================
//
//      Optional<TreatmentSchedule> treatmentOptional =
//              treatmentScheduleRepository
//                      .findByClinicIdAndBranchIdAndBookingIdAndPatientId(
//                              dto.getClinicId(),
//                              dto.getBranchId(),
//                              dto.getBookingId(),
//                              dto.getPatientId());
//
//      if (treatmentOptional.isEmpty()) {
//          response.setSuccess(false);
//          response.setStatus(404);
//          response.setMessage("Treatment Schedule Not Found");
//          return response;
//      }
//
//      TreatmentSchedule treatment = treatmentOptional.get();
//            // ==========================
//            // Final Amount
//            // ==========================
//
//         // Amount After Discount
//      double packageAmount =
//    	        treatment.getPackagePrice() == null
//    	                ? 0.0
//    	                : treatment.getPackagePrice();
//
//    	dto.setPackageAmount(packageAmount);
//
//    	// FIX: discount now correctly derived — percentage drives the calculation when provided,
//    	// otherwise falls back to the raw discountAmount sent in the request
//    	double discountPercentage =
//    	        dto.getDiscountPercentage() == null
//    	                ? 0.0
//    	                : dto.getDiscountPercentage();
//
//    	double discountAmount;
//
//    	if (discountPercentage > 0.0) {
//    	    discountAmount = (packageAmount * discountPercentage) / 100;
//    	} else {
//    	    discountAmount =
//    	            dto.getDiscountAmount() == null
//    	                    ? 0.0
//    	                    : dto.getDiscountAmount();
//    	}
//
//    	// keep dto in sync with whichever value was actually applied
//    	dto.setDiscountPercentage(discountPercentage);
//    	dto.setDiscountAmount(discountAmount);
//
//    	double amountAfterDiscount =
//    	        packageAmount - discountAmount;
//    	
//            // 1 Loyalty Point = ₹1
//            double finalAmount =
//                    amountAfterDiscount - usedPoints;
//
//            dto.setFinalAmount(finalAmount);
//
//            double remainingDue =
//                    finalAmount - amountPaying;
//
//            if (remainingDue < 0) {
//                remainingDue = 0.0;
//            }
//
//            dto.setRemainingDue(remainingDue);
//            if (dto.getSittings() != null) {
//
//                int totalSittings = dto.getSittings().size();
//
//                double perSittingAmount =
//                        totalSittings > 0
//                                ? finalAmount / totalSittings
//                                : 0.0;
//
//                dto.getSittings().forEach(s -> {
//
//                    if (String.valueOf(s.getSittingNo())
//                            .equals(String.valueOf(dto.getSittingNo()))) {
//
//                    	double paidNow = amountPaying;
//
//                    	s.setAmountPayingNow(paidNow);
//
//                    	double sittingRemainingDue =
//                    	        perSittingAmount - paidNow;
//
//                    	if (sittingRemainingDue < 0) {
//                    	    sittingRemainingDue = 0.0;
//                    	}
//
//                        s.setCurrentDue(perSittingAmount);
//                        s.setAvailableRoyaltyPoints(remainingPoints);
//                        s.setRemainingDue(sittingRemainingDue);
//                    }
//                });
//            }
//
//            // ==========================
//            // Convert DTO to Entity
//            // ==========================
//
//            Payment payment = convertToEntity(dto);
//            
//            payment.setAvailableRoyaltyPoints(remainingPoints);
//            payment.setRoyaltyPointsUsed(usedPoints);
//            payment.setEarnedRoyaltyPoints(earnedPoints);
//            payment.setRemainingRoyaltyPoints(remainingPoints);
//            payment.setRoyaltyDeduction(usedPoints);
//            payment.setFinalAmount(finalAmount);
//            payment.setRemainingDue(remainingDue);
//            payment.setAmountPaying(amountPaying);
//            payment.setDiscountPercentage(discountPercentage);
//            payment.setDiscountAmount(discountAmount);
//
//            // ==========================
//            // Transaction History
//            // ==========================
//
//            TransactionHistory history = new TransactionHistory();
//
//            history.setReceiptNumber(generateReceiptNumber());
//            history.setTransactionId(dto.getTransactionId());
//            history.setDate(dto.getPaidAt());
//            history.setSittingNo(dto.getSittingNo());
//            history.setPaymentMode(dto.getPaymentMode());
//            history.setAmount(amountPaying);
//            history.setDiscount(discountAmount);
//            history.setRoyaltyDeduction(usedPoints);
//            history.setEarnedRoyaltyPoints(earnedPoints);
//            history.setRemainingRoyaltyPoints(remainingPoints);
//            history.setFinalPaid(amountPaying);
//         // Add transaction history to payment
//            if (payment.getTransactionHistory() == null) {
//                payment.setTransactionHistory(new ArrayList<>());
//            }
//
//            payment.getTransactionHistory().add(history);
//            // ==========================
//            // Save Payment
//            // ==========================
//
//            Payment savedPayment = repository.save(payment);
//
//            // ==========================
//            // Update Customer Loyalty
//            // ==========================
//
//            customer.setAvailableRoyaltyPoints(remainingPoints);
//            customerRepository.save(customer);
//
//            response.setSuccess(true);
//            response.setStatus(201);
//            response.setMessage("Payment Created Successfully");
//            response.setData(convertToDTO(savedPayment));
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage("Payment Creation Failed : " + e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    @Transactional
//    public Response updatePayment(String billingId, PaymentDTO dto) {
//
//        Response response = new Response();
//
//        try {
//
//            Optional<Payment> optional = repository.findByBillingId(billingId);
//
//            if (optional.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Payment Not Found");
//                return response;
//            }
//
//            Payment payment = optional.get();
//
//         // Fetch Clinic Details
//            ResponseEntity<Response> clinicResponse =
//                    adminServiceClient.getClinicById(dto.getClinicId());
//
//            if (clinicResponse == null || clinicResponse.getBody() == null) {
//                response.setSuccess(false);
//                response.setStatus(500);
//                response.setMessage("Unable to fetch clinic details.");
//                return response;
//            }
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> clinicMap =
//                    (Map<String, Object>) clinicResponse.getBody().getData();
//            double loyaltyPercentage = parseLoyaltyPercentage(clinicMap.get("loyaltyPoints"));
//            // Fetch Customer
//            CustomerOnbording customer =
//                    customerRepository.findByPatientId(dto.getPatientId());
//
//            if (customer == null) {
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Customer not found");
//                return response;
//            }
//
//            // Available Loyalty Points
//            double availablePatientPoints =
//                    customer.getAvailableRoyaltyPoints() == null
//                            ? 0.0
//                            : customer.getAvailableRoyaltyPoints();
//
//            // Redeemed Points (this installment only)
//            double usedPoints =
//                    dto.getRoyaltyPointsUsed() == null
//                            ? 0.0
//                            : dto.getRoyaltyPointsUsed();
//
//            if (usedPoints > availablePatientPoints) {
//                response.setSuccess(false);
//                response.setStatus(400);
//                response.setMessage("Insufficient Loyalty Points");
//                return response;
//            }
//
//            // Cumulative loyalty points used across ALL installments on this booking
//            double previousUsedPoints =
//                    payment.getRoyaltyPointsUsed() == null
//                            ? 0.0
//                            : payment.getRoyaltyPointsUsed();
//
//            double totalUsedPoints = previousUsedPoints + usedPoints;
//
//            // Current Payment
//            double currentPaid =
//                    dto.getAmountPaying() == null
//                            ? 0.0
//                            : dto.getAmountPaying();
//
//            // Deduct Used Loyalty Points (this installment)
//            double balanceAfterDeduction =
//                    availablePatientPoints - usedPoints;
//
//            // Earned Points on Current Payment
//            double earnedPoints =
//                    (currentPaid * loyaltyPercentage) / 100;
//
//            // Remaining Loyalty Points
//            double remainingPoints =
//                    balanceAfterDeduction + earnedPoints;
//
//            // Package Amount - null-safe
//            double packageAmount =
//                    payment.getPackageAmount() == null
//                            ? 0.0
//                            : payment.getPackageAmount();
//
//
//            // Discount now read from the incoming request (dto), not the stale saved payment.
//            // FIX: discount now read from the incoming request (dto), not the stale saved payment.
//            // Percentage drives the calculation when provided, otherwise falls back to raw discountAmount.
//            double discountPercentage =
//                    dto.getDiscountPercentage() == null
//                            ? 0.0
//                            : dto.getDiscountPercentage();
//
//            double discountAmount;
//
//            if (discountPercentage > 0.0) {
//                discountAmount = (packageAmount * discountPercentage) / 100;
//            } else {
//                discountAmount =
//                        dto.getDiscountAmount() == null
//                                ? 0.0
//                                : dto.getDiscountAmount();
//            }
//
//            double amountAfterDiscount = packageAmount - discountAmount;
//          
//         // Patient Details
//            if (dto.getPatientId() != null) {
//                payment.setPatientId(dto.getPatientId());
//            }
//
//            if (dto.getPatientName() != null && !dto.getPatientName().isEmpty()) {
//                payment.setPatientName(dto.getPatientName());
//            }
//
//            if (dto.getMobileNumber() != null && !dto.getMobileNumber().isEmpty()) {
//                payment.setMobileNumber(dto.getMobileNumber());
//            }
//
//            // Clinic Details
//            if (dto.getClinicId() != null) {
//                payment.setClinicId(dto.getClinicId());
//            }
//
//            if (dto.getBranchId() != null) {
//                payment.setBranchId(dto.getBranchId());
//            }
//
//            // Booking Details
//            if (dto.getBookingId() != null) {
//                payment.setBookingId(dto.getBookingId());
//            }
//
//            // Sitting
//            if (dto.getSittingNo() != null) {
//                payment.setSittingNo(dto.getSittingNo());
//            }
//
//
//
//            // Persist the current discount values onto the payment entity
//
//            // FIX: uncommented — actually persist the new discount values onto the payment entity
//            payment.setDiscountPercentage(discountPercentage);
//            payment.setDiscountAmount(discountAmount);
//
//            // Loyalty Snapshot
//            payment.setAvailableRoyaltyPoints(remainingPoints);
//            payment.setRoyaltyPointsUsed(totalUsedPoints);
//            payment.setEarnedRoyaltyPoints(earnedPoints);
//            payment.setRemainingRoyaltyPoints(remainingPoints);
//            payment.setRoyaltyDeduction(totalUsedPoints);
//
//            // Final Amount
//            // 1 Loyalty Point = ₹1, use cumulative totalUsedPoints (not just this call's usedPoints)
//            double finalAmount =
//                    amountAfterDiscount - totalUsedPoints;
//
//            payment.setFinalAmount(finalAmount);
//
//            // Previous Paid + Current Paid
//            double previousPaid =
//                    payment.getAmountPaying() == null
//                            ? 0.0
//                            : payment.getAmountPaying();
//
//            double totalPaid = previousPaid + currentPaid;
//
//            payment.setAmountPaying(totalPaid);
//
//            // Remaining Due after loyalty deduction
//            double remainingDue = finalAmount - totalPaid;
//
//            if (remainingDue < 0) {
//                remainingDue = 0.0;
//            }
//
//            payment.setRemainingDue(remainingDue);
//            if (payment.getSittings() != null) {
//
//                int totalSittings = payment.getSittings().size();
//
//                double perSittingAmount =
//                        totalSittings > 0
//                                ? finalAmount / totalSittings
//                                : 0.0;
//
//                payment.getSittings().forEach(s -> {
//
//                    if (String.valueOf(s.getSittingNo())
//                            .equals(String.valueOf(dto.getSittingNo()))) {
//
//                        double alreadyPaid =
//                                s.getAmountPayingNow() == null
//                                        ? 0.0
//                                        : s.getAmountPayingNow();
//
//                        double totalSittingPaid = alreadyPaid + currentPaid;
//
//                        double sittingRemainingDue =
//                                perSittingAmount - totalSittingPaid;
//
//                        if (sittingRemainingDue < 0) {
//                            sittingRemainingDue = 0.0;
//                        }
//
//                        s.setCurrentDue(perSittingAmount);
//                        s.setAmountPayingNow(totalSittingPaid);
//                        s.setRemainingDue(sittingRemainingDue);
//                        s.setAvailableRoyaltyPoints(remainingPoints);
//                    }
//                });
//            }
//
//           
//         // Payment Details
//            if (dto.getPaymentMode() != null && !dto.getPaymentMode().isEmpty()) {
//                payment.setPaymentMode(dto.getPaymentMode());
//            }
//
//            if (dto.getTransactionId() != null && !dto.getTransactionId().isEmpty()) {
//                payment.setTransactionId(dto.getTransactionId());
//            }
//
//            if (dto.getPaidAt() != null) {
//                payment.setPaidAt(dto.getPaidAt());
//            }
//
//
//            // Transaction History
//            if (payment.getTransactionHistory() == null) {
//                payment.setTransactionHistory(new ArrayList<>());
//            }
//
//            TransactionHistory history = new TransactionHistory();
//
//            history.setReceiptNumber(generateReceiptNumber());
//            history.setTransactionId(dto.getTransactionId());
//            history.setDate(dto.getPaidAt());
//            history.setSittingNo(dto.getSittingNo());
//            history.setPaymentMode(dto.getPaymentMode());
//            history.setAmount(currentPaid);
//            history.setDiscount(discountAmount); // whatever discount was applied in THIS call
//            history.setRoyaltyDeduction(usedPoints); // this installment's redemption only
//            history.setEarnedRoyaltyPoints(earnedPoints);
//            history.setRemainingRoyaltyPoints(remainingPoints);
//            history.setFinalPaid(currentPaid);
//            payment.getTransactionHistory().add(history);
//
//            // Save Payment
//            Payment updatedPayment = repository.save(payment);
//
//            // Update Customer Loyalty
//            customer.setAvailableRoyaltyPoints(remainingPoints);
//            customerRepository.save(customer);
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Payment Updated Successfully");
//            response.setData(convertToDTO(updatedPayment));
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    public Response getPaymentByBillingId(String clinicId,
//                                          String branchId,
//                                          String billingId) {
//
//        Response response = new Response();
//
//        try {
//
//            Optional<Payment> optional =
//                    repository.findByClinicIdAndBranchIdAndBillingId(
//                            clinicId,
//                            branchId,
//                            billingId);
//
//            if (optional.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Payment Not Found");
//
//                return response;
//            }
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Payment Retrieved Successfully");
//            response.setData(convertToDTO(optional.get()));
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    public Response getAllPayments() {
//
//        Response response = new Response();
//
//        try {
//
//            List<Payment> payments = repository.findAll();
//
//            if (payments.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("No Payments Found");
//
//                return response;
//            }
//
//            // Convert entities to DTOs before returning, consistent with the
//            // overloaded getAllPayments(clinicId, branchId) method below it
//            List<PaymentDTO> dtoList = payments.stream()
//                    .map(this::convertToDTO)
//                    .toList();
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Payments Retrieved Successfully");
//            response.setData(dtoList);
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    public Response getAllPayments(String clinicId,
//                                   String branchId) {
//
//        Response response = new Response();
//
//        try {
//
//            List<Payment> payments =
//                    repository.findByClinicIdAndBranchId(clinicId, branchId);
//
//            if (payments.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("No Payments Found");
//
//                return response;
//            }
//
//            List<PaymentDTO> dtoList = payments.stream()
//                                               .map(this::convertToDTO)
//                                               .toList();
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Payments Retrieved Successfully");
//            response.setData(dtoList);
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    public Response deletePayment(String billingId) {
//
//        Response response = new Response();
//
//        try {
//
//            Optional<Payment> optional = repository.findByBillingId(billingId);
//
//            if (optional.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Payment Not Found");
//
//                return response;
//            }
//
//            repository.delete(optional.get());
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Payment Deleted Successfully");
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    @Override
//    public Response getPaymentByPatientAndBooking(
//            String clinicId,
//            String branchId,
//            String patientId,
//            String bookingId) {
//
//        Response response = new Response();
//
//        try {
//
//            List<Payment> payments =
//                    repository.findByClinicIdAndBranchIdAndPatientIdAndBookingId(
//                            clinicId,
//                            branchId,
//                            patientId,
//                            bookingId);
//
//            if (!payments.isEmpty()) {
//
//                // Pick the most recently created/updated payment if duplicates exist
//                Payment payment = payments.get(payments.size() - 1);
//
//                response.setSuccess(true);
//                response.setStatus(200);
//                response.setMessage("Payment Retrieved Successfully");
//                response.setData(convertToDTO(payment));
//
//                return response;
//            }
//
//            // ==========================
//            // No Payment Yet - Fallback to Treatment Schedule
//            // ==========================
//
//            Optional<TreatmentSchedule> treatmentOptional =
//                    treatmentScheduleRepository
//                            .findByClinicIdAndBranchIdAndBookingIdAndPatientId(
//                                    clinicId,
//                                    branchId,
//                                    bookingId,
//                                    patientId);
//
//            if (treatmentOptional.isEmpty()) {
//
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Payment Not Found");
//
//                return response;
//            }
//
//            TreatmentSchedule treatment = treatmentOptional.get();
//
//            double packageAmount =
//                    treatment.getPackagePrice() == null
//                            ? 0.0
//                            : treatment.getPackagePrice();
//
//            PaymentDTO dto = new PaymentDTO();
//
//            dto.setBillingId(null);
//            dto.setPatientId(treatment.getPatientId());
//            dto.setPatientName(treatment.getPatientName());
//            dto.setMobileNumber(treatment.getMobileNumber());
//            dto.setClinicId(treatment.getClinicId());
//            dto.setBranchId(treatment.getBranchId());
//            dto.setBookingId(treatment.getBookingId());
//            dto.setSittingNo(null);
//
//            dto.setPackageAmount(packageAmount);
//            dto.setDiscountPercentage(0.0);
//            dto.setDiscountAmount(0.0);
//
//            dto.setRoyaltyPointsUsed(0.0);
//            dto.setRoyaltyDeduction(0.0);
//            dto.setEarnedRoyaltyPoints(0.0);
//            dto.setAvailableRoyaltyPoints(0.0);
//            dto.setRemainingRoyaltyPoints(0.0);
//
//            dto.setFinalAmount(packageAmount);
//            dto.setAmountPaying(0.0);
//            dto.setRemainingDue(packageAmount);
//
//            dto.setPaymentMode(null);
//            dto.setTransactionId("");
//            dto.setPaidAt(null);
//
//            dto.setSittings(null);
//            dto.setTransactionHistory(new ArrayList<>());
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Treatment Schedule Retrieved Successfully (No Payment Yet)");
//            response.setData(dto);
//
//        } catch (Exception e) {
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(e.getMessage());
//        }
//
//        return response;
//    }
//
//    /**
//     * Safely parses the loyaltyPoints percentage value coming from AdminService's
//     * clinic response. AdminService may return this field as null, an empty string,
//     * or an otherwise non-numeric value depending on how the clinic record was
//     * created/edited. Any of those cases should be treated as "no loyalty program"
//     * (0%) rather than blowing up the whole payment transaction.
//     *
//     * FIX for: NumberFormatException("empty String") thrown by
//     * Double.parseDouble("") when loyaltyPoints was set to "" in the clinic record.
//     */
//    private double parseLoyaltyPercentage(Object loyaltyPointsRaw) {
//
//        if (loyaltyPointsRaw == null) {
//            return 0.0;
//        }
//
//        String loyalty = loyaltyPointsRaw.toString().trim();
//
//        if (loyalty.isEmpty()) {
//            return 0.0;
//        }
//
//        try {
//            double parsed = Double.parseDouble(loyalty);
//            // Defensive clamp - a loyalty percentage should never be negative.
//            return Math.max(parsed, 0.0);
//        } catch (NumberFormatException e) {
//            // Log-worthy in production (e.g. via a logger) - malformed clinic data.
//            return 0.0;
//        }
//    }
//
//    /**
//     * BILL-65AF90B087AC
//     */
//    private String generateBillingId() {
//
//        String billingId;
//
//        do {
//
//            StringBuilder builder = new StringBuilder("BILL-");
//
//            for (int i = 0; i < 12; i++) {
//
//                builder.append(
//                        CHARACTERS.charAt(
//                                RANDOM.nextInt(CHARACTERS.length())));
//            }
//
//            billingId = builder.toString();
//
//        } while (repository.existsByBillingId(billingId));
//
//        return billingId;
//    }
//
//    private String generateReceiptNumber() {
//
//        String receiptNumber;
//
//        do {
//
//            StringBuilder builder = new StringBuilder("REC-");
//
//            for (int i = 0; i < 10; i++) {
//                builder.append(
//                        CHARACTERS.charAt(
//                                RANDOM.nextInt(CHARACTERS.length())));
//            }
//
//            receiptNumber = builder.toString();
//
//        } while (repository.existsByTransactionHistory_ReceiptNumber(receiptNumber));
//
//        return receiptNumber;
//    }
//
//    public Map<String, Double> retriveInfoBasedOnBookingId(String id) {
//
//        Map<String, Double> res = new LinkedHashMap<>();
//
//        try {
//            Optional<Payment> optional = repository.findByBookingIdIgnoreCase(id);
//
//            if (optional.isPresent()) {
//                res.put("finalAmount", optional.get().getFinalAmount());
//                res.put("paidAmount", optional.get().getAmountPaying());
//                res.put("dueAmount", optional.get().getRemainingDue());
//                res.put("discount", optional.get().getDiscountAmount());
//                res.put("actualAccount", optional.get().getPackageAmount());
//                return res;
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    /**
//     * DTO -> Entity
//     */
//    private Payment convertToEntity(PaymentDTO dto) {
//
//        Payment payment = new Payment();
//
//        // Billing
//        payment.setBillingId(generateBillingId());
//
//        // Patient Details
//        payment.setPatientId(dto.getPatientId());
//        payment.setPatientName(dto.getPatientName());
//        payment.setMobileNumber(dto.getMobileNumber());
//
//        // Clinic Details
//        payment.setClinicId(dto.getClinicId());
//        payment.setBranchId(dto.getBranchId());
//
//        // Booking
//        payment.setBookingId(dto.getBookingId());
//        payment.setSittingNo(dto.getSittingNo());
//
//        // Package
//        payment.setPackageAmount(dto.getPackageAmount());
//
//        // Discount
//        payment.setDiscountPercentage(dto.getDiscountPercentage());
//        payment.setDiscountAmount(dto.getDiscountAmount());
//
//        // Loyalty
//        payment.setAvailableRoyaltyPoints(dto.getAvailableRoyaltyPoints());
//        payment.setRoyaltyPointsUsed(dto.getRoyaltyPointsUsed());
//        payment.setEarnedRoyaltyPoints(dto.getEarnedRoyaltyPoints());
//        payment.setRemainingRoyaltyPoints(dto.getRemainingRoyaltyPoints());
//        payment.setRoyaltyDeduction(dto.getRoyaltyDeduction());
//
//        // Payment
//        payment.setFinalAmount(dto.getFinalAmount());
//        payment.setAmountPaying(dto.getAmountPaying());
//        payment.setRemainingDue(dto.getRemainingDue());
//
//        payment.setPaymentMode(dto.getPaymentMode());
//        payment.setTransactionId(dto.getTransactionId());
//        payment.setPaidAt(dto.getPaidAt());
//
//        payment.setSittings(dto.getSittings());
//
//        if (dto.getTransactionHistory() != null) {
//            payment.setTransactionHistory(dto.getTransactionHistory());
//        } else {
//            payment.setTransactionHistory(new ArrayList<>());
//        }
//
//        return payment;
//    }
//
//    /**
//     * Entity -> DTO
//     */
//    private PaymentDTO convertToDTO(Payment payment) {
//
//        PaymentDTO dto = new PaymentDTO();
//
//        dto.setBillingId(payment.getBillingId());
//
//        // Patient Details
//        dto.setPatientId(payment.getPatientId());
//        dto.setPatientName(payment.getPatientName());
//        dto.setMobileNumber(payment.getMobileNumber());
//
//        // Clinic Details
//        dto.setClinicId(payment.getClinicId());
//        dto.setBranchId(payment.getBranchId());
//
//        // Booking Details
//        dto.setBookingId(payment.getBookingId());
//
//        // Sitting Details
//        dto.setSittingNo(payment.getSittingNo());
//
//        // Package Details
//        dto.setPackageAmount(payment.getPackageAmount());
//
//        // Discount
//        dto.setDiscountPercentage(payment.getDiscountPercentage());
//        dto.setDiscountAmount(payment.getDiscountAmount());
//
//        // Loyalty
//        dto.setAvailableRoyaltyPoints(payment.getAvailableRoyaltyPoints());
//        dto.setRoyaltyPointsUsed(payment.getRoyaltyPointsUsed());
//        dto.setEarnedRoyaltyPoints(payment.getEarnedRoyaltyPoints());
//        dto.setRemainingRoyaltyPoints(payment.getRemainingRoyaltyPoints());
//        dto.setRoyaltyDeduction(payment.getRoyaltyDeduction());
//
//        // Payment Details
//        dto.setFinalAmount(payment.getFinalAmount());
//        dto.setAmountPaying(payment.getAmountPaying());
//        dto.setRemainingDue(payment.getRemainingDue());
//
//        dto.setPaymentMode(payment.getPaymentMode());
//        dto.setTransactionId(payment.getTransactionId());
//        dto.setPaidAt(payment.getPaidAt());
//
//        // Lists
//        dto.setSittings(payment.getSittings());
//        dto.setTransactionHistory(payment.getTransactionHistory());
//
//        return dto;
//    }
//
//    @Override
//    public Response getTreatmentScheduleWithPayment(
//            String clinicId,
//            String branchId,
//            String bookingId,
//            String patientId) {
//
//        Response response = new Response();
//
//        try {
//
//            Optional<TreatmentSchedule> optional =
//                    treatmentScheduleRepository
//                            .findByClinicIdAndBranchIdAndBookingIdAndPatientId(
//                                    clinicId,
//                                    branchId,
//                                    bookingId,
//                                    patientId);
//
//            if (optional.isEmpty()) {
//                response.setSuccess(false);
//                response.setStatus(404);
//                response.setMessage("Treatment Schedule Not Found");
//                return response;
//            }
//
//            TreatmentSchedule treatmentSchedule = optional.get();
//
//            // Payment
//            List<Payment> paymentList =
//                    repository.findByClinicIdAndBranchIdAndPatientIdAndBookingId(
//                            clinicId,
//                            branchId,
//                            patientId,
//                            bookingId);
//
//            Payment payment =
//                    paymentList.isEmpty()
//                            ? null
//                            : paymentList.get(paymentList.size() - 1);
//
//            Map<String, Object> result = new LinkedHashMap<>();
//            List<Map<String, Object>> sittingList = new ArrayList<>();
//
//            double finalAmount = 0.0;
//            double totalPaid = 0.0;
//            double remainingDue = 0.0;
//
//            int totalSittings = treatmentSchedule.getSittings().size();
//
//            if (payment != null) {
//
//                finalAmount =
//                        payment.getFinalAmount() == null
//                                ? 0.0
//                                : payment.getFinalAmount();
//
//                totalPaid =
//                        payment.getAmountPaying() == null
//                                ? 0.0
//                                : payment.getAmountPaying();
//
//                remainingDue = finalAmount - totalPaid;
//
//                if (remainingDue < 0) {
//                    remainingDue = 0.0;
//                }
//
//                result.put("clinicId", payment.getClinicId());
//                result.put("branchId", payment.getBranchId());
//                result.put("bookingId", payment.getBookingId());
//                result.put("patientId", payment.getPatientId());
//                result.put("patientName", payment.getPatientName());
//                result.put("mobileNumber", payment.getMobileNumber());
//                result.put("finalAmount", finalAmount);
//                result.put("amountPaid", totalPaid);
//                result.put("remainingDue", remainingDue);
//
//            } else {
//
//                finalAmount =
//                        treatmentSchedule.getPackagePrice() == null
//                                ? 0.0
//                                : treatmentSchedule.getPackagePrice();
//
//                totalPaid = 0.0;
//                remainingDue = finalAmount;
//
//                result.put("clinicId", clinicId);
//                result.put("branchId", branchId);
//                result.put("bookingId", bookingId);
//                result.put("patientId", patientId);
//                result.put("patientName", treatmentSchedule.getPatientName());
//                result.put("mobileNumber", treatmentSchedule.getMobileNumber());
//                result.put("finalAmount", finalAmount);
//                result.put("amountPaid", totalPaid);
//                result.put("remainingDue", remainingDue);
//            }
//
//            double perSittingAmount =
//                    totalSittings > 0
//                            ? finalAmount / totalSittings
//                            : 0.0;
//
//            double remainingPaid = totalPaid;
//
//            for (Sittings treatmentSitting : treatmentSchedule.getSittings()) {
//
//                Map<String, Object> sittingMap = new LinkedHashMap<>();
//
//                String paymentStatus;
//                double amountPaidForSitting = 0.0;
//                double remainingForSitting = perSittingAmount;
//
//                if (perSittingAmount <= 0.0) {
//
//                    paymentStatus = "Pending";
//                    amountPaidForSitting = 0.0;
//                    remainingForSitting = perSittingAmount;
//
//                } else if (remainingPaid >= perSittingAmount) {
//
//                    paymentStatus = "Paid";
//                    amountPaidForSitting = perSittingAmount;
//                    remainingForSitting = 0.0;
//
//                    remainingPaid -= perSittingAmount;
//
//                } else if (remainingPaid > 0) {
//
//                    paymentStatus = "Partially Paid";
//                    amountPaidForSitting = remainingPaid;
//                    remainingForSitting = perSittingAmount - remainingPaid;
//
//                    remainingPaid = 0;
//
//                } else {
//
//                    paymentStatus = "Pending";
//                }
//
//                // doctorName comes ONLY from the sitting itself - SOAP note fallback removed
//                String doctorName = treatmentSitting.getDoctorName();
//                if (doctorName == null || doctorName.isBlank()) {
//                    doctorName = "N/A";
//                }
//
//                // date comes ONLY from the sitting itself - SOAP note fallback removed
//                String date = treatmentSitting.getDate();
//                if (date == null || date.isBlank()) {
//                    date = "N/A";
//                }
//
//                String doctorId = treatmentSitting.getDoctorId();
//                if (doctorId == null || doctorId.isBlank()) {
//                    doctorId = "N/A";
//                }
//
//                String slot = treatmentSitting.getSlot();
//                if (slot == null || slot.isBlank()) {
//                    slot = "N/A";
//                }
//
//                sittingMap.put("sittingsId", treatmentSitting.getSittingsId());
//                sittingMap.put("sittingNumber", treatmentSitting.getSittingNumber());
//                sittingMap.put("doctorId", doctorId);
//                sittingMap.put("doctorName", doctorName);
//                sittingMap.put("date", date);
//                sittingMap.put("slot", slot);
//
//                sittingMap.put("perSittingAmount", perSittingAmount);
//                sittingMap.put("amountPaid", amountPaidForSitting);
//                sittingMap.put("remainingAmount", remainingForSitting);
//                sittingMap.put("paymentStatus", paymentStatus);
//
//                sittingList.add(sittingMap);
//            }
//
//            result.put("sittings", sittingList);
//
//            response.setSuccess(true);
//            response.setStatus(200);
//            response.setMessage("Treatment Schedule Retrieved Successfully");
//            response.setData(result);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            response.setSuccess(false);
//            response.setStatus(500);
//            response.setMessage(
//                    "Failed to retrieve Treatment Schedule : " + e.getMessage());
//        }
//
//        return response;
//    }
//    
//    /**
//     * True once editing should be locked from the frontend:
//     *  - any amount has actually been paid (single sitting OR package), OR
//     *  - any sitting numbered 2+ has been fully booked (doctorId, doctorName,
//     *    date, and slot all present). Sitting #1 is skipped on purpose since it
//     *    defaults to the SOAP note's session start date/slot and doesn't
//     *    reflect a deliberate booking action by itself.
//     */
//    private boolean computeEditingDisabledFlag(double amountPaidSoFar, List<Sittings> scheduleSittings) {
//        if (amountPaidSoFar > 0) {
//            return true;
//        }
//        if (scheduleSittings == null) {
//            return false;
//        }
//        for (Sittings s : scheduleSittings) {
//            Integer num = s.getSittingNumber();
//            if (num == null || num == 1) {
//                continue; // skip sitting #1
//            }
//            boolean doctorIdSet = s.getDoctorId() != null && !s.getDoctorId().isBlank();
//            boolean doctorNameSet = s.getDoctorName() != null && !s.getDoctorName().isBlank();
//            boolean dateSet = s.getDate() != null && !s.getDate().isBlank();
//            boolean slotSet = s.getSlot() != null && !s.getSlot().isBlank();
//            if (doctorIdSet && doctorNameSet && dateSet && slotSet) {
//                return true;
//            }
//        }
//        return false;
//    }  
//}