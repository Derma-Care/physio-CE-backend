package com.clinicadmin.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.DashboardTotalsDTO;
import com.clinicadmin.dto.PatientRevenueDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.RevenueAnalyticsResponseDTO;
import com.clinicadmin.dto.RevenueSummaryDTO;
import com.clinicadmin.dto.RevenueTrendPointDTO;
import com.clinicadmin.entity.Payment;
import com.clinicadmin.entity.TransactionHistory;
import com.clinicadmin.repository.PaymentRepository;
import com.clinicadmin.service.RevenueAnalyticsService;

@Service
public class RevenueAnalyticsServiceImpl implements RevenueAnalyticsService {

	@Autowired
	private PaymentRepository paymentRepository;

	private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter YEAR_FMT = DateTimeFormatter.ofPattern("yyyy");

	private enum Granularity {
		DAY, MONTH, YEAR
	}

	// ==========================================================
	// PUBLIC API #1 - predefined filters (Today / Week / Month / Year / Overall)
	// ==========================================================

	@Override
	public Response getRevenueAnalytics(String clinicId, String branchId, Integer filterType) {

		Response validation = validateClinicAndBranch(clinicId, branchId);
		if (validation != null) {
			return validation;
		}

		LocalDate today = LocalDate.now(ZONE);
		int type = filterType == null ? 5 : filterType;

		LocalDate rangeStart;
		LocalDate rangeEnd;
		String filterLabel;
		boolean overall = false;

		switch (type) {

		case 1: // Today
			rangeStart = today;
			rangeEnd = today;
			filterLabel = "Today";
			break;

		case 2: // This Week (Monday - Sunday, current week up to today)
			rangeStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			rangeEnd = today;
			filterLabel = "This Week";
			break;

		case 3: // This Month
			rangeStart = today.withDayOfMonth(1);
			rangeEnd = today;
			filterLabel = "This Month";
			break;

		case 4: // This Year
			rangeStart = today.withDayOfYear(1);
			rangeEnd = today;
			filterLabel = "This Year";
			break;

		case 5: // Overall
		default:
			rangeStart = null;
			rangeEnd = null;
			overall = true;
			filterLabel = "Overall";
			break;
		}

		try {
			return buildAnalytics(clinicId, branchId, rangeStart, rangeEnd, overall, filterLabel);
		} catch (Exception e) {
			return errorResponse("Failed to retrieve revenue analytics: " + e.getMessage());
		}
	}

	// ==========================================================
	// PUBLIC API #2 - dedicated custom date range
	// ==========================================================

	@Override
	public Response getRevenueAnalyticsCustomRange(String clinicId, String branchId, String startDate, String endDate) {

		Response validation = validateClinicAndBranch(clinicId, branchId);
		if (validation != null) {
			return validation;
		}

		if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
			return badRequest("startDate and endDate are both required (format yyyy-MM-dd)");
		}

		LocalDate rangeStart;
		LocalDate rangeEnd;

		try {
			rangeStart = LocalDate.parse(startDate.trim());
			rangeEnd = LocalDate.parse(endDate.trim());
		} catch (Exception e) {
			return badRequest("startDate/endDate must be in yyyy-MM-dd format");
		}

		if (rangeEnd.isBefore(rangeStart)) {
			return badRequest("endDate cannot be before startDate");
		}

		String filterLabel = "Custom (" + rangeStart + " to " + rangeEnd + ")";

		try {
			return buildAnalytics(clinicId, branchId, rangeStart, rangeEnd, false, filterLabel);
		} catch (Exception e) {
			return errorResponse("Failed to retrieve revenue analytics: " + e.getMessage());
		}
	}

	// ==========================================================
	// PUBLIC API #3 - dashboard quick totals (Today/Week/Month/Year at once)
	// ==========================================================

	@Override
	public Response getDashboardTotals(String clinicId, String branchId) {

		Response validation = validateClinicAndBranch(clinicId, branchId);
		if (validation != null) {
			return validation;
		}

		try {

			LocalDate today = LocalDate.now(ZONE);
			LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			LocalDate monthStart = today.withDayOfMonth(1);
			LocalDate yearStart = today.withDayOfYear(1);

			List<Payment> allPayments = paymentRepository.findByClinicIdAndBranchId(clinicId, branchId);

			double todayPaid = 0.0;
			double weekPaid = 0.0;
			double monthPaid = 0.0;
			double yearPaid = 0.0;
			double overallPaid = 0.0;

			for (Payment p : allPayments) {

				if (p.getTransactionHistory() == null) {
					continue;
				}

				for (TransactionHistory th : p.getTransactionHistory()) {

					if (th.getDate() == null) {
						continue;
					}

					LocalDate txDate = th.getDate().toLocalDate();
					double amount = nz(th.getAmount());

					overallPaid += amount;

					if (isWithinRange(txDate, yearStart, today)) {
						yearPaid += amount;
					}
					if (isWithinRange(txDate, monthStart, today)) {
						monthPaid += amount;
					}
					if (isWithinRange(txDate, weekStart, today)) {
						weekPaid += amount;
					}
					if (txDate.isEqual(today)) {
						todayPaid += amount;
					}
				}
			}

			DashboardTotalsDTO dto = new DashboardTotalsDTO(round2(todayPaid), round2(weekPaid), round2(monthPaid),
					round2(yearPaid), round2(overallPaid));

			Response response = new Response();
			response.setSuccess(true);
			response.setStatus(200);
			response.setMessage("Dashboard totals retrieved successfully");
			response.setData(dto);
			return response;

		} catch (Exception e) {
			return errorResponse("Failed to retrieve dashboard totals: " + e.getMessage());
		}
	}

	// ==========================================================
	// Shared core - both public methods funnel into this once their
	// date range (or "overall") has been resolved and validated.
	// ==========================================================

	private Response buildAnalytics(String clinicId, String branchId, LocalDate rangeStart, LocalDate rangeEnd,
			boolean overall, String filterLabel) {

		Response response = new Response();

		// ==========================
		// Fetch all bills for this clinic/branch, then filter by bill-level
		// activity (paidAt) for the summary cards + patient table.
		// ==========================

		List<Payment> allPayments = paymentRepository.findByClinicIdAndBranchId(clinicId, branchId);

		List<Payment> matchedPayments = allPayments.stream()
				.filter(p -> overall || isWithinRange(p.getPaidAt(), rangeStart, rangeEnd)).toList();

		// ==========================
		// Summary Cards
		// ==========================

		double totalRevenue = 0.0;
		double totalPaid = 0.0;
		double totalDue = 0.0;
		double totalDiscount = 0.0;

		for (Payment p : matchedPayments) {
			totalRevenue += nz(p.getFinalAmount());
			totalPaid += nz(p.getAmountPaying());
			totalDue += nz(p.getRemainingDue());
			// NOTE: payment.getDiscountAmount() is NOT reliable here - it gets
			// overwritten on every updatePayment call with only that call's
			// discount, so it does not reflect discount given on earlier
			// installments (e.g. discount applied on installment 1 but not
			// repeated on installment 2 leaves the bill-level field at 0
			// even though real discount was given). Sum from the actual
			// transaction history instead, which records what was applied
			// at the time of each installment.
			totalDiscount += sumDiscountFromHistory(p);
		}

		RevenueSummaryDTO summary = new RevenueSummaryDTO(round2(totalRevenue), round2(totalPaid), round2(totalDue),
				round2(totalDiscount), (long) matchedPayments.size());

		// ==========================
		// Patient Revenue Analytics table
		// ==========================

		Map<String, PatientRevenueDTO> patientMap = new LinkedHashMap<>();

		for (Payment p : matchedPayments) {

			String patientId = p.getPatientId() == null ? "UNKNOWN" : p.getPatientId();

			PatientRevenueDTO row = patientMap.computeIfAbsent(patientId, id -> {
				PatientRevenueDTO dto = new PatientRevenueDTO();
				dto.setPatientId(id);
				dto.setPatientName(p.getPatientName());
				dto.setTotalBillAmount(0.0);
				dto.setPaidAmount(0.0);
				dto.setDueAmount(0.0);
				dto.setNumberOfVisits(0);
				dto.setLastPaymentDate(null);
				dto.setLastPaidAmount(0.0);
				return dto;
			});

			row.setTotalBillAmount(round2(row.getTotalBillAmount() + nz(p.getFinalAmount())));
			row.setPaidAmount(round2(row.getPaidAmount() + nz(p.getAmountPaying())));
			row.setDueAmount(round2(Math.max(0.0, row.getTotalBillAmount() - row.getPaidAmount())));
			row.setNumberOfVisits(row.getNumberOfVisits() + 1);

			// Find this bill's own most recent transaction (date + amount actually
			// paid in that single transaction, not the bill's cumulative total),
			// then keep it only if it's newer than what we've already recorded
			// for this patient across their other bills.
			LocalDateTime latestTxDate = null;
			double latestTxAmount = 0.0;

			if (p.getTransactionHistory() != null) {
				for (TransactionHistory th : p.getTransactionHistory()) {
					if (th.getDate() != null && (latestTxDate == null || th.getDate().isAfter(latestTxDate))) {
						latestTxDate = th.getDate();
						latestTxAmount = nz(th.getAmount());
					}
				}
			}

			// Fallback for a bill with no transaction history entries recorded
			if (latestTxDate == null && p.getPaidAt() != null) {
				latestTxDate = p.getPaidAt();
				latestTxAmount = nz(p.getAmountPaying());
			}

			if (latestTxDate != null
					&& (row.getLastPaymentDate() == null || latestTxDate.isAfter(row.getLastPaymentDate()))) {
				row.setLastPaymentDate(latestTxDate);
				row.setLastPaidAmount(round2(latestTxAmount));
			}
		}

		List<PatientRevenueDTO> patientRevenueList = new ArrayList<>(patientMap.values());
		patientRevenueList.sort(Comparator.comparing(PatientRevenueDTO::getLastPaymentDate,
				Comparator.nullsLast(Comparator.reverseOrder())));

		// ==========================
		// Revenue Trend chart - built from individual transaction history
		// entries (not bill snapshots), so each installment shows up on its
		// own actual payment date regardless of which bill it belongs to.
		// ==========================

		Granularity granularity = resolveGranularity(overall, rangeStart, rangeEnd);

		// TreeMap keeps buckets sorted chronologically by their label
		Map<String, double[]> buckets = new TreeMap<>();

		for (Payment p : allPayments) {

			if (p.getTransactionHistory() == null) {
				continue;
			}

			for (TransactionHistory th : p.getTransactionHistory()) {

				if (th.getDate() == null) {
					continue;
				}

				LocalDate txDate = th.getDate().toLocalDate();

				if (!overall && !isWithinRange(txDate, rangeStart, rangeEnd)) {
					continue;
				}

				String bucketLabel = bucketLabel(txDate, granularity);

				double[] bucket = buckets.computeIfAbsent(bucketLabel, k -> new double[2]);
				bucket[0] += nz(th.getAmount()); // revenue collected in this bucket
				bucket[1] += nz(th.getDiscount()); // discount applied in this bucket
			}
		}

		List<RevenueTrendPointDTO> revenueTrend = new ArrayList<>();
		for (Map.Entry<String, double[]> entry : buckets.entrySet()) {
			revenueTrend.add(
					new RevenueTrendPointDTO(entry.getKey(), round2(entry.getValue()[0]), round2(entry.getValue()[1])));
		}

		// ==========================
		// Today's Paid Amount - always "today" regardless of which filter/range
		// was requested, computed from actual transaction dates (not bill
		// snapshots), same logic as the dashboard-totals endpoint.
		// ==========================

		LocalDate today = LocalDate.now(ZONE);
		double todayPaid = 0.0;

		for (Payment p : allPayments) {
			if (p.getTransactionHistory() == null) {
				continue;
			}
			for (TransactionHistory th : p.getTransactionHistory()) {
				if (th.getDate() != null && th.getDate().toLocalDate().isEqual(today)) {
					todayPaid += nz(th.getAmount());
				}
			}
		}

		// ==========================
		// Assemble response
		// ==========================

		RevenueAnalyticsResponseDTO result = new RevenueAnalyticsResponseDTO();
		result.setFilterApplied(filterLabel);
		result.setRangeStart(overall ? null : rangeStart.format(DAY_FMT));
		result.setRangeEnd(overall ? null : rangeEnd.format(DAY_FMT));
		result.setSummary(summary);
		result.setTodayPaidAmount(round2(todayPaid));
		result.setPatientRevenueList(patientRevenueList);
		result.setRevenueTrend(revenueTrend);
		result.setPaidVsDuePaid(summary.getTotalPaidAmount());
		result.setPaidVsDueDue(summary.getTotalDueAmount());

		response.setSuccess(true);
		response.setStatus(200);
		response.setMessage("Revenue analytics retrieved successfully");
		response.setData(result);

		return response;
	}

	// ==========================
	// Helpers
	// ==========================

	private Response validateClinicAndBranch(String clinicId, String branchId) {
		if (clinicId == null || clinicId.isBlank() || branchId == null || branchId.isBlank()) {
			return badRequest("clinicId and branchId are required");
		}
		return null;
	}

	private Response badRequest(String message) {
		Response response = new Response();
		response.setSuccess(false);
		response.setStatus(400);
		response.setMessage(message);
		return response;
	}

	private Response errorResponse(String message) {
		Response response = new Response();
		response.setSuccess(false);
		response.setStatus(500);
		response.setMessage(message);
		return response;
	}

	private boolean isWithinRange(LocalDateTime dateTime, LocalDate start, LocalDate end) {
		if (dateTime == null || start == null || end == null) {
			return false;
		}
		return isWithinRange(dateTime.toLocalDate(), start, end);
	}

	private boolean isWithinRange(LocalDate date, LocalDate start, LocalDate end) {
		if (date == null || start == null || end == null) {
			return false;
		}
		return !date.isBefore(start) && !date.isAfter(end);
	}

	private Granularity resolveGranularity(boolean overall, LocalDate start, LocalDate end) {

		if (overall) {
			return Granularity.MONTH;
		}

		long days = ChronoUnit.DAYS.between(start, end) + 1;

		if (days <= 31) {
			return Granularity.DAY;
		} else if (days <= 731) {
			return Granularity.MONTH;
		} else {
			return Granularity.YEAR;
		}
	}

	private String bucketLabel(LocalDate date, Granularity granularity) {
		return switch (granularity) {
		case DAY -> date.format(DAY_FMT);
		case MONTH -> date.format(MONTH_FMT);
		case YEAR -> date.format(YEAR_FMT);
		};
	}

	private double sumDiscountFromHistory(Payment p) {
		if (p.getTransactionHistory() == null) {
			return 0.0;
		}
		double total = 0.0;
		for (TransactionHistory th : p.getTransactionHistory()) {
			total += nz(th.getDiscount());
		}
		return total;
	}

	private double nz(Double value) {
		return value == null ? 0.0 : value;
	}

	private double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}