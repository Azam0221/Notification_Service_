package com.example.quizapp.notificationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class  NotificationListener {

    private final ObjectMapper objectMapper;
    private final EmailSenderService emailSenderService;

    @KafkaListener(topics = "notification-events",groupId = "notification-service")
    public void handleMessage(String message){

    }
}
