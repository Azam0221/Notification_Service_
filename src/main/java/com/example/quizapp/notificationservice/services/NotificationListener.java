package com.example.quizapp.notificationservice.services;

import com.example.quizapp.notificationservice.model.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class  NotificationListener {

    private final ObjectMapper objectMapper;
    private final EmailSenderService emailSenderService;
    private final RedisTemplate<String,String> redisTemplate;



    @KafkaListener(topics = "notification.events",groupId = "notification-service-group",containerFactory = "kafkaListenerContainerFactory")
    public void handleMessage(ConsumerRecord<String,String> record, Acknowledgment ack){
        String payload = record.value();

        try {
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
            System.out.println("Email sent to: " + event.getRecipient());

            //Checking for set key if absent with TTL
            String idKey = "notif:dedup:" + event.getRecipient();
            Boolean set = redisTemplate.opsForValue().setIfAbsent(idKey,"1", Duration.ofMinutes(10));
            if(Boolean.FALSE.equals(set)){
                System.out.println("Duplicate-Notification");
                ack.acknowledge();
                return;
            }

            //Rate Limit per recipient
            String rateKey = "notif:rate" + event.getEventType();
            String countStr = redisTemplate.opsForValue().get(rateKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            if(count > 100){
                // skip or requeue logic (here we skip)
                ack.acknowledge();
                return;
            }
            redisTemplate.opsForValue().increment(rateKey);
            redisTemplate.expire(rateKey, Duration.ofMinutes(1));


            // send the mail
            emailSenderService.sendMail(event.getRecipient(),event.getSubject(),event.getBody());

            ack.acknowledge();
//            switch (event.getEventType()){
//                case "EMAIL":
//                emailSenderService.sendMail(event.getRecipient(),
//                        "New Notification"+ event.getEventType(),
//                     "body");
//                        break;
//
//                case "INAPP":
//                    System.out.println(" In-App notification for: " + event.getRecipient());
//                    break;
//
//                case "PUSH":
//                    System.out.println("📱 Push notification to: " + event.getRecipient());
//                    break;
//
//                default:
//                    System.out.println("⚠ Unknown channel: " + event.getEventType());
//            }
        }
        catch (Exception e){
            // allow re-delivery (no ack). Consider DLQ in production (will be updated later)
            throw new RuntimeException(e);
        }

    }
}
