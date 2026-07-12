package me.danielx.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI noScamApi() {
    Schema<?> loginForm =
        new Schema<>()
            .type("object")
            .required(java.util.List.of("email", "password"))
            .addProperties("email", new StringSchema().format("email"))
            .addProperties("password", new StringSchema().format("password"));

    return new OpenAPI()
        .info(new Info().title("NoScam API").version("v1").description("NoScam HTTP API."))
        .addTagsItem(
            new Tag().name("Authentication").description("Registration and session authentication"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "sessionCookie",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID")
                        .description("Session cookie returned after a successful login.")))
        .path(
            "/api/v1/auth/login",
            new PathItem()
                .post(
                    new Operation()
                        .addTagsItem("Authentication")
                        .summary("Log in")
                        .description("Creates an authenticated session. This route is handled by Spring Security.")
                        .requestBody(
                            new RequestBody()
                                .required(true)
                                .content(
                                    new Content()
                                        .addMediaType(
                                            "application/x-www-form-urlencoded",
                                            new MediaType().schema(loginForm))))
                        .responses(
                            new ApiResponses()
                                .addApiResponse("204", new ApiResponse().description("Login successful"))
                                .addApiResponse("401", new ApiResponse().description("Invalid credentials")))))
        .path(
            "/api/v1/auth/logout",
            new PathItem()
                .post(
                    new Operation()
                        .addTagsItem("Authentication")
                        .summary("Log out")
                        .description("Ends the current session. This route is handled by Spring Security.")
                        .addSecurityItem(new SecurityRequirement().addList("sessionCookie"))
                        .responses(
                            new ApiResponses()
                                .addApiResponse("204", new ApiResponse().description("Logout successful"))
                                .addApiResponse("401", new ApiResponse().description("Authentication is required"))
                                .addApiResponse("403", new ApiResponse().description("CSRF token is missing or invalid")))));
  }
}
