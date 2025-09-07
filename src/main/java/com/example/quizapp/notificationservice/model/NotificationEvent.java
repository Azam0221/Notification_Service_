package com.example.quizapp.notificationservice.model;

import lombok.Data;

@Data
public class NotificationEvent {
    private String id;
    private String eventType;
    private String recipient;
    private String subject;
    private String body;
    private long occurredOn;
}
