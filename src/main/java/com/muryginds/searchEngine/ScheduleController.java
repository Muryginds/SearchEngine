package com.muryginds.searchEngine;

import com.muryginds.searchEngine.parser.LinkParser;
import com.muryginds.searchEngine.parser.ParseConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleController {

  private static final String URL = "https://skillbox.ru";
  private static final String SITE_NAME = "skillbox.ru";
  private final LinkParser linkParser;
  private final ParseConfiguration configuration;

  @Scheduled(fixedDelay = 100)
  public void siteScanner() {
    linkParser.parse(URL, SITE_NAME, configuration);
  }
}