package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.service.FieldService;
import com.muryginds.searchEngine.service.IndexService;
import com.muryginds.searchEngine.service.LemmaService;
import com.muryginds.searchEngine.service.WebPageService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PageIndexer {

  private static final int MAX_PAGES_SIZE = 600;
  private static final int PAGE_SUCCESSFULLY_PARSED_CODE = 200;
  private static Map<String, BigDecimal> fields;
  private final WebPageService webPageService;
  private final LemmaConverter lemmaConverter;
  private final IndexService indexService;
  private final LemmaService lemmaService;
  private final Map<String, Integer> lemmas = new ConcurrentHashMap<>();
  private final Map<WebPage, Map<String, BigDecimal>> indexes = new ConcurrentHashMap<>();

  @Autowired
  public PageIndexer (
      FieldService fieldService,
      WebPageService webPageService,
      LemmaConverter lemmaConverter,
      IndexService indexService,
      LemmaService lemmaService) {
    this.webPageService = webPageService;
    this.lemmaConverter = lemmaConverter;
    this.indexService = indexService;
    this.lemmaService = lemmaService;
    fields = fieldService.findAll().stream()
        .collect(Collectors.toMap(Field::getSelector, v -> BigDecimal.valueOf(v.getWeight())));
  }

  public void index(Site site) {
    Pageable pageable = PageRequest.ofSize(MAX_PAGES_SIZE);
    long start = System.currentTimeMillis();
    log.info("New indexer started: {}", site.getUrl());
    Page<WebPage> webPages = webPageService
        .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
    while (webPages.hasContent()) {
      List<RecursiveAction> actions = new ArrayList<>();
      lemmas.clear();
      indexes.clear();
      webPages.get().forEach(page -> {
        RecursiveAction action = new PageIndexerTask(
            page,
            fields,
            lemmas,
            indexes.computeIfAbsent(page, v -> new HashMap<>()),
            lemmaConverter);
        actions.add(action);
        action.fork();
      });
      for (RecursiveAction action : actions) {
        action.join();
      }

      Map<String, Lemma> lemmasSet =
          lemmaService.saveAll(lemmas, site).stream()
              .collect(Collectors.toMap(Lemma::getLemma, v -> v));
      indexService.saveAll(indexes, lemmasSet);
      pageable = pageable.next();
      webPages = webPageService
          .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
    }
    double time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Indexer finished. Time: {} sec.", time);
  }
}