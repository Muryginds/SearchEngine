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

  public void parse(String siteUrl) {

    long start = System.currentTimeMillis();

    LinkParserTask linkParserTask = new LinkParserTask(
        siteUrl,
        "/",
        ConcurrentHashMap.newKeySet(),
        webPageService);

    log.info("New scan started: {}", siteUrl);

    Set<WebPage> result = forkJoinPool.invoke(linkParserTask);
    webPageService.saveAll(result);

    double time = (System.currentTimeMillis() - start)/1000d;
    StringBuilder builder = new StringBuilder();
      builder.append("Scan finished. Time: ")
          .append(time)
          .append(" sec.");
    log.info(builder.toString());
  }
}