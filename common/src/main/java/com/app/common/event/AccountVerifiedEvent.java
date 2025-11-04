package com.app.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerifiedEvent implements Serializable {

    private String accountId;
    private String username;
    private String email;

    @Builder.Default
    private Instant verifiedAt = Instant.now();

    @Builder.Default
    private String eventId = java.util.UUID.randomUUID().toString();
}