package com.muryginds.searchEngine;

import com.muryginds.searchEngine.entity.Site;
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
  private final SiteProcessor siteProcessor;

  @Scheduled(fixedDelayString = "P1D")
  public void worker() {

    for (Map<String, String> siteParams : sites) {
      var site = new Site();
      site.setUrl(siteParams.get("url"));
      site.setName(siteParams.get("name"));
      siteProcessor.process(site);
      break;
    }
  }
}