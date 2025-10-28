package ru.mws.link_shorter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.exception.LinkNotFoundException;
import ru.mws.link_shorter.service.ShorterService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
public class PageControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ShorterService shorterService;

  @Test
  void shouldRedirectToOriginalUrl() throws Exception {
    String shortKey = "abc123";
    LinkDto linkDto = new LinkDto(shortKey, "https://example.com", 5);
    when(shorterService.getIncrementedOriginalLinkByShortKey(shortKey)).thenReturn(linkDto);

    mockMvc.perform(get("/{shortKey}", shortKey))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com"));
  }

  @Test
  void shouldAddHttpsWhenMissingProtocol() throws Exception {
    String shortKey = "abc123";
    LinkDto linkDto = new LinkDto(shortKey, "example.com", 5);
    when(shorterService.getIncrementedOriginalLinkByShortKey(shortKey)).thenReturn(linkDto);

    mockMvc.perform(get("/{shortKey}", shortKey))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com"));
  }

  @Test
  void shouldReturn404WhenRedirectLinkNotFound() throws Exception {
    String shortKey = "non.existent";
    when(shorterService.getIncrementedOriginalLinkByShortKey(shortKey))
        .thenThrow(new LinkNotFoundException("Link not found"));

    mockMvc.perform(get("/{shortKey}", shortKey))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnHomePage() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
  }
}