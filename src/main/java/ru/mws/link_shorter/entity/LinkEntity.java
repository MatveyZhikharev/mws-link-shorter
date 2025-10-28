package ru.mws.link_shorter.entity;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Table(name = "links")
@Entity
public class LinkEntity {
  private static final Logger logger = LoggerFactory.getLogger(LinkEntity.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "short_key", nullable = false)
  private String shortKey;

  @Column(name = "original_url", nullable = false)
  private String originalUrl;

  @Column(name = "click_count")
  private int clickCount;

  public LinkEntity() {
  }
  public LinkEntity(String shortKey, String originalUrl) {
    this.shortKey = shortKey;
    this.originalUrl = originalUrl;
  }

  @PrePersist
  public void logCreate() {
    logger.debug("Creating new LinkEntity: {}", this);
  }

  @PreUpdate
  public void logUpdate() {
    logger.debug("Updating LinkEntity: {}", this);
  }

  public String getId() {
    return shortKey;
  }

  public String getShortKey() {
    return shortKey;
  }

  public void setShortKey(String shortKey) {
    this.shortKey = shortKey;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public int getClickCount() {
    return clickCount;
  }

  public void setClickCount(int clickCount) {
    this.clickCount = clickCount;
  }
}
