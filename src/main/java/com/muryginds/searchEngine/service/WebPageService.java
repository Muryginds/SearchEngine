package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.repository.WebPageRepository;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebPageService {

  private final WebPageRepository webPageRepository;

  public void saveAll(Collection<WebPage> collection) {
    webPageRepository.saveAll(collection);
  }

  public Page<WebPage> getPageParsedWithCode(Site site, Integer code, Pageable pageable) {
    return webPageRepository.findPageBySiteAndCode(site, code, pageable);
  }
}