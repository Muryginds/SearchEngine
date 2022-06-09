package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.service.SiteService;
import com.muryginds.searchEngine.service.WebPageService;
import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkParser {

  private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
  private final WebPageService webPageService;
  private final SiteService siteService;
  private final ParseConfiguration parseConfiguration;

  public void parse(Site site) {

    var start = System.currentTimeMillis();

    log.info("Clearing old site data: {}", site.getUrl());
    siteService.findSite(site).ifPresent(siteService::delete);

    var time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Clearing finished. Time: {} sec.", time);

    start = System.currentTimeMillis();
    log.info("New scan started: {}", site.getUrl());

    site.setStatus(ParsingStatus.INDEXING);
    site.setStatusTime(LocalDateTime.now());
    siteService.save(site);

    LinkParserTask linkParserTask = LinkParserTask.initialise(
        site,
        parseConfiguration,
        webPageService
    );

    var result = forkJoinPool.invoke(linkParserTask);
    webPageService.saveAll(result);

    site.setStatus(ParsingStatus.INDEXED);
    siteService.save(site);

    time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Scan finished. Time: {} sec.", time);
  }
}