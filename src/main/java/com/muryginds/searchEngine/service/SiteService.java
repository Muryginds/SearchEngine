package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.repository.SiteRepository;
import java.util.Collection;
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

  public Site getSite(String url) {
    return findByUrl(url).orElseGet(() -> {
      Site site = new Site();
      site.setUrl(url);
      return site;
    });
  }

  public void saveAll(Collection<Site> collection) {
    siteRepository.saveAll(collection);
  }

  public Optional<Site> findById(int id) {
    return siteRepository.findById(id);
  }

  public Optional<Site> findByUrl(String url) {
    return siteRepository.findByUrl(url);
  }
}