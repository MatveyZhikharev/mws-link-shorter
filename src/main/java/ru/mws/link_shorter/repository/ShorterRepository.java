package ru.mws.link_shorter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mws.link_shorter.entity.LinkEntity;

import java.util.Optional;

public interface ShorterRepository extends JpaRepository<LinkEntity, Long> {
  Optional<LinkEntity> findByShortKey(String shortKey);

  Optional<LinkEntity> findByOriginalUrl(String originalUrl);

  boolean existsByShortKey(String shortKey);
}
