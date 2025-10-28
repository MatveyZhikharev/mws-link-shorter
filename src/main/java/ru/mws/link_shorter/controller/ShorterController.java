package ru.mws.link_shorter.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.exception.LinkIsInvalid;
import ru.mws.link_shorter.exception.LinkLengthOverflow;
import ru.mws.link_shorter.exception.LinkNotFoundException;
import ru.mws.link_shorter.request.CreateLinkRequest;
import ru.mws.link_shorter.service.ShorterService;

import java.io.IOException;

@RequestMapping("/api/links")
@RestController
public class ShorterController {
  private static final Logger logger = LoggerFactory.getLogger(ShorterController.class);
  private final ShorterService shorterService;

  public ShorterController(ShorterService shorterService) {
    this.shorterService = shorterService;
  }

  @GetMapping("/{shortKey}")
  public ResponseEntity<?> getLink(@PathVariable String shortKey) {
    logger.info("GET /api/links/{}", shortKey);
    try {
      LinkDto linkDto = shorterService.getOriginalLinkByShortKey(shortKey);
      logger.info("Found link: {}", linkDto);
      return ResponseEntity.ok(linkDto);
    } catch (LinkNotFoundException e) {
      logger.warn("Link not found: {}", shortKey);
      return ResponseEntity.notFound().build();
    } catch (LinkIsInvalid e) {
      logger.warn("Link is invalid: {}", shortKey);
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/")
  public ResponseEntity<?> postShortLink(@Valid @RequestBody CreateLinkRequest request, HttpServletResponse response) throws IOException {
    logger.info("POST /api/links/ - URL: {}, Length: {}", request.url(), request.len());
    try {
      LinkDto linkDto = shorterService.createShortLinkWithLen(request.url(), request.len());
      logger.info("Created link: {}", linkDto);
      return ResponseEntity.ok(linkDto);
    } catch (LinkLengthOverflow e) {
      logger.warn("Link is too long: {}", request.url());
      return ResponseEntity.badRequest().body("Длина ссылки превосходит 128 символов");
    } catch (LinkIsInvalid e) {
      logger.warn("Link is invalid: {}", request.url());
      return ResponseEntity.badRequest().body("Ссылки невалидна");
    } catch (Exception e) {
      logger.warn("Error with link: {}", request.url());
      return ResponseEntity.badRequest().body("Непредвиденная ошибка");
    }
  }
}