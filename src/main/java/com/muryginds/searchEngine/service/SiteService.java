package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.repository.SiteRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteService {

  private final SiteRepository siteRepository;

  public void save(Site site) {
    siteRepository.save(site);
  }

  public Optional<Site> findSite(Site site) {
    return findByUrl(site.getUrl());
  }

  public Optional<Site> findByUrl(String url) {
    return siteRepository.findByUrl(url);
  }

  public void delete(Site site) {
    siteRepository.delete(site);
  }
}