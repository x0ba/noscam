package me.danielx.api.auth.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RegisterUserResponse(
        UUID publicId,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName,
        Instant createdAt
) {
}
