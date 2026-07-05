package me.danielx.api.auth.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LoginResponse (
        UUID publicId,
        String email,
        boolean isVerified,
        String firstName,
        String lastName
) {
}
