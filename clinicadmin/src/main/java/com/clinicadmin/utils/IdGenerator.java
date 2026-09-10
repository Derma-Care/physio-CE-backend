package com.clinicadmin.utils;


import java.util.UUID;

public class IdGenerator {

    public static String generateWardBoyId() {
      
        return "WB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public static String generateSecurityStaffId() {
        return "SEC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}