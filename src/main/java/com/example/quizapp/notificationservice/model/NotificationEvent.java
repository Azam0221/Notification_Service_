package com.example.quizapp.notificationservice.model;

import lombok.Data;

@Data
public class NotificationEvent {
    private String messageId;
    private String eventType;
    private String recipient;
    private String message;
    private String sender;
}
