package com.example.quizapp.notificationservice.services;

import com.example.quizapp.notificationservice.model.NotificationEvent;
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

        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);

            switch (event.getEventType()){
                case "EMAIL":
                emailSenderService.sendMail(event.getRecipient(),
                        "New Notification"+ event.getEventType(),
                        event.getMessageId());
                        break;

                case "INAPP":
                    System.out.println(" In-App notification for: " + event.getRecipient());
                    break;

                case "PUSH":
                    System.out.println("📱 Push notification to: " + event.getRecipient());
                    break;

                default:
                    System.out.println("⚠ Unknown channel: " + event.getEventType());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
