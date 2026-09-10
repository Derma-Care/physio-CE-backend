package com.dermacare.bookingService.util;

public interface SendAppNotification {

public void sendPushNotification(String deviceToken, String title, String body, String type, String screen, String sound,String path);

public void sendPushNotificationForImage(String deviceToken, String title, String body, String type, String screen, String sound,String imageUrl);

}
