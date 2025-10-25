package ru.mws.link_shorter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShorterController {
  @GetMapping("/get-short-link")
  public String getShortLink() {
    return "";
  }

  @PostMapping("/get-short-link")
  public String postShortLink() {
    return "";
  }
}
