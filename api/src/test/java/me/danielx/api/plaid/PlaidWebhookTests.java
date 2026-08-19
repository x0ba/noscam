package me.danielx.api.plaid;

import me.danielx.api.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PlaidWebhookTests extends PostgresIntegrationTest {
  @Autowired MockMvc mockMvc;

  @Test
  void webhookIsPublicAndIdempotent() throws Exception {
    String body =
        """
        {"webhook_type":"TRANSACTIONS","webhook_code":"SYNC_UPDATES_AVAILABLE","item_id":"missing"}
        """;
    mockMvc
        .perform(post("/api/v1/plaid/webhook").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/api/v1/plaid/webhook").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());
  }
}
