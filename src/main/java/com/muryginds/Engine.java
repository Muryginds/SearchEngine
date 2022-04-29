package com.muryginds;

import com.muryginds.searchEngine.parser.LinkParser;
import com.muryginds.searchEngine.parser.ParseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Engine {

  private static LinkParser linkParser;
  private static ParseConfiguration configuration;
  private static final String URL = "https://volochek.life";
  private static final String SITE_NAME = "volochek.life";

  public static void main(String[] args) {
    SpringApplication.run(Engine.class, args);
    linkParser.parse(URL, SITE_NAME, configuration);
  }

  @Autowired
  public void setLinkParser(LinkParser linkParser) {
    Engine.linkParser = linkParser;
  }

  @Autowired
  public void setParseConfiguration(ParseConfiguration configuration) {
    Engine.configuration = configuration;
  }
}