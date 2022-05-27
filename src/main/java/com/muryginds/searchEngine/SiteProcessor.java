package com.muryginds.searchEngine;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.indexer.PageIndexer;
import com.muryginds.searchEngine.parser.LinkParser;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteProcessor {

  private final LinkParser linkParser;
  private final PageIndexer pageIndexer;

  public void process(Site site, Map<String, BigDecimal> fields) {
    linkParser.parse(site);
    pageIndexer.index(site, fields);
  }
}