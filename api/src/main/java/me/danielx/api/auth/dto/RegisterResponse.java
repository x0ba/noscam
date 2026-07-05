package me.danielx.api.auth.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RegisterResponse(
    UUID publicId,
    String email,
    boolean emailVerified,
    String firstName,
    String lastName,
    Instant createdAt) {}
