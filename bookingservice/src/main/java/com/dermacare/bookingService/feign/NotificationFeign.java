package com.dermacare.bookingService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.dermacare.bookingService.dto.BookingResponse;
import com.dermacare.bookingService.dto.DoctorPushNotificationDTO;
import com.dermacare.bookingService.dto.NotificationDTO;
import com.dermacare.bookingService.util.Response;

@FeignClient(value = "notification-service")
public interface NotificationFeign {

	@GetMapping("/api/notificationservice/getNotificationByBookingId/{id}")
	public NotificationDTO getNotificationByBookingId(@PathVariable String id);

	@PutMapping("/api/notificationservice/updateNotification")
	public NotificationDTO updateNotification(@RequestBody NotificationDTO notificationDTO);

	@PostMapping("/api/notificationservice/notifications")
	public ResponseEntity<Response> createNotification(@RequestBody BookingResponse booking);

	@PostMapping("/api/notificationservice/doctor-push/send")
	ResponseEntity<?> sendDoctorPushNotification(@RequestBody DoctorPushNotificationDTO dto);

}