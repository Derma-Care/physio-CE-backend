//package com.clinicadmin.service.impl;
//
//import com.clinicadmin.dto.CityDTO;
//import com.clinicadmin.dto.Response;
//import com.clinicadmin.feignclient.PharmacyManagementFeignClient;
//import com.clinicadmin.service.CityService;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import feign.FeignException;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CityServiceImpl implements CityService {
//
//    private final PharmacyManagementFeignClient pharmacyClient;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public Response saveCity(CityDTO dto) {
//        try {
//            return pharmacyClient.saveCity(dto);
//        } catch (FeignException ex) {
//            return extractPharmacyError(ex);
//        }
//    }
//
//    @Override
//    public Response updateCity(String id, CityDTO dto) {
//        try {
//            return pharmacyClient.updateCity(id, dto);
//        } catch (FeignException ex) {
//            return extractPharmacyError(ex);
//        }
//    }
//
//    @Override
//    public Response getCityById(String id) {
//        try {
//            return pharmacyClient.getCityById(id);
//        } catch (FeignException ex) {
//            return extractPharmacyError(ex);
//        }
//    }
//
//    @Override
//    public Response getAllCities() {
//        try {
//            return pharmacyClient.getAllCities();
//        } catch (FeignException ex) {
//            return extractPharmacyError(ex);
//        }
//    }
//
//    @Override
//    public Response deleteCity(String id) {
//        try {
//            return pharmacyClient.deleteCity(id);
//        } catch (FeignException ex) {
//            return extractPharmacyError(ex);
//        }
//    }
//
//    // Extract pharmacy error JSON (same logic as AreaServiceImpl)
//    private Response extractPharmacyError(FeignException ex) {
//        try {
//            return objectMapper.readValue(ex.contentUTF8(), Response.class);
//        } catch (Exception parseError) {
//            Response fallback = new Response();
//            fallback.setStatus(500);
//            fallback.setMessage("Unexpected error: " + ex.getMessage());
//            fallback.setData(null);
//            return fallback;
//        }
//    }
//}
