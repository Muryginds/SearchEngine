package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.model.Site;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
  Optional<Site> findByUrl(String url);
}