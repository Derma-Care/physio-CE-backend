package com.AdminService.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PermissionsUtil {

    private PermissionsUtil() {}

    // Default admin role permissions
    public static Map<String, List<String>> getAdminPermissions() {
        Map<String, List<String>> adminPermissions = new LinkedHashMap<>();

//        adminPermissions.put("Dashboard", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Appointments", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Employee management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Doctors", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Therapist", List.of("create", "read", "update", "delete"));
////        adminPermissions.put("Pharmacist", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Lab Technician", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Administrator", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("FrontDesk", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Security", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("OtherStaff", List.of("create", "read", "update", "delete"));
////        adminPermissions.put("Disease-Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Tests", List.of("create", "read", "update", "delete"));
////        adminPermissions.put("Treatments", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Therapy Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("LabReport Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Payouts", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Help", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Customer Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("ConsentForms", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Activity Library", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Program Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Package Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Patient Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Patient FeedBack", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Equipment Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Attendance Reports", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Recovery Support", List.of("create", "read", "update", "delete"));
//       
//        adminPermissions.put("Vendor Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Pharmacy Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Inventory Management", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Support", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Billing", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Analytics", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Tax reports", List.of("create", "read", "update", "delete"));
//        adminPermissions.put("Refered By Doctor", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Push Notification", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Doctor Management", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Doctors", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Book Appointments", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Analytics", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Appointments", List.of("create", "read", "update", "delete"));
         adminPermissions.put("Patient Management", List.of("create", "read", "update", "delete"));
       adminPermissions.put("FrontDesk", List.of("create", "read", "update", "delete"));
       adminPermissions.put("Help", List.of("create", "read", "update", "delete"));

        return adminPermissions;
    }
}
