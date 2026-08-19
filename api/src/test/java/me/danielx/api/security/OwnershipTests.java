package me.danielx.api.security;

import me.danielx.api.support.PostgresIntegrationTest;
import me.danielx.api.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class OwnershipTests extends PostgresIntegrationTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void accountsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/accounts")).andExpect(status().isUnauthorized());
  }

  @Test
  void csrfIsRequiredForAccountCreate() throws Exception {
    TestData.Session session =
        TestData.registerAndLogin(mockMvc, objectMapper, "csrf@example.com", "supersecret12");
    mockMvc
        .perform(
            post("/api/v1/accounts")
                .cookie(session.cookie())
                .contentType("application/json")
                .content(
                    """
                    {"bank":"Chase","accountName":"Checking","type":"CHECKING","currency":"USD"}
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void foreignPlaidItemIsHidden() throws Exception {
    TestData.Session session =
        TestData.registerAndLogin(mockMvc, objectMapper, "plaid-owner@example.com", "supersecret12");
    mockMvc
        .perform(
            post("/api/v1/plaid/items/" + UUID.randomUUID() + "/sync")
                .cookie(session.cookie())
                .with(csrf()))
        .andExpect(status().isNotFound());
  }
}
