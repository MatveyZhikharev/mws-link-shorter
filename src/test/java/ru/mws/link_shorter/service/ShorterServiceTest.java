package ru.mws.link_shorter.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mws.link_shorter.dto.LinkDto;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class ShorterServiceTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @AfterAll
  static void shutDown() {
    postgres.close();
  }

  @Autowired
  private ShorterService shorterService;

  @Test
  void shouldCreateAndRetrieveLink() {
    String originalUrl = "https://integration-test.com";

    LinkDto created = shorterService.createShortLinkWithLen(originalUrl, 6);
    LinkDto retrieved = shorterService.getOriginalLinkByShortKey(created.shortKey());

    assertThat(retrieved.originalUrl()).isEqualTo(originalUrl);
    assertThat(retrieved.shortKey()).isEqualTo(created.shortKey());
  }

  @Test
  void shouldIncrementClickCountInRealDatabase() {
    String originalUrl = "https://click-test.com";
    LinkDto created = shorterService.createShortLinkWithLen(originalUrl, 6);

    LinkDto afterFirstClick = shorterService.getIncrementedOriginalLinkByShortKey(created.shortKey());
    LinkDto afterSecondClick = shorterService.getIncrementedOriginalLinkByShortKey(created.shortKey());

    assertThat(afterFirstClick.clickCount()).isEqualTo(1);
    assertThat(afterSecondClick.clickCount()).isEqualTo(2);
  }
}