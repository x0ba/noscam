package me.danielx.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {}

  @Test
  void apiDocsExposeEveryCurrentApiRoute() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/accounts")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/auth/register")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/auth/login")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/v1/auth/logout")));
  }
}
