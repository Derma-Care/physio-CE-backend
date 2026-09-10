//package com.clinicadmin.service.impl;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import com.clinicadmin.dto.StockDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.StockService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//@Service
//public class StockServiceImpl implements StockService {
//
//    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);
//
//    private final PharmacyManagementFeignClient pharmacyFeignClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public Response addStock(StockDTO dto) {
//
//        log.info("Add Stock request received | productId={}", dto.getProductId());
//
//        try {
//            Response response = pharmacyFeignClient.addStock(dto);
//            log.info("Stock added successfully | productId={}", dto.getProductId());
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while adding stock | status={}", ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response updateStock(String id, StockDTO dto) {
//
//        log.info("Update Stock request | stockId={}", id);
//
//        try {
//            Response response = pharmacyFeignClient.updateStock(id, dto);
//            log.info("Stock updated successfully | stockId={}", id);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while updating stock | stockId={}, status={}",
//                    id, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response getStockById(String id) {
//
//        log.info("Get Stock by ID request | stockId={}", id);
//
//        try {
//            Response response = pharmacyFeignClient.getStockById(id);
//            log.info("Stock fetched successfully | stockId={}", id);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while fetching stock | stockId={}, status={}",
//                    id, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response getStockByProductId(String productId) {
//
//        log.info("Get Stock by productId request | productId={}", productId);
//
//        try {
//            Response response = pharmacyFeignClient.getStockByProductId(productId);
//            log.info("Stock fetched successfully | productId={}", productId);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while fetching stock by productId | productId={}, status={}",
//                    productId, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response getAllStock() {
//
//        log.info("Get all stock request");
//
//        try {
//            Response response = pharmacyFeignClient.getAllStock();
//            log.info("All stock fetched successfully");
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while fetching all stock | status={}", ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    @Override
//    public Response deleteStock(String id) {
//
//        log.info("Delete Stock request | stockId={}", id);
//
//        try {
//            Response response = pharmacyFeignClient.deleteStock(id);
//            log.info("Stock deleted successfully | stockId={}", id);
//            return response;
//        } catch (FeignException ex) {
//
//            log.error("Feign error while deleting stock | stockId={}, status={}",
//                    id, ex.status(), ex);
//            return extractFeignError(ex);
//        }
//    }
//
//    /**
//     * Same FeignException handler pattern used in SupplierServiceImpl
//     */
//    private Response extractFeignError(FeignException ex) {
//
//        log.warn("Extracting Feign error response | status={}", ex.status());
//
//        try {
//            return objectMapper.readValue(ex.contentUTF8(), Response.class);
//        } catch (Exception parseEx) {
//
//            log.error("Failed to parse Feign error response", parseEx);
//
//            Response fallback = new Response();
//            fallback.setStatus(500);
//            fallback.setMessage("Unexpected error from pharmacy service: " + ex.getMessage());
//            fallback.setData(null);
//            return fallback;
//        }
//    }
//}
