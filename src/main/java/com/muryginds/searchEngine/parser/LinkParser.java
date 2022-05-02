package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.service.SiteService;
import com.muryginds.searchEngine.service.WebPageService;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkParser {

  private static final String STARTING_URL = "/";
  private static final ForkJoinPool forkJoinPool = new ForkJoinPool();
  private final WebPageService webPageService;
  private final SiteService siteService;
  private final ParseConfiguration parseConfiguration;

  public void parse(String siteUrl, String siteName) {

    long start = System.currentTimeMillis();
    Set<String> startingSet = ConcurrentHashMap.newKeySet();
    startingSet.add(STARTING_URL);

    Site site = siteService.getSite(siteUrl);
    site.setName(siteName);
    site.setStatus(ParsingStatus.INDEXING);
    site.setStatusTime(LocalDateTime.now());
    siteService.save(site);

    LinkParserTask linkParserTask = new LinkParserTask(
        STARTING_URL,
        siteUrl + STARTING_URL,
        startingSet,
        parseConfiguration,
        site,
        webPageService);

    log.info("New scan started: {}", siteUrl);

    Set<WebPage> result = forkJoinPool.invoke(linkParserTask);
    webPageService.saveAll(result);

    startingSet.clear();

    site.setStatus(ParsingStatus.INDEXED);
    siteService.save(site);

    double time = (System.currentTimeMillis() - start)/1000d;
    log.info("Scan finished. Time: {} sec.", time);
  }
}