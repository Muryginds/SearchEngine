package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.service.FieldService;
import com.muryginds.searchEngine.service.IndexService;
import com.muryginds.searchEngine.service.LemmaService;
import com.muryginds.searchEngine.service.SiteService;
import com.muryginds.searchEngine.service.WebPageService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageIndexer {

  private static final int MAX_PAGES_SIZE = 300;
  private static final int PAGE_SUCCESSFULLY_PARSED_CODE = 200;
  private final FieldService fieldService;
  private final WebPageService webPageService;
  private final SiteService siteService;
  private final LemmaConverter lemmaConverter;
  private final IndexService indexService;
  private final LemmaService lemmaService;
  private final Map<String, Integer> lemmas = new ConcurrentHashMap<>();
  private final Map<WebPage, Map<String, BigDecimal>> indexes = new ConcurrentHashMap<>();

  public void index() {

    List<Site> sites = siteService.findParsed();
    Map<String, Double> fields = fieldService.findAll().stream()
        .collect(Collectors.toMap(Field::getSelector, Field::getWeight));
    Pageable pageable = PageRequest.ofSize(MAX_PAGES_SIZE);
    for (Site site : sites) {
      long start = System.currentTimeMillis();
      log.info("New indexer started: {}", site.getUrl());
      Page<WebPage> webPages = webPageService
          .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
      while (webPages.getContent().size() > 0) {
        List<RecursiveAction> actions = new ArrayList<>();
        lemmas.clear();
        indexes.clear();
        webPages.get().forEach(p -> {
          RecursiveAction action = new PageIndexerTask(
              p.getContent(),
              site.getUrl() + p.getPath(),
              fields,
              lemmas,
              indexes.computeIfAbsent(p, v -> new HashMap<>()),
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
        webPages = webPageService
            .getPageParsedWithCode(site,PAGE_SUCCESSFULLY_PARSED_CODE, pageable.next());
      }
      double time = (System.currentTimeMillis() - start)/1000d;
      log.info("Indexer finished. Time: {} sec.", time);
    }
  }
}