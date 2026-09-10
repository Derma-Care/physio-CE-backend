package com.clinicadmin.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PushNotificationService {

    // ================= NOTIFICATION TYPES =================
    public static final String TYPE_SESSION_FEEDBACK = "SESSION_FEEDBACK";

    // ================= 50% SESSION COMPLETED =================
    public void sendHalfSessionNotification(
            String fcmToken,
            String bookingId,
            String patientName,
            String mobileNumber) {

        sendNotification(
                fcmToken,
                "Patient Feedback Reminder",
                patientName
                        + " has completed 50% of their sessions. "
                        + "Please collect feedback and a rating from the patient "
                        + "to help us monitor their treatment experience.",
                bookingId,
                patientName,
                mobileNumber,
                TYPE_SESSION_FEEDBACK,
                "/session-feedback");
    }

    // ================= 100% SESSION COMPLETED =================
    public void sendFullSessionNotification(
            String fcmToken,
            String bookingId,
            String patientName,
            String mobileNumber) {

        sendNotification(
                fcmToken,
                "Treatment Completed – Feedback Required",
                patientName
                        + " has completed 100% of their sessions. "
                        + "Please collect the patient's final feedback and rating "
                        + "before closing the treatment.",
                bookingId,
                patientName,
                mobileNumber,
                TYPE_SESSION_FEEDBACK,
                "/session-feedback");
    }

    // ================= CORE METHOD =================
    private void sendNotification(
            String fcmToken,
            String title,
            String body,
            String bookingId,
            String patientName,
            String mobileNumber,
            String type,
            String path) {

        try {

            Message message =
                    Message.builder()
                            .setToken(fcmToken)
                            .setNotification(
                                    Notification.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                            .putData("bookingId",
                                    bookingId != null ? bookingId : "")
                            .putData("patientName",
                                    patientName != null ? patientName : "")
                            .putData("mobileNumber",
                                    mobileNumber != null ? mobileNumber : "")
                            .putData("type", type)
                            .putData("path", path)
                            .build();

            String messageId =
                    FirebaseMessaging.getInstance().send(message);

            log.info(
                    "Notification sent | MessageId: {} | Type: {} | BookingId: {} | Name: {} | Mobile: {}",
                    messageId,
                    type,
                    bookingId,
                    patientName,
                    mobileNumber);

        } catch (Exception e) {

            log.error(
                    "Notification failed | Type: {} | BookingId: {} | Name: {} | Mobile: {} | Error: {}",
                    type,
                    bookingId,
                    patientName,
                    mobileNumber,
                    e.getMessage());
        }
    }
}