package com.muryginds.searchEngine;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.indexer.PageIndexer;
import com.muryginds.searchEngine.parser.LinkParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteProcessor {

  private final LinkParser linkParser;
  private final PageIndexer pageIndexer;

  public void process(Site site) {
    linkParser.parse(site);
    pageIndexer.index(site);
  }
}