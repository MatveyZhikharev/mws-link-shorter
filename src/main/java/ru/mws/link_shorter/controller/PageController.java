package ru.mws.link_shorter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.exception.LinkNotFoundException;
import ru.mws.link_shorter.service.ShorterService;

import java.net.URI;

@Controller
public class PageController {
  private static final Logger logger = LoggerFactory.getLogger(PageController.class);
  private final ShorterService shorterService;

  public PageController(ShorterService shorterService) {
    this.shorterService = shorterService;
  }

  @GetMapping("/")
  public String homePage() {
    logger.info("GET / - Home page");
    return "index";
  }

  @GetMapping("/{shortKey}")
  public ResponseEntity<String> redirect(@PathVariable String shortKey) {
    logger.info("GET /{} - Redirect attempt", shortKey);
    try {
      LinkDto linkDto = shorterService.getIncrementedOriginalLinkByShortKey(shortKey);

      String url = linkDto.originalUrl();
      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "https://" + url;
      }

      logger.info("Redirecting {} -> {}", shortKey, url);
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(url))
          .build();
    } catch (LinkNotFoundException e) {
      logger.warn("Link not found for key: {}", shortKey);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ссылка не найдена");
    } catch (Exception e) {
      logger.warn("Error with link: {}", shortKey);
      logger.warn(e.toString());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Непредвиденная ошибка");
    }
  }
}
