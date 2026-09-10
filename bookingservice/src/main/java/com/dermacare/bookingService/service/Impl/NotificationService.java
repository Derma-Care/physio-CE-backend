package com.dermacare.bookingService.service.Impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.dermacare.bookingService.dto.BookingResponse;
import com.dermacare.bookingService.feign.AdminServiceClient;
import com.dermacare.bookingService.util.Response;
import com.dermacare.bookingService.util.SendAppNotification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

 @Autowired
 private AdminServiceClient adminServiceClient;
 
 @Autowired
 private SendAppNotification appNotification;
 
	 private List<String> Id = null;
	

// @Secured("ROLE_BOOKINGSERVICE")
// @RateLimiter(name = "notificationService", fallbackMethod = "createNotificationFallback")
 public ResponseEntity<Response> createNotification(BookingResponse bookingDTO) {

     log.info("Create notification request received. BookingId={}, CustomerId={}, ClinicId={}, BranchId={}",
             bookingDTO.getBookingId(),
             bookingDTO.getCustomerId(),
             bookingDTO.getClinicId(),
             bookingDTO.getBranchId());

     Response res = new Response();

     try {

         log.debug("Converting booking to notification entity. BookingId={}",
                 bookingDTO.getBookingId());

        // convertToNotification(bookingDTO);
   
         Id = adminServiceClient.getFcmTokens(bookingDTO.getClinicId(), bookingDTO.getBranchId());
         //System.out.println(Id);

         log.info("Clinic device id fetched successfully. DevicePresent={}",
                 Id != null);
         //System.out.println(bookingDTO);
         try {
             if (Id != null) {         	 
            	 Id.stream().map(n->{   String result = n.substring(n.indexOf(":") + 1);
            	// System.out.println(result);
            		   String content =
                               "AppointmentId:" + bookingDTO.getBookingId() + "\n\n"
                                       + "Doctor:" + bookingDTO.getDoctorName() + "\n\n"
                                       + "Branch:" + bookingDTO.getBranchname() + "\n\n"
                                       + "Date:" + bookingDTO.getServiceDate() + "\n\n"
                                       + "Time:" + bookingDTO.getServicetime();

                       log.info("Sending notification to clinic admin. BookingId={}, DeviceId={}",
                               bookingDTO.getBookingId(),
                               result);

                       appNotification.sendPushNotification(
                    		   result,
                               "An appointment has been successfully confirmed for "
                                       + bookingDTO.getName() + ".\n\n",
                               content,
                               "BOOKING",
                               "BookingScreen",
                               "default",
                               "dashboard");

                       log.info("Clinic admin notification sent successfully. BookingId={}",
                               bookingDTO.getBookingId());
                    return n;}).toList();}

             res.setMessage("notification sent");
             res.setStatus(200);
             res.setSuccess(true);

             log.info("Notification process completed successfully. BookingId={}",
                     bookingDTO.getBookingId());

         } catch (Exception e) {

             log.error("Failed while sending push notification. BookingId={}, Error={}",
                     bookingDTO.getBookingId(),
                     e.getMessage(),
                     e);

             res.setMessage(e.getMessage());
             res.setStatus(404);
             res.setSuccess(false);
         }

     } catch (Exception e) {

         log.error("Unexpected error while creating notification. BookingId={}, Error={}",
                 bookingDTO.getBookingId(),
                 e.getMessage(),
                 e);

         res.setMessage(e.getMessage());
         res.setStatus(500);
         res.setSuccess(false);
     }

     log.info("Returning createNotification response. Status={}", res.getStatus());

     return ResponseEntity.status(res.getStatus()).body(res);
 }
 
 
 public void sendFollowupReminder(String cId, String bId, String name, String patientId,String time,String appointmentId,String doctorname) {
	    Response res = new Response();
	    try {
	        Id = adminServiceClient.getFcmTokens(cId, bId);

	        log.info("Clinic device id fetched successfully. DevicePresent={}", Id != null);
     // System.out.println(Id);
	        try {
	            if (Id != null) {
	                Id.stream().map(n -> { String result = n.substring(n.indexOf(":") + 1);
	               // System.out.println(result);
	                    String content =
	                            "Follow-up Appointment Details\n\n"
	                                    + "AppointmentId: " + appointmentId + "\n\n"
	                                    + "Patient: " + name + "\n\n"
	                                    + "Doctor: " + doctorname + "\n\n"
	                                    + "Branch: " + bId + "\n\n"
	                                    + "Date: " + LocalDate.now() + "\n\n"
	                                    + "Time: " + time;

	               
	                    appNotification.sendPushNotification(
	                    		result,
	                            "Follow-up Appointment Reminder for " + name,
	                            content,
	                            "BOOKING",
	                               "BookingScreen",
	                               "default",
	                               "dashboard"
	                    );

	                    return n;
	                }).toList();
	            }

	            res.setMessage("Follow-up notification sent");
	            res.setStatus(200);
	            res.setSuccess(true);
	        } catch (Exception e) {
	            log.error("Error={}", e);
	            res.setMessage(e.getMessage());
	            res.setStatus(404);
	            res.setSuccess(false);
	        }
	    } catch (Exception e) {
	        log.error("Outer Error={}", e);
	    }
	}}
