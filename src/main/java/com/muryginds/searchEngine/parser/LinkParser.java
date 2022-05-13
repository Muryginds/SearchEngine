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

  public Site parse(String siteUrl, String siteName) {

    var start = System.currentTimeMillis();

    var search = siteService.findSite(siteUrl);
    search.ifPresent(siteService::delete);

    var site = new Site();
    site.setUrl(siteUrl);
    site.setName(siteName);
    site.setStatus(ParsingStatus.INDEXING);
    site.setStatusTime(LocalDateTime.now());
    siteService.save(site);

    LinkParserTask linkParserTask = new LinkParserTask(
        parseConfiguration,
        site,
        webPageService);

    log.info("New scan started: {}", siteUrl);

    var result = forkJoinPool.invoke(linkParserTask);
    webPageService.saveAll(result);

    site.setStatus(ParsingStatus.INDEXED);
    siteService.save(site);

    var time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Scan finished. Time: {} sec.", time);

    return site;
  }
}