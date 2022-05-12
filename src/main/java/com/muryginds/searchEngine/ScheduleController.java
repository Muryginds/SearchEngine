package com.muryginds.searchEngine;

import com.muryginds.searchEngine.service.SiteProcessingService;
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
  private final SiteProcessingService siteProcessingService;

  @Scheduled(fixedDelayString = "P1D")
  public void worker() {
    for (Map<String, String> siteParams : sites) {
      siteProcessingService.process(siteParams.get("url"), siteParams.get("name"));
      break;
    }
  }
}