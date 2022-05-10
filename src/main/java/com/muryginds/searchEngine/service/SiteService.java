package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.parser.ParsingStatus;
import com.muryginds.searchEngine.repository.SiteRepository;
import java.util.List;
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

  public List<Site> findParsed() {
    return siteRepository.findAllByStatus(ParsingStatus.INDEXED);
  }

  public Optional<Site> findByUrl(String url) {
    return siteRepository.findByUrl(url);
  }
}