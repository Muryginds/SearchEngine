package com.muryginds.searchEngine;

import com.muryginds.searchEngine.parser.LinkParser;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@RequiredArgsConstructor
public class ScheduleController {
  @Setter
  private List<Map<String, String>> sites;
  private final LinkParser linkParser;

  @Scheduled(fixedDelayString = "P1D")
  public void siteScanner() {
    for (Map<String, String> siteParams : sites) {
      linkParser.parse(siteParams.get("url"), siteParams.get("name"));
      break;
    }
  }
}