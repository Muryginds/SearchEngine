package com.muryginds;

import com.muryginds.searchEngine.parser.SiteParser;

//@SpringBootApplication
public class Engine {

  private static final String URL = "https://tatparts.ru";

  public static void main(String[] args) {
    //SpringApplication.run(Engine.class, args);
    new SiteParser().parse(URL);
  }
}