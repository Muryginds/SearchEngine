package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.repository.WebPageRepository;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebPageService {

  private final WebPageRepository webPageRepository;

  public void save(WebPage webPage) {
    webPageRepository.save(webPage);
  }

  public void saveAll(Collection<WebPage> collection) {
    webPageRepository.saveAll(collection);
  }

  public Optional<WebPage> findById(int id) {
    return webPageRepository.findById(id);
  }

  public int getCount(Site site) {
    return webPageRepository.countBySite(site);
  }

  public Page<WebPage> getPageParsedSuccessfully(Site site, Pageable pageable) {
    return webPageRepository.findPageBySiteAndCode(site, 200, pageable);
  }
}