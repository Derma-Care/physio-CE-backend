package com.clinicadmin.service;

import com.clinicadmin.dto.BillingDTO;
import com.clinicadmin.dto.Response;

public interface BillingService {

    // ================= CREATE =================

    Response createBilling( BillingDTO billingDTO);


    // ================= GET BY BILLING ID =================

    Response getBillingById(String billingId);


    // ================= GET ALL BY CLINIC AND BRANCH =================

    Response getAllBillings(String clinicId,String branchId);
    
    
	Response getAllBillings();


	Response getAllBillingsByClinicId(String clinicId);


    // ================= UPDATE BY BILLING ID =================

    Response updateBilling(String billingId,BillingDTO billingDTO);


    // ================= DELETE BY BILLING ID =================

    Response deleteBilling(String billingId);



}