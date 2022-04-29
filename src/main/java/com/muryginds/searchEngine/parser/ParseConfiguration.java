package com.muryginds.searchEngine.parser;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ParseConfiguration {

  @Value("${parsing.user-agent:'SiteParsingBot'}")
  private String userAgent;

  @Value("${parsing.referrer:'https://github.com/'}")
  private String referrer;
}