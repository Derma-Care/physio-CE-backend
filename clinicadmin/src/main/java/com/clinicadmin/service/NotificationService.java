package com.clinicadmin.service;


import org.springframework.http.ResponseEntity;
import com.clinicadmin.dto.ImageForNotificationDto;
import com.clinicadmin.dto.PriceDropAlertDto;


public interface NotificationService {
	
	public ResponseEntity<?> storeImageForNotification(ImageForNotificationDto imageForNotificationDto);
	public byte[] getImageForNotification();	
	public ResponseEntity<?> pricedrop(PriceDropAlertDto priceDropAlertDto);
	public ResponseEntity<?> priceDropNotification(String clinicId, String branchId );
	public ResponseEntity<?> updatePriceDropNotification(String clinicId, String branchId,String id, PriceDropAlertDto dto );
	public ResponseEntity<?> deletePriceDropNotification(String clinicId, String branchId,String id);
		
			
	
}
