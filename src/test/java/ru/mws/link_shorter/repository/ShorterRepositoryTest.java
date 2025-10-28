package ru.mws.link_shorter.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mws.link_shorter.entity.LinkEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShorterRepositoryTest {

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

    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.jpa.show-sql", () -> "true");
    registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
  }

  @AfterAll
  static void shutDown() {
    postgres.close();
  }

  @Autowired
  private ShorterRepository shorterRepository;

  @Test
  void shouldSaveAndFindLink() {
    LinkEntity link = new LinkEntity("abc123", "https://example.com");

    LinkEntity saved = shorterRepository.save(link);
    Optional<LinkEntity> found = shorterRepository.findByShortKey("abc123");

    assertThat(found).isPresent();
    assertThat(found.get().getOriginalUrl()).isEqualTo(saved.getOriginalUrl());
    assertThat(found.get().getShortKey()).isEqualTo(saved.getShortKey());
    assertThat(found.get().getId()).isNotNull();
  }

  @Test
  void shouldReturnEmptyWhenLinkNotFound() {
    Optional<LinkEntity> found = shorterRepository.findByShortKey("nonexistent");
    assertThat(found).isEmpty();
  }

  @Test
  void shouldCheckIfShortKeyExists() {
    LinkEntity link = new LinkEntity("test123", "https://test.com");
    shorterRepository.save(link);

    assertThat(shorterRepository.existsByShortKey("test123")).isTrue();
    assertThat(shorterRepository.existsByShortKey("nonexistent")).isFalse();
  }
}