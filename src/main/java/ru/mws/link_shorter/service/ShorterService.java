package ru.mws.link_shorter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.entity.LinkEntity;
import ru.mws.link_shorter.exception.LinkIsInvalid;
import ru.mws.link_shorter.exception.LinkLengthOverflow;
import ru.mws.link_shorter.exception.LinkNotFoundException;
import ru.mws.link_shorter.repository.ShorterRepository;

import java.beans.Transient;
import java.util.Optional;

@Service
public class ShorterService {
  private static final Logger logger = LoggerFactory.getLogger(ShorterService.class);
  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private final ShorterRepository shorterRepository;

  public ShorterService(ShorterRepository shorterRepository) {
    this.shorterRepository = shorterRepository;
  }

  public LinkDto getOriginalLinkByShortKey(String shortKey) {
    logger.debug("Finding link by short key: {}", shortKey);
    Optional<LinkEntity> linkEntity = shorterRepository.findByShortKey(shortKey);
    if (linkEntity.isEmpty()) {
      logger.warn("Link not found for short key: {}", shortKey);
      throw new LinkNotFoundException("Link with short key " + shortKey + " not found");
    }
    logger.debug("Link found: {}", linkEntity.get());
    return new LinkDto(linkEntity.get().getShortKey(), linkEntity.get().getOriginalUrl(), linkEntity.get().getClickCount());
  }

  @Transactional
  public LinkDto createShortLinkWithLen(String originalUrl, int len) {
    logger.info("Creating short link for URL: {}, length: {}", originalUrl, len);
    if (originalUrl.length() > 128) {
      logger.warn("Original link is too long: {}", originalUrl);
      throw new LinkLengthOverflow("Link:" + originalUrl + " is too long: " + originalUrl.length());
    }

    if (!linkIsValid(originalUrl)) {
      logger.warn("Original link is invalid: {}", originalUrl);
      throw new LinkIsInvalid("Link is invalid: " + originalUrl);
    }

    Optional<LinkEntity> existing = shorterRepository.findByOriginalUrl(originalUrl);
    if (existing.isPresent()) {
      logger.info("Found existing link for URL: {}", originalUrl);
      return new LinkDto(existing.get().getShortKey(), existing.get().getOriginalUrl(), existing.get().getClickCount());
    }

    String shortKey;
    int attempts = 0;
    do {
      String salt = attempts > 0 ? "_" + attempts : "";
      long hash = Math.abs((originalUrl + salt).hashCode());
      shortKey = encodeAlphabetBase(hash);
      while (shortKey.length() < len) {
        shortKey += "0";
      }
      attempts++;
      logger.debug("Generation attempt {}: shortKey = {}", attempts, shortKey);
    } while (shorterRepository.existsByShortKey(shortKey) && attempts < 10);
    shortKey = shortKey.substring(0, len);

    if (attempts >= 10) {
      logger.error("Failed to generate unique short key after {} attempts", attempts);
    }

    LinkEntity newLinkEntity = new LinkEntity(shortKey, originalUrl);
    shorterRepository.save(newLinkEntity);
    logger.info("Created new link: {}", newLinkEntity);

    return new LinkDto(newLinkEntity.getShortKey(), newLinkEntity.getOriginalUrl(), newLinkEntity.getClickCount());
  }

  @Transactional
  public LinkDto getIncrementedOriginalLinkByShortKey(String shortKey) {
    logger.debug("Finding link to increment by short key: {}", shortKey);
    Optional<LinkEntity> linkEntity = shorterRepository.findByShortKey(shortKey);
    if (linkEntity.isEmpty()) {
      logger.warn("Link not found for short key: {}", shortKey);
      throw new LinkNotFoundException("Link with short key " + shortKey + " not found");
    }
    linkEntity.get().setClickCount(linkEntity.get().getClickCount() + 1);
    shorterRepository.save(linkEntity.get());
    logger.debug("Link was incremented: {}", linkEntity.get());
    return new LinkDto(linkEntity.get().getShortKey(), linkEntity.get().getOriginalUrl(), linkEntity.get().getClickCount());
  }

  private boolean linkIsValid(String originalUrl) {
    return originalUrl.contains(".") && (originalUrl.length() > 4);
  }

  private String encodeAlphabetBase(long number) {
    if (number == 0) return "0";

    StringBuilder sb = new StringBuilder();
    while (number > 0) {
      sb.append(ALPHABET.charAt((int) (number % ALPHABET.length())));
      number /= ALPHABET.length();
    }
    return sb.reverse().toString();
  }
}