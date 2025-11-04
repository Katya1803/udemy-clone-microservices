package com.app.email.consumer;

import com.app.common.constant.KafkaTopics;
import com.app.common.dto.common.EmailRequest;
import com.app.common.event.AccountVerifiedEvent;
import com.app.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = KafkaTopics.ACCOUNT_EVENTS,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAccountVerifiedEvent(
            @Payload AccountVerifiedEvent event,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received AccountVerifiedEvent: userId={}, eventId={}, partition={}, offset={}",
                event.getAccountId(), event.getEventId(), partition, offset);

        try {
            sendWelcomeEmail(event);
            log.info("✅ Welcome email sent successfully for account: {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email for account: {}", event.getEmail(), e);
        }
    }

    private void sendWelcomeEmail(AccountVerifiedEvent event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", event.getUsername());
        variables.put("email", event.getEmail());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(event.getEmail())
                .subject("Welcome to Our Platform! 🎉")
                .template("welcome-email")
                .variables(variables)
                .build();

        emailService.sendEmail(emailRequest);
    }

}
