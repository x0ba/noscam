package me.danielx.api.support;

import me.danielx.api.auth.dto.RegisterRequest;
import me.danielx.api.common.accounts.AccountType;
import me.danielx.api.common.accounts.dto.CreateAccountRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestData {
  private TestData() {}

  public static Session registerAndLogin(
      MockMvc mockMvc, ObjectMapper objectMapper, String email, String password) throws Exception {
    RegisterRequest register =
        new RegisterRequest(email, password, "Ada", "Lovelace");
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
        .andExpect(status().isCreated());
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("email", email)
                    .param("password", password))
            .andExpect(status().isNoContent())
            .andReturn();
    return new Session(login.getResponse().getCookie("JSESSIONID").getValue());
  }

  public static String createAccount(MockMvc mockMvc, ObjectMapper objectMapper, Session session)
      throws Exception {
    CreateAccountRequest request =
        new CreateAccountRequest("Chase", "Checking", AccountType.CHECKING, "USD");
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/accounts")
                    .cookie(session.cookie())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }

  public record Session(String jsessionId) {
    public jakarta.servlet.http.Cookie cookie() {
      return new jakarta.servlet.http.Cookie("JSESSIONID", jsessionId);
    }
  }
}
