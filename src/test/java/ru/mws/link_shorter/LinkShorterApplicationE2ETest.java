package ru.mws.link_shorter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mws.link_shorter.dto.LinkDto;
import ru.mws.link_shorter.request.CreateLinkRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LinkShorterApplicationE2ETest {
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

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldCreateShortLink() {
    CreateLinkRequest request = new CreateLinkRequest("https://example.com", 6);

    ResponseEntity<LinkDto> createResponse = restTemplate.postForEntity(
        "/api/links/",
        request,
        LinkDto.class
    );

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    LinkDto createdLink = createResponse.getBody();
    String shortKey = createdLink.shortKey();

    ResponseEntity<LinkDto> infoResponse = restTemplate.getForEntity(
        "/api/links/" + shortKey,
        LinkDto.class
    );

    assertThat(infoResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(infoResponse.getBody().originalUrl()).isEqualTo("https://example.com");
  }

  @Test
  void shouldReturn404ForNonExistentLink() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/links/nonexistent123",
        String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldReturn400ForInvalidUrl() {
    CreateLinkRequest request = new CreateLinkRequest("invalid-url", 6);

    ResponseEntity<String> response = restTemplate.postForEntity(
        "/api/links/",
        request,
        String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturn400ForTooLongUrl() {
    String longUrl = "https://" + "a".repeat(200) + ".com";
    CreateLinkRequest request = new CreateLinkRequest(longUrl, 6);

    ResponseEntity<String> response = restTemplate.postForEntity(
        "/api/links/",
        request,
        String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturnSameShortLinkForSameUrl() {
    String originalUrl = "https://google.com";
    CreateLinkRequest request = new CreateLinkRequest(originalUrl, 6);

    ResponseEntity<String> firstResponse = restTemplate.postForEntity(
        "/api/links/",
        request,
        String.class
    );

    ResponseEntity<String> secondResponse = restTemplate.postForEntity(
        "/api/links/",
        request,
        String.class
    );

    assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    String firstShortKey = extractShortKey(firstResponse.getBody());
    String secondShortKey = extractShortKey(secondResponse.getBody());

    assertThat(firstShortKey).isEqualTo(secondShortKey);
  }

  @Test
  void shouldIncrementClickCounter() {
    CreateLinkRequest request = new CreateLinkRequest("https://test-click.com", 6);
    ResponseEntity<LinkDto> createResponse = restTemplate.postForEntity(
        "/api/links/",
        request,
        LinkDto.class
    );

    String shortKey = createResponse.getBody().shortKey();
    int initialCount = createResponse.getBody().clickCount();

    for (int i = 0; i < 3; i++) {
      restTemplate.getForEntity("/api/links/" + shortKey, LinkDto.class);
    }

    ResponseEntity<LinkDto> infoResponse = restTemplate.getForEntity(
        "/api/links/" + shortKey,
        LinkDto.class
    );
    assertThat(infoResponse.getBody().clickCount()).isEqualTo(initialCount);

    TestRestTemplate noRedirectTemplate = new TestRestTemplate();
    noRedirectTemplate.getRestTemplate().setRequestFactory(
        new SimpleClientHttpRequestFactory()
    );
  }

  @Test
  void shouldServeHomePage() {
    ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("MWS link shorter");
  }

  private String extractShortKey(String jsonResponse) {
    int start = jsonResponse.indexOf("\"shortKey\":\"") + 12;
    int end = jsonResponse.indexOf("\"", start);
    return jsonResponse.substring(start, end);
  }
}