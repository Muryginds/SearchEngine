package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.indexer.PageIndexer;
import com.muryginds.searchEngine.parser.LinkParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteProcessingService {

  private final LinkParser linkParser;
  private final PageIndexer pageIndexer;

  public void process(String siteUrl, String siteName) {
    Site site = linkParser.parse(siteUrl, siteName);
    pageIndexer.index(site);
  }
}