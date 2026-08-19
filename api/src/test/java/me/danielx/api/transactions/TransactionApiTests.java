package me.danielx.api.transactions;

import me.danielx.api.support.PostgresIntegrationTest;
import me.danielx.api.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TransactionApiTests extends PostgresIntegrationTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void unauthenticatedCreateIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/transactions")
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void manualCreateScoresAndIsOwned() throws Exception {
    TestData.Session session =
        TestData.registerAndLogin(mockMvc, objectMapper, "txn-owner@example.com", "supersecret12");
    String accountId = TestData.createAccount(mockMvc, objectMapper, session);
    String body =
        """
        {
          "accountId": "%s",
          "amount": -1.99,
          "currency": "USD",
          "merchant": "CUSTOMS FEE *PKGHOLD"
        }
        """
            .formatted(accountId);

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/transactions")
                    .cookie(session.cookie())
                    .with(csrf())
                    .header("Idempotency-Key", "create-customs-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.merchant").value("CUSTOMS FEE *PKGHOLD"))
            .andExpect(jsonPath("$.riskScore").value(greaterThanOrEqualTo(70)))
            .andExpect(jsonPath("$.riskLevel").value("high"))
            .andReturn();

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .cookie(session.cookie())
                .with(csrf())
                .header("Idempotency-Key", "create-customs-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText()));

    JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
    mockMvc
        .perform(get("/api/v1/transactions/" + createdBody.get("id").asText()).cookie(session.cookie()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.factors").isArray());

    mockMvc
        .perform(get("/api/v1/notifications/unread-count").cookie(session.cookie()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unreadCount").value(greaterThanOrEqualTo(1)));

    TestData.Session other =
        TestData.registerAndLogin(mockMvc, objectMapper, "txn-other@example.com", "supersecret12");
    mockMvc
        .perform(get("/api/v1/transactions/" + createdBody.get("id").asText()).cookie(other.cookie()))
        .andExpect(status().isNotFound());
  }
}
