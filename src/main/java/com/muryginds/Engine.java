package com.muryginds;

import com.muryginds.searchEngine.parser.LinkParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Engine {

  private static final String URL = "https://skillbox.ru";

  public static void main(String[] args) {
    SpringApplication.run(Engine.class, args);
    new LinkParser().parse(URL);
  }
}