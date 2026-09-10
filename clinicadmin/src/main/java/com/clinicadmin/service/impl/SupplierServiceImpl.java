//package com.clinicadmin.service.impl;
//
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.SupplierDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.SupplierService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Service
//@RequiredArgsConstructor
//public class SupplierServiceImpl implements SupplierService {
//    private static final Logger log = LoggerFactory.getLogger(SupplierServiceImpl.class);
//
//    private final PharmacyManagementFeignClient pharmacyFeignClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public Response addSupplier(SupplierDTO dto) {
//        log.info("Add Supplier request received | supplierName={}", dto.getSupplierName());
//
//    	try {
//    		log.info("Supplier added successfully");
//            return pharmacyFeignClient.addSupplier(dto);
//        } catch (FeignException ex) {
//            log.error("Feign error while adding supplier | status={}", ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response updateSupplier(String supplierId, SupplierDTO dto) {
//        log.info("Update Supplier request | supplierId={}", supplierId);
//
//    	try {
//            log.info("Supplier updated successfully | supplierId={}", supplierId);
//            return pharmacyFeignClient.updateSupplier(supplierId, dto);
//        } catch (FeignException ex) {
//        	log.error("Feign error while updating supplier | supplierId={}, status={}",
//                    supplierId, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response getSupplierById(String supplierId) {
//    	log.info("Get Supplier by ID request | supplierId={}", supplierId);
//        try {
//        	log.info("Supplier fetched successfully | supplierId={}", supplierId);
//            return pharmacyFeignClient.getSupplierById(supplierId);
//        } catch (FeignException ex) {
//        	
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response getAllSuppliers() {
//        try {
//            log.info("All suppliers fetched successfully");
//            return pharmacyFeignClient.getAllSuppliers();
//        } catch (FeignException ex) {
//            log.error("Feign error while fetching all suppliers | status={}", ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response deleteSupplier(String supplierId) {
//    	log.info("Delete Supplier request | supplierId={}", supplierId);
//        try {
//            log.info("Supplier deleted successfully | supplierId={}", supplierId);
//
//            return pharmacyFeignClient.deleteSupplier(supplierId);
//        } catch (FeignException ex) {
//        	log.error("Feign error while deleting supplier | supplierId={}, status={}",
//                    supplierId, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    // Same Feign Error Handler used in PurchaseBillServiceImpl
//    private Response extractFeignError(FeignException ex) {
//        log.warn("Extracting Feign error response | status={}", ex.status());
//
//        try {
//            return objectMapper.readValue(ex.contentUTF8(), Response.class);
//        } catch (Exception parseEx) {
//        	log.error("Failed to parse Feign error response", parseEx);
//            Response fallback = new Response();
//            fallback.setStatus(500);
//            fallback.setMessage("Unexpected error from pharmacy service: " + ex.getMessage());
//            fallback.setData(null);
//            return fallback;
//        }
//    }
//}
