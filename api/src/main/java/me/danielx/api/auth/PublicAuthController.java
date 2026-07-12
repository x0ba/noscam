package me.danielx.api.auth;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.danielx.api.auth.dto.LoginRequest;
import me.danielx.api.auth.dto.LoginResponse;
import me.danielx.api.auth.dto.RegisterRequest;
import me.danielx.api.auth.dto.RegisterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration and session authentication")
public class PublicAuthController {
  private final PublicAuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register a user")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Email already exists")
  })
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
