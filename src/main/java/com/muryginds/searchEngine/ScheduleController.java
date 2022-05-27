package com.muryginds.searchEngine;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.service.FieldService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@RequiredArgsConstructor
public class ScheduleController {
  @Setter
  private List<Map<String, String>> sites;
  private final SiteProcessor siteProcessor;
  private final FieldService fieldService;

  //@Scheduled(fixedDelayString = "P1D")
  public void worker() {
    var fields = fieldService.findAll().stream()
        .collect(Collectors.toMap(Field::getSelector, v -> BigDecimal.valueOf(v.getWeight())));
    for (Map<String, String> siteParams : sites) {
      var site = new Site();
      site.setUrl(siteParams.get("url"));
      site.setName(siteParams.get("name"));
      siteProcessor.process(site, fields);
      break;
    }
  }
}