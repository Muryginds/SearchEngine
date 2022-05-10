package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.parser.ParsingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
  Optional<Site> findByUrl(String url);
  List<Site> findAllByStatus(ParsingStatus status);
}