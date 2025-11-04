package com.app.auth.service.event;

import com.app.auth.entity.Account;
import com.app.common.constant.KafkaTopics;
import com.app.common.event.AccountVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountVerifiedEvent(Account account) {
        AccountVerifiedEvent event = AccountVerifiedEvent.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .build();

        log.info("Publishing account verified event for email: {}", event.getEmail());

        kafkaTemplate.send(KafkaTopics.ACCOUNT_EVENTS, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("✅ Event published successfully: accountId={}", account.getId());
                    } else {
                        log.error("❌ Failed to publish event: accountId={}", account.getId(), ex);
                    }
                });
    }
}

