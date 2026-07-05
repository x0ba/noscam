package me.danielx.api.auth.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record LoginResponse(
    UUID publicId, String email, boolean isVerified, String firstName, String lastName) {}
