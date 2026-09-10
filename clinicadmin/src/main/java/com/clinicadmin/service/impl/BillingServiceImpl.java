package com.clinicadmin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.AdditionalDetailsDTO;
import com.clinicadmin.dto.BillingDTO;
import com.clinicadmin.dto.PatientDTO;
import com.clinicadmin.dto.PaymentSummaryDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.dto.ServiceItemDTO;
import com.clinicadmin.dto.TransactionDTO;

import com.clinicadmin.entity.AdditionalDetails;
import com.clinicadmin.entity.Billing;
import com.clinicadmin.entity.Patient;
import com.clinicadmin.entity.PaymentSummary;
import com.clinicadmin.entity.ServiceItem;
import com.clinicadmin.entity.Transaction;

import com.clinicadmin.repository.BillingRepository;
import com.clinicadmin.service.BillingService;

@Service
public class BillingServiceImpl implements BillingService {

    @Autowired
    private BillingRepository billingRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public Response createBilling(BillingDTO billingDTO) {

        Response response = new Response();

        try {

            // ================= VALIDATION =================

            if (billingDTO.getClinicId() == null
                    || billingDTO.getClinicId().isBlank()) {

                response.setSuccess(false);
                response.setData(null);
                response.setMessage("Clinic ID is required");
                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }

            if (billingDTO.getBranchId() == null
                    || billingDTO.getBranchId().isBlank()) {

                response.setSuccess(false);
                response.setData(null);
                response.setMessage("Branch ID is required");
                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }

            if (billingDTO.getPatient() == null) {

                response.setSuccess(false);
                response.setData(null);
                response.setMessage(
                        "Patient details are required");
                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }

            if (billingDTO.getServices() == null
                    || billingDTO.getServices().isEmpty()) {

                response.setSuccess(false);
                response.setData(null);
                response.setMessage(
                        "At least one service is required");
                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }


            // ================= CALCULATE BILL =================

            double subTotal = 0.0;
            double totalDiscount = 0.0;
            double totalTax = 0.0;

            for (ServiceItemDTO service :
                    billingDTO.getServices()) {

                double qty =
                        service.getQty();

                double unitPrice =
                        service.getUnitPrice();

                double discountPercent =
                        service.getDiscountPercent();

                double taxPercent =
                        service.getTaxPercent();


                // Qty * Unit Price
                double serviceSubTotal =
                        qty * unitPrice;


                // Discount
                double discountAmount =
                        serviceSubTotal
                                * discountPercent
                                / 100;


                // Amount after discount
                double amountAfterDiscount =
                        serviceSubTotal
                                - discountAmount;


                // Tax after discount
                double taxAmount =
                        amountAfterDiscount
                                * taxPercent
                                / 100;


                subTotal += serviceSubTotal;

                totalDiscount += discountAmount;

                totalTax += taxAmount;
            }


            // ================= TOTAL =================

            double totalAmount =
                    subTotal
                            - totalDiscount
                            + totalTax;


            // ================= FIRST PAYMENT =================

            double totalPaid = 0.0;

            if (billingDTO.getNewTransaction() != null
                    && billingDTO
                            .getNewTransaction()
                            .getAmount() != null) {

                totalPaid =
                        billingDTO
                                .getNewTransaction()
                                .getAmount();
            }


            if (totalPaid < 0) {

                response.setSuccess(false);
                response.setData(null);

                response.setMessage(
                        "Payment amount cannot be negative");

                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }


            if (totalPaid > totalAmount) {

                response.setSuccess(false);
                response.setData(null);

                response.setMessage(
                        "Payment amount cannot exceed total bill amount");

                response.setStatus(
                        HttpStatus.BAD_REQUEST.value());

                return response;
            }


            // ================= DUE =================

            double dueAmount =
                    totalAmount - totalPaid;


            // ================= PAYMENT SUMMARY =================

            PaymentSummaryDTO summaryDTO =
                    new PaymentSummaryDTO();

            summaryDTO.setSubTotal(
                    subTotal);

            summaryDTO.setTotalDiscount(
                    totalDiscount);

            summaryDTO.setTotalTax(
                    totalTax);

            summaryDTO.setTotalAmount(
                    totalAmount);

            summaryDTO.setTotalPaid(
                    totalPaid);

            summaryDTO.setDueAmount(
                    dueAmount);

            billingDTO.setPaymentSummary(
                    summaryDTO);


            // ================= INVOICE STATUS =================

            if (totalPaid <= 0) {

                billingDTO.setInvoiceStatus(
                        "Unpaid");

            } else if (dueAmount <= 0) {

                billingDTO.setInvoiceStatus(
                        "Paid");

            } else {

                billingDTO.setInvoiceStatus(
                        "Partially Paid");
            }


            // ================= DTO -> ENTITY =================

            Billing billing =
                    convertToEntity(billingDTO);


            // ================= BILLING ID =================

            billing.setBillingId(
                    generateBillingId());


            // ================= TRANSACTION HISTORY =================

            List<Transaction> transactions =
                    new ArrayList<>();

            if (billing.getNewTransaction() != null) {

                transactions.add(
                        billing.getNewTransaction());
            }

            billing.setTransactions(
                    transactions);


            // ================= CREATED / UPDATED =================

            LocalDateTime now =
                    LocalDateTime.now();

            billing.setCreatedAt(now);

            billing.setUpdatedAt(now);


            // ================= SAVE =================

            Billing savedBilling =
                    billingRepository.save(
                            billing);


            // ================= RESPONSE =================

            BillingDTO responseDTO =
                    convertToDTO(
                            savedBilling);

            response.setSuccess(true);

            response.setData(
                    responseDTO);

            response.setMessage(
                    "Billing created successfully");

            response.setStatus(
                    HttpStatus.CREATED.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to create billing: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // GET BY BILLING ID
    // =========================================================

    @Override
    public Response getBillingById(
            String billingId) {

        Response response = new Response();

        try {

            Billing billing =
                    billingRepository
                            .findById(billingId)
                            .orElse(null);

            if (billing == null) {

                response.setSuccess(false);
                response.setData(null);

                response.setMessage(
                        "Billing not found");

                response.setStatus(
                        HttpStatus.NOT_FOUND.value());

                return response;
            }


            BillingDTO billingDTO =
                    convertToDTO(
                            billing);


            response.setSuccess(true);

            response.setData(
                    billingDTO);

            response.setMessage(
                    "Billing fetched successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to fetch billing: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // GET ALL BY CLINIC ID AND BRANCH ID
    // =========================================================

    @Override
    public Response getAllBillings(
            String clinicId,
            String branchId) {

        Response response = new Response();

        try {

            List<Billing> billings =
                    billingRepository
                            .findByClinicIdAndBranchId(
                                    clinicId,
                                    branchId);


            List<BillingDTO> billingDTOs =
                    billings.stream()
                            .map(this::convertToDTO)
                            .collect(
                                    Collectors.toList());


            response.setSuccess(true);

            response.setData(
                    billingDTOs);

            response.setMessage(
                    "Billings fetched successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to fetch billings: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public Response updateBilling(
            String billingId,
            BillingDTO billingDTO) {

        Response response = new Response();

        try {

            Billing existingBilling =
                    billingRepository
                            .findById(billingId)
                            .orElse(null);


            if (existingBilling == null) {

                response.setSuccess(false);

                response.setData(null);

                response.setMessage(
                        "Billing not found");

                response.setStatus(
                        HttpStatus.NOT_FOUND.value());

                return response;
            }


            // ================= BASIC DETAILS =================

            if (billingDTO.getClinicId() != null) {

                existingBilling.setClinicId(
                        billingDTO.getClinicId());
            }


            if (billingDTO.getBranchId() != null) {

                existingBilling.setBranchId(
                        billingDTO.getBranchId());
            }


            if (billingDTO.getDoctorId() != null) {

                existingBilling.setDoctorId(
                        billingDTO.getDoctorId());
            }


            if (billingDTO.getVisitType() != null) {

                existingBilling.setVisitType(
                        billingDTO.getVisitType());
            }


            if (billingDTO.getBillDate() != null) {

                existingBilling.setBillDate(
                        billingDTO.getBillDate());
            }


            if (billingDTO.getInvoiceDate() != null) {

                existingBilling.setInvoiceDate(
                        billingDTO.getInvoiceDate());
            }


            // ================= PATIENT =================

            if (billingDTO.getPatient() != null) {

                Patient patient =
                        existingBilling.getPatient();

                if (patient == null) {

                    patient =
                            new Patient();
                }


                if (billingDTO
                        .getPatient()
                        .getPatientId() != null) {

                    patient.setPatientId(
                            billingDTO
                                    .getPatient()
                                    .getPatientId());
                }


                if (billingDTO
                        .getPatient()
                        .getPatientName() != null) {

                    patient.setPatientName(
                            billingDTO
                                    .getPatient()
                                    .getPatientName());
                }


                if (billingDTO
                        .getPatient()
                        .getMobileNumber() != null) {

                    patient.setMobileNumber(
                            billingDTO
                                    .getPatient()
                                    .getMobileNumber());
                }


                if (billingDTO
                        .getPatient()
                        .getAge() != null) {

                    patient.setAge(
                            billingDTO
                                    .getPatient()
                                    .getAge());
                }


                if (billingDTO
                        .getPatient()
                        .getGender() != null) {

                    patient.setGender(
                            billingDTO
                                    .getPatient()
                                    .getGender());
                }


                existingBilling.setPatient(
                        patient);
            }


            // ================= SERVICES =================

            if (billingDTO.getServices() != null) {

                List<ServiceItem> services =
                        billingDTO
                                .getServices()
                                .stream()
                                .map(serviceDTO -> {

                                    ServiceItem service =
                                            new ServiceItem();

                                    service.setServiceId(
                                            serviceDTO
                                                    .getServiceId());

                                    service.setServiceName(
                                            serviceDTO
                                                    .getServiceName());

                                    service.setQty(
                                            serviceDTO
                                                    .getQty());

                                    service.setUnitPrice(
                                            serviceDTO
                                                    .getUnitPrice());

                                    service.setDiscountPercent(
                                            serviceDTO
                                                    .getDiscountPercent());

                                    service.setTaxPercent(
                                            serviceDTO
                                                    .getTaxPercent());

                                    return service;

                                })
                                .collect(
                                        Collectors.toList());


                existingBilling.setServices(
                        services);
            }


            // =================================================
            // NEW TRANSACTION
            // =================================================

            if (billingDTO.getNewTransaction() != null) {

                TransactionDTO transactionDTO =
                        billingDTO.getNewTransaction();


                Double newPaymentAmount =
                        transactionDTO.getAmount();


                // ================= VALIDATE =================

                if (newPaymentAmount == null
                        || newPaymentAmount <= 0) {

                    response.setSuccess(false);

                    response.setData(null);

                    response.setMessage(
                            "Payment amount must be greater than zero");

                    response.setStatus(
                            HttpStatus.BAD_REQUEST.value());

                    return response;
                }


                // ================= GET SUMMARY =================

                PaymentSummary summary =
                        existingBilling
                                .getPaymentSummary();


                if (summary == null) {

                    response.setSuccess(false);

                    response.setData(null);

                    response.setMessage(
                            "Payment summary not found");

                    response.setStatus(
                            HttpStatus.BAD_REQUEST.value());

                    return response;
                }


                double totalAmount =
                        summary.getTotalAmount() != null
                                ? summary.getTotalAmount()
                                : 0.0;


                double oldTotalPaid =
                        summary.getTotalPaid() != null
                                ? summary.getTotalPaid()
                                : 0.0;


                double currentDueAmount =
                        totalAmount
                                - oldTotalPaid;


                // ================= VALIDATE DUE =================

                if (newPaymentAmount >
                        currentDueAmount) {

                    response.setSuccess(false);

                    response.setData(null);

                    response.setMessage(
                            "Payment amount cannot exceed due amount");

                    response.setStatus(
                            HttpStatus.BAD_REQUEST.value());

                    return response;
                }


                // ================= CREATE TRANSACTION =================

                Transaction newTransaction =
                        new Transaction();


                newTransaction.setReceiptNo(
                        transactionDTO
                                .getReceiptNo());


                newTransaction.setPaymentDate(
                        transactionDTO
                                .getPaymentDate());


                newTransaction.setPaymentMode(
                        transactionDTO
                                .getPaymentMode());


                newTransaction.setTransactionId(
                        transactionDTO
                                .getTransactionId());


                newTransaction.setAmount(
                        transactionDTO
                                .getAmount());


                newTransaction.setRemarks(
                        transactionDTO
                                .getRemarks());


                // Latest transaction
                existingBilling.setNewTransaction(
                        newTransaction);


                // ================= TRANSACTION HISTORY =================

                if (existingBilling
                        .getTransactions() == null) {

                    existingBilling.setTransactions(
                            new ArrayList<>());
                }


                existingBilling
                        .getTransactions()
                        .add(
                                newTransaction);


                // ================= UPDATE SUMMARY =================

                double updatedTotalPaid =
                        oldTotalPaid
                                + newPaymentAmount;


                double updatedDueAmount =
                        totalAmount
                                - updatedTotalPaid;


                summary.setTotalPaid(
                        updatedTotalPaid);


                summary.setDueAmount(
                        updatedDueAmount);


                existingBilling.setPaymentSummary(
                        summary);


                // ================= STATUS =================

                if (updatedTotalPaid <= 0) {

                    existingBilling.setInvoiceStatus(
                            "Unpaid");

                } else if (updatedDueAmount <= 0) {

                    existingBilling.setInvoiceStatus(
                            "Paid");

                } else {

                    existingBilling.setInvoiceStatus(
                            "Partially Paid");
                }
            }


            // ================= ADDITIONAL DETAILS =================

            if (billingDTO
                    .getAdditionalDetails() != null) {

                AdditionalDetails additionalDetails =
                        existingBilling
                                .getAdditionalDetails();


                if (additionalDetails == null) {

                    additionalDetails =
                            new AdditionalDetails();
                }


                if (billingDTO
                        .getAdditionalDetails()
                        .getBillingStaff() != null) {

                    additionalDetails.setBillingStaff(

                            billingDTO
                                    .getAdditionalDetails()
                                    .getBillingStaff());
                }


                if (billingDTO
                        .getAdditionalDetails()
                        .getNotes() != null) {

                    additionalDetails.setNotes(

                            billingDTO
                                    .getAdditionalDetails()
                                    .getNotes());
                }


                if (billingDTO
                        .getAdditionalDetails()
                        .getInternalComments() != null) {

                    additionalDetails.setInternalComments(

                            billingDTO
                                    .getAdditionalDetails()
                                    .getInternalComments());
                }


                existingBilling.setAdditionalDetails(
                        additionalDetails);
            }


            // ================= UPDATE TIME =================

            existingBilling.setUpdatedAt(
                    LocalDateTime.now());


            // ================= SAVE =================

            Billing savedBilling =
                    billingRepository.save(
                            existingBilling);


            // ================= RESPONSE =================

            BillingDTO responseDTO =
                    convertToDTO(
                            savedBilling);


            response.setSuccess(true);

            response.setData(
                    responseDTO);

            response.setMessage(
                    "Billing updated successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to update billing: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // GET ALL BY CLINIC ID
    // =========================================================

    @Override
    public Response getAllBillingsByClinicId(
            String clinicId) {

        Response response = new Response();

        try {

            List<Billing> billings =
                    billingRepository
                            .findByClinicId(
                                    clinicId);


            List<BillingDTO> billingDTOs =
                    billings.stream()
                            .map(this::convertToDTO)
                            .collect(
                                    Collectors.toList());


            response.setSuccess(true);

            response.setData(
                    billingDTOs);

            response.setMessage(
                    "Billings fetched successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to fetch billings: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // GET ALL BILLINGS
    // =========================================================

    @Override
    public Response getAllBillings() {

        Response response = new Response();

        try {

            List<Billing> billings =
                    billingRepository.findAll();


            List<BillingDTO> billingDTOs =
                    billings.stream()
                            .map(this::convertToDTO)
                            .collect(
                                    Collectors.toList());


            response.setSuccess(true);

            response.setData(
                    billingDTOs);

            response.setMessage(
                    "All billings fetched successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to fetch billings: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public Response deleteBilling(
            String billingId) {

        Response response = new Response();

        try {

            Billing billing =
                    billingRepository
                            .findById(billingId)
                            .orElse(null);


            if (billing == null) {

                response.setSuccess(false);

                response.setData(null);

                response.setMessage(
                        "Billing not found");

                response.setStatus(
                        HttpStatus.NOT_FOUND.value());

                return response;
            }


            billingRepository.delete(
                    billing);


            response.setSuccess(true);

            response.setData(null);

            response.setMessage(
                    "Billing deleted successfully");

            response.setStatus(
                    HttpStatus.OK.value());


        } catch (Exception e) {

            response.setSuccess(false);

            response.setData(null);

            response.setMessage(
                    "Failed to delete billing: "
                            + e.getMessage());

            response.setStatus(
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }


    // =========================================================
    // DTO -> ENTITY
    // =========================================================

    private Billing convertToEntity(
            BillingDTO dto) {

        if (dto == null) {

            return null;
        }


        Billing billing =
                new Billing();


        billing.setBillingId(
                dto.getBillingId());


        billing.setClinicId(
                dto.getClinicId());


        billing.setBranchId(
                dto.getBranchId());


        billing.setDoctorId(
                dto.getDoctorId());


        billing.setVisitType(
                dto.getVisitType());


        billing.setBillDate(
                dto.getBillDate());


        billing.setInvoiceDate(
                dto.getInvoiceDate());


        billing.setInvoiceStatus(
                dto.getInvoiceStatus());


        // ================= PATIENT =================

        if (dto.getPatient() != null) {

            Patient patient =
                    new Patient();


            patient.setPatientId(
                    dto.getPatient()
                            .getPatientId());


            patient.setPatientName(
                    dto.getPatient()
                            .getPatientName());


            patient.setMobileNumber(
                    dto.getPatient()
                            .getMobileNumber());


            patient.setAge(
                    dto.getPatient()
                            .getAge());


            patient.setGender(
                    dto.getPatient()
                            .getGender());


            billing.setPatient(
                    patient);
        }


        // ================= SERVICES =================

        if (dto.getServices() != null) {

            List<ServiceItem> services =
                    dto.getServices()
                            .stream()
                            .map(serviceDTO -> {

                                ServiceItem service =
                                        new ServiceItem();


                                service.setServiceId(
                                        serviceDTO
                                                .getServiceId());


                                service.setServiceName(
                                        serviceDTO
                                                .getServiceName());


                                service.setQty(
                                        serviceDTO
                                                .getQty());


                                service.setUnitPrice(
                                        serviceDTO
                                                .getUnitPrice());


                                service.setDiscountPercent(
                                        serviceDTO
                                                .getDiscountPercent());


                                service.setTaxPercent(
                                        serviceDTO
                                                .getTaxPercent());


                                return service;

                            })
                            .collect(
                                    Collectors.toList());


            billing.setServices(
                    services);
        }


        // ================= NEW TRANSACTION =================

        if (dto.getNewTransaction() != null) {

            Transaction transaction =
                    new Transaction();


            transaction.setReceiptNo(
                    dto.getNewTransaction()
                            .getReceiptNo());


            transaction.setPaymentDate(
                    dto.getNewTransaction()
                            .getPaymentDate());


            transaction.setPaymentMode(
                    dto.getNewTransaction()
                            .getPaymentMode());


            transaction.setTransactionId(
                    dto.getNewTransaction()
                            .getTransactionId());


            transaction.setAmount(
                    dto.getNewTransaction()
                            .getAmount());


            transaction.setRemarks(
                    dto.getNewTransaction()
                            .getRemarks());


            billing.setNewTransaction(
                    transaction);
        }


        // ================= PAYMENT SUMMARY =================

        if (dto.getPaymentSummary() != null) {

            PaymentSummary summary =
                    new PaymentSummary();


            summary.setSubTotal(
                    dto.getPaymentSummary()
                            .getSubTotal());


            summary.setTotalDiscount(
                    dto.getPaymentSummary()
                            .getTotalDiscount());


            summary.setTotalTax(
                    dto.getPaymentSummary()
                            .getTotalTax());


            summary.setTotalAmount(
                    dto.getPaymentSummary()
                            .getTotalAmount());


            summary.setTotalPaid(
                    dto.getPaymentSummary()
                            .getTotalPaid());


            summary.setDueAmount(
                    dto.getPaymentSummary()
                            .getDueAmount());


            billing.setPaymentSummary(
                    summary);
        }


        // ================= TRANSACTIONS =================

        if (dto.getTransactions() != null) {

            List<Transaction> transactions =
                    dto.getTransactions()
                            .stream()
                            .map(transactionDTO -> {

                                Transaction transaction =
                                        new Transaction();


                                transaction.setReceiptNo(
                                        transactionDTO
                                                .getReceiptNo());


                                transaction.setPaymentDate(
                                        transactionDTO
                                                .getPaymentDate());


                                transaction.setPaymentMode(
                                        transactionDTO
                                                .getPaymentMode());


                                transaction.setTransactionId(
                                        transactionDTO
                                                .getTransactionId());


                                transaction.setAmount(
                                        transactionDTO
                                                .getAmount());


                                transaction.setRemarks(
                                        transactionDTO
                                                .getRemarks());


                                return transaction;

                            })
                            .collect(
                                    Collectors.toList());


            billing.setTransactions(
                    transactions);
        }


        // ================= ADDITIONAL DETAILS =================

        if (dto.getAdditionalDetails() != null) {

            AdditionalDetails additionalDetails =
                    new AdditionalDetails();


            additionalDetails.setBillingStaff(

                    dto.getAdditionalDetails()
                            .getBillingStaff());


            additionalDetails.setNotes(

                    dto.getAdditionalDetails()
                            .getNotes());


            additionalDetails.setInternalComments(

                    dto.getAdditionalDetails()
                            .getInternalComments());


            billing.setAdditionalDetails(
                    additionalDetails);
        }


        return billing;
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private BillingDTO convertToDTO(
            Billing billing) {

        if (billing == null) {

            return null;
        }


        BillingDTO dto =
                new BillingDTO();


        dto.setBillingId(
                billing.getBillingId());


        dto.setClinicId(
                billing.getClinicId());


        dto.setBranchId(
                billing.getBranchId());


        dto.setDoctorId(
                billing.getDoctorId());


        dto.setVisitType(
                billing.getVisitType());


        dto.setBillDate(
                billing.getBillDate());


        dto.setInvoiceDate(
                billing.getInvoiceDate());


        dto.setInvoiceStatus(
                billing.getInvoiceStatus());


        // ================= PATIENT =================

        if (billing.getPatient() != null) {

            PatientDTO patientDTO =
                    new PatientDTO();


            patientDTO.setPatientId(
                    billing.getPatient()
                            .getPatientId());


            patientDTO.setPatientName(
                    billing.getPatient()
                            .getPatientName());


            patientDTO.setMobileNumber(
                    billing.getPatient()
                            .getMobileNumber());


            patientDTO.setAge(
                    billing.getPatient()
                            .getAge());


            patientDTO.setGender(
                    billing.getPatient()
                            .getGender());


            dto.setPatient(
                    patientDTO);
        }


        // ================= SERVICES =================

        if (billing.getServices() != null) {

            List<ServiceItemDTO> services =
                    billing.getServices()
                            .stream()
                            .map(service -> {

                                ServiceItemDTO serviceDTO =
                                        new ServiceItemDTO();


                                serviceDTO.setServiceId(
                                        service.getServiceId());


                                serviceDTO.setServiceName(
                                        service.getServiceName());


                                serviceDTO.setQty(
                                        service.getQty());


                                serviceDTO.setUnitPrice(
                                        service.getUnitPrice());


                                serviceDTO.setDiscountPercent(
                                        service.getDiscountPercent());


                                serviceDTO.setTaxPercent(
                                        service.getTaxPercent());


                                return serviceDTO;

                            })
                            .collect(
                                    Collectors.toList());


            dto.setServices(
                    services);
        }


        // ================= NEW TRANSACTION =================

        if (billing.getNewTransaction() != null) {

            TransactionDTO transactionDTO =
                    new TransactionDTO();


            transactionDTO.setReceiptNo(
                    billing.getNewTransaction()
                            .getReceiptNo());


            transactionDTO.setPaymentDate(
                    billing.getNewTransaction()
                            .getPaymentDate());


            transactionDTO.setPaymentMode(
                    billing.getNewTransaction()
                            .getPaymentMode());


            transactionDTO.setTransactionId(
                    billing.getNewTransaction()
                            .getTransactionId());


            transactionDTO.setAmount(
                    billing.getNewTransaction()
                            .getAmount());


            transactionDTO.setRemarks(
                    billing.getNewTransaction()
                            .getRemarks());


            dto.setNewTransaction(
                    transactionDTO);
        }


        // ================= PAYMENT SUMMARY =================

        if (billing.getPaymentSummary() != null) {

            PaymentSummaryDTO summaryDTO =
                    new PaymentSummaryDTO();


            summaryDTO.setSubTotal(
                    billing.getPaymentSummary()
                            .getSubTotal());


            summaryDTO.setTotalDiscount(
                    billing.getPaymentSummary()
                            .getTotalDiscount());


            summaryDTO.setTotalTax(
                    billing.getPaymentSummary()
                            .getTotalTax());


            summaryDTO.setTotalAmount(
                    billing.getPaymentSummary()
                            .getTotalAmount());


            summaryDTO.setTotalPaid(
                    billing.getPaymentSummary()
                            .getTotalPaid());


            summaryDTO.setDueAmount(
                    billing.getPaymentSummary()
                            .getDueAmount());


            dto.setPaymentSummary(
                    summaryDTO);
        }


        // ================= TRANSACTIONS =================

        if (billing.getTransactions() != null) {

            List<TransactionDTO> transactionDTOs =
                    billing.getTransactions()
                            .stream()
                            .map(transaction -> {

                                TransactionDTO transactionDTO =
                                        new TransactionDTO();


                                transactionDTO.setReceiptNo(
                                        transaction
                                                .getReceiptNo());


                                transactionDTO.setPaymentDate(
                                        transaction
                                                .getPaymentDate());


                                transactionDTO.setPaymentMode(
                                        transaction
                                                .getPaymentMode());


                                transactionDTO.setTransactionId(
                                        transaction
                                                .getTransactionId());


                                transactionDTO.setAmount(
                                        transaction
                                                .getAmount());


                                transactionDTO.setRemarks(
                                        transaction
                                                .getRemarks());


                                return transactionDTO;

                            })
                            .collect(
                                    Collectors.toList());


            dto.setTransactions(
                    transactionDTOs);
        }


        // ================= ADDITIONAL DETAILS =================

        if (billing.getAdditionalDetails() != null) {

            AdditionalDetailsDTO additionalDetailsDTO =
                    new AdditionalDetailsDTO();


            additionalDetailsDTO.setBillingStaff(

                    billing.getAdditionalDetails()
                            .getBillingStaff());


            additionalDetailsDTO.setNotes(

                    billing.getAdditionalDetails()
                            .getNotes());


            additionalDetailsDTO.setInternalComments(

                    billing.getAdditionalDetails()
                            .getInternalComments());


            dto.setAdditionalDetails(
                    additionalDetailsDTO);
        }


        return dto;
    }


    // =========================================================
    // GENERATE BILLING ID
    // =========================================================

    private String generateBillingId() {

        String uniquePart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        return "BILL-" + uniquePart;
    }
}