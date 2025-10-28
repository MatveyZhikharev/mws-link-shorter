package ru.mws.link_shorter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.exception.LinkIsInvalid;
import ru.mws.link_shorter.exception.LinkLengthOverflow;
import ru.mws.link_shorter.exception.LinkNotFoundException;
import ru.mws.link_shorter.request.CreateLinkRequest;
import ru.mws.link_shorter.service.ShorterService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShorterController.class)
class ShorterControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ShorterService shorterService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldReturnLinkWhenExists() throws Exception {
    String shortKey = "abc123";
    LinkDto linkDto = new LinkDto(shortKey, "https://example.com", 5);
    when(shorterService.getOriginalLinkByShortKey(shortKey)).thenReturn(linkDto);

    mockMvc.perform(get("/api/links/{shortKey}", shortKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortKey").value(shortKey))
        .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
        .andExpect(jsonPath("$.clickCount").value(5));
  }

  @Test
  void shouldReturn404WhenLinkNotFound() throws Exception {
    String shortKey = "nonexistent";
    when(shorterService.getOriginalLinkByShortKey(shortKey))
        .thenThrow(new LinkNotFoundException("Link not found"));

    mockMvc.perform(get("/api/links/{shortKey}", shortKey))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateShortLinkSuccessfully() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("https://example.com", 6);
    LinkDto linkDto = new LinkDto("abc123", "https://example.com", 0);

    when(shorterService.createShortLinkWithLen(request.url(), request.len()))
        .thenReturn(linkDto);

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortKey").value("abc123"))
        .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
        .andExpect(jsonPath("$.clickCount").value(0));
  }

  @Test
  void shouldReturn400WhenLinkIsTooLong() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("https://" + "a".repeat(130) + ".com", 6);

    when(shorterService.createShortLinkWithLen(request.url(), request.len()))
        .thenThrow(new LinkLengthOverflow("Link is too long"));

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenLinkIsInvalid() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("invalid-url", 6);

    when(shorterService.createShortLinkWithLen(request.url(), request.len()))
        .thenThrow(new LinkIsInvalid("Link is invalid"));

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Ссылки невалидна"));
  }

  @Test
  void shouldReturn400ForUnexpectedError() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("https://example.com", 6);

    when(shorterService.createShortLinkWithLen(request.url(), request.len()))
        .thenThrow(new RuntimeException("Unexpected error"));

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Непредвиденная ошибка"));
  }

  @Test
  void shouldValidateRequestPayload() throws Exception {
    String invalidJson = "{}";

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldHandleEmptyUrl() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("", 6);

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldHandleNullUrl() throws Exception {
    String invalidJson = "{\"len\": 6}";

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldHandleInvalidLength() throws Exception {
    CreateLinkRequest request = new CreateLinkRequest("https://example.com", 0);

    mockMvc.perform(post("/api/links/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}