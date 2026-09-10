//package com.clinicadmin.service.impl;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.dto.StockDTO;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.StockLedgerService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class StockLedgerServiceImpl implements StockLedgerService {
//
//    private static final Logger log = LoggerFactory.getLogger(StockLedgerServiceImpl.class);
//
//    private final PharmacyManagementFeignClient pharmacyFeignClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // --------------------------------------------------------------------------
//    // PURCHASE STOCK
//    // --------------------------------------------------------------------------
//    @Override
//    public Response addPurchase(String purchaseBillNo, StockDTO dto) {
//
//        log.info("Add Purchase Stock request | purchaseBillNo={}, productId={}",
//                purchaseBillNo, dto.getProductId());
//
//        try {
//            Response response = pharmacyFeignClient.addPurchase(purchaseBillNo, dto);
//            log.info("Purchase stock added successfully | purchaseBillNo={}", purchaseBillNo);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while adding purchase stock | purchaseBillNo={}, status={}",
//                    purchaseBillNo, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    // --------------------------------------------------------------------------
//    // SALE STOCK
//    // --------------------------------------------------------------------------
//    @Override
//    public Response addSale(String productId, String batchNo, int qty, String saleId) {
//
//        log.info("Add Sale Stock request | productId={}, batchNo={}, qty={}, saleId={}",
//                productId, batchNo, qty, saleId);
//
//        try {
//            Response response = pharmacyFeignClient.addSale(productId, batchNo, qty, saleId);
//            log.info("Sale stock added successfully | productId={}, saleId={}", productId, saleId);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while adding sale stock | productId={}, saleId={}, status={}",
//                    productId, saleId, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    // --------------------------------------------------------------------------
//    // DAMAGE STOCK
//    // --------------------------------------------------------------------------
//    @Override
//    public Response addDamage(String productId, String batchNo, int qty, String reason) {
//
//        log.info("Add Damage Stock request | productId={}, batchNo={}, qty={}",
//                productId, batchNo, qty);
//
//        try {
//            Response response = pharmacyFeignClient.addDamage(productId, batchNo, qty, reason);
//            log.info("Damage stock recorded successfully | productId={}, batchNo={}",
//                    productId, batchNo);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while adding damage stock | productId={}, batchNo={}, status={}",
//                    productId, batchNo, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    // --------------------------------------------------------------------------
//    // COMMON FEIGN ERROR HANDLER (Same as your SupplierServiceImpl)
//    // --------------------------------------------------------------------------
//    private Response extractFeignError(FeignException ex) {
//
//        log.warn("Extracting Feign error response | status={}", ex.status());
//
//        try {
//            return objectMapper.readValue(ex.contentUTF8(), Response.class);
//        } catch (Exception parseEx) {
//
//            log.error("Failed to parse Feign error response from pharmacy service", parseEx);
//
//            Response fallback = new Response();
//            fallback.setStatus(500);
//            fallback.setMessage("Unexpected error from pharmacy service: " + ex.getMessage());
//            fallback.setData(null);
//            return fallback;
//        }
//    }
//}
