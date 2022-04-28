package com.muryginds;

import com.muryginds.searchEngine.parser.LinkParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Engine {

  private static LinkParser linkParser;
  private static final String URL = "https://skillbox.ru";
  public static void main(String[] args) {
    SpringApplication.run(Engine.class, args);
    linkParser.parse(URL);
  }
  @Autowired
  public void setLinkParser(LinkParser linkParser) {
    Engine.linkParser = linkParser;
  }
}