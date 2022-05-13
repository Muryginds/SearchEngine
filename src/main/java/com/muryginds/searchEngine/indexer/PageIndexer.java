package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.service.FieldService;
import com.muryginds.searchEngine.service.WebPageService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PageIndexer {

  private static final int MAX_PAGES_SIZE = 60;
  private static final int PAGE_SUCCESSFULLY_PARSED_CODE = 200;
  private static final ExecutorService executorService = Executors.newWorkStealingPool();
  private static Map<String, BigDecimal> fields;
  private final WebPageService webPageService;
  private final LemmaConverter lemmaConverter;
  private final IndexedDataSaver indexedDataSaver;
  private final Map<String, Integer> lemmas = new HashMap<>();
  private final Map<WebPage, Map<String, BigDecimal>> indexes = new ConcurrentHashMap<>();

  @Autowired
  public PageIndexer (
      FieldService fieldService,
      WebPageService webPageService,
      LemmaConverter lemmaConverter,
      IndexedDataSaver indexedDataSaver) {
    this.webPageService = webPageService;
    this.lemmaConverter = lemmaConverter;
    this.indexedDataSaver = indexedDataSaver;
    fields = fieldService.findAll().stream()
        .collect(Collectors.toMap(Field::getSelector, v -> BigDecimal.valueOf(v.getWeight())));
  }

  public void index(Site site) {
    var pageable = PageRequest.ofSize(MAX_PAGES_SIZE);
    var start = System.currentTimeMillis();
    log.info("New indexer started: {}", site.getUrl());
    var pagesPage = webPageService
        .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
    while (pagesPage.hasContent()) {
      var callableTasks = new ArrayList<Callable<Map<String, Integer>>>();

      pagesPage.get().forEach(page -> {
        var task = new PageIndexerTask(
            page,
            fields,
            new HashMap<>(),
            indexes.computeIfAbsent(page, v -> new HashMap<>()),
            lemmaConverter);
        callableTasks.add(task);
      });

      try {
        var futureResults = executorService.invokeAll(callableTasks);
        for (var futureResult : futureResults) {
          futureResult.get().forEach((k, v) -> lemmas.merge(k, v, Integer::sum));
        }
      } catch (InterruptedException | ExecutionException e) {
        log.error("Parsing text failed : {}", e.getLocalizedMessage());
      }

      saveResults(site);

      pageable = pageable.next();
      pagesPage = webPageService
          .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
    }
    var time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Indexer finished. Time: {} sec.", time);
  }

  private void saveResults(Site site) {
    indexedDataSaver.saveResults(lemmas, indexes, site);
    indexes.clear();
    lemmas.clear();
  }
}