package me.danielx.api;

import me.danielx.api.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApiApplicationTests extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {}

  @Test
  void apiDocsExposeCurrentApiRoutes() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/accounts")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/auth/register")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/transactions")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/risk-settings")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/notifications")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/plaid/link-token")));
  }
}
