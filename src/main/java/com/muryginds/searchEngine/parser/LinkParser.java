package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.model.WebPage;
import com.muryginds.searchEngine.service.WebPageService;
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

  private final WebPageService webPageService;
  private final ForkJoinPool forkJoinPool = new ForkJoinPool();
  private static final String CURRENT_URL = "/";

  public void parse(String siteUrl) {

    long start = System.currentTimeMillis();
    Set<String> startingSet = ConcurrentHashMap.newKeySet();
    startingSet.add(CURRENT_URL);

    LinkParserTask linkParserTask = new LinkParserTask(
        siteUrl,
        CURRENT_URL,
        startingSet,
        webPageService);

    log.info("New scan started: {}", siteUrl);

    Set<WebPage> result = forkJoinPool.invoke(linkParserTask);
    webPageService.saveAll(result);

    startingSet.clear();

    double time = (System.currentTimeMillis() - start)/1000d;
    log.info("Scan finished. Time: {} sec.", time);
  }
}