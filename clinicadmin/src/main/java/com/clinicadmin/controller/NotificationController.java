package com.clinicadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clinicadmin.dto.ImageForNotificationDto;
import com.clinicadmin.dto.PriceDropAlertDto;
import com.clinicadmin.service.NotificationService;


@RestController
@RequestMapping("/clinic-admin")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NotificationController {
	
	@Autowired
	private NotificationService serviceImpl;
	
	
	  @PostMapping("/uploadImageForNotification")
	   	public ResponseEntity<?> uploadImageForNotification(@RequestBody ImageForNotificationDto imageForNotificationDto ){
	       	 return serviceImpl.storeImageForNotification(imageForNotificationDto);
	    }
	    
	    @GetMapping("/retrieveImageForNotification")
	   	public byte[] retrieveImageForNotification(){
	       	 return serviceImpl.getImageForNotification();
	    }

	
	    @PostMapping("/pricedrop")
	   	public ResponseEntity<?> pricedrop(@RequestBody PriceDropAlertDto priceDropAlertDto ){
	       	 return serviceImpl.pricedrop(priceDropAlertDto);
	    }
	    
	    @GetMapping("/priceDropNotification/{clinicId}/{branchId}")
	   	public ResponseEntity<?> priceDropNotification(@PathVariable String clinicId,@PathVariable String branchId){
	       	 return serviceImpl.priceDropNotification(clinicId, branchId);
	    }

	    
	    @PutMapping("/updatePriceDropNotification/{clinicId}/{branchId}/{id}")
	   	public ResponseEntity<?> updatePriceDropNotification(@PathVariable String clinicId,@PathVariable String branchId,
	   			@PathVariable String id,@RequestBody PriceDropAlertDto dto){
	       	 return serviceImpl.updatePriceDropNotification(clinicId, branchId,id, dto);
	    }
	    
	    
	    @DeleteMapping("/deletePriceDropNotification/{clinicId}/{branchId}/{id}")
	   	public ResponseEntity<?> deletePriceDropNotification(@PathVariable String clinicId,@PathVariable String branchId
	   			,@PathVariable String id){
	       	 return serviceImpl.deletePriceDropNotification(clinicId, branchId,id);
	    }

}
