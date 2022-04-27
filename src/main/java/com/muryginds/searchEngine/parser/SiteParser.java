package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.model.Page;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SiteParser {

  private final ForkJoinPool forkJoinPool = new ForkJoinPool();
  public void parse(String siteUrl) {
    Runtime runtime = Runtime.getRuntime();
    long start = System.currentTimeMillis();
    long usage = runtime.totalMemory() - runtime.freeMemory();

    LinkParserTask linkParserTask =
        new LinkParserTask(siteUrl);
    log.info("New scan started: {}", siteUrl);

    forkJoinPool.invoke(linkParserTask);

    Map<String, Page> map = linkParserTask.getResults();
    double time = (System.currentTimeMillis() - start)/1000d;
    usage = (runtime.totalMemory() - runtime.freeMemory() - usage)/1024/1024;
    StringBuilder builder = new StringBuilder();
      builder.append("Scan finished. Total links: ")
          .append(map.size())
          .append(". Time: ")
          .append(time)
          .append(" sec. Memory used: ")
          .append(usage)
          .append(" mb.");
    log.info(builder.toString());
  }
}