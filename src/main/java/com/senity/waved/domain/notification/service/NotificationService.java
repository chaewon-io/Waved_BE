package com.senity.waved.domain.notification.service;

import com.senity.waved.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getNotifications(String email);
    void deleteNotification(Long notificationId);
}