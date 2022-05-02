package com.muryginds.searchEngine.parser;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ParseConfiguration {

  private final String userAgent;
  private final String referrer;

  public ParseConfiguration(
      @Value("${parsing.user-agent:'SiteParsingBot'}") String userAgent,
      @Value("${parsing.referrer:'https://github.com/'}") String referrer) {
    this.userAgent = userAgent;
    this.referrer = referrer;
  }
}