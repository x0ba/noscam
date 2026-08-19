package me.danielx.api.risk;

import me.danielx.api.support.PostgresIntegrationTest;
import me.danielx.api.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RiskSettingsApiTests extends PostgresIntegrationTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void defaultsAreCreatedAndInvalidUpdatesRejected() throws Exception {
    TestData.Session session =
        TestData.registerAndLogin(mockMvc, objectMapper, "settings@example.com", "supersecret12");
    mockMvc
        .perform(get("/api/v1/risk-settings").cookie(session.cookie()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alertThreshold").value(70))
        .andExpect(jsonPath("$.engineVersion").value(1));

    mockMvc
        .perform(
            put("/api/v1/risk-settings")
                .cookie(session.cookie())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"alertThreshold":70,"lowMax":80,"mediumMax":20,"factors":[{"key":"new_merchant","enabled":true,"maxPoints":10,"parameters":{}}]}
                    """))
        .andExpect(status().isUnprocessableContent());
  }
}
