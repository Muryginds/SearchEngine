package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Index;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageIndexer {

  private static final int MAX_PAGES_SIZE = 5;
  private static final int PAGE_SUCCESSFULLY_PARSED_CODE = 200;
  private static final ExecutorService executorService = Executors.newWorkStealingPool();
  private final WebPageService webPageService;
  private final LemmaConverter lemmaConverter;
  private final LemmaService lemmaService;
  private final FieldService fieldService;
  private final IndexService indexService;
  private Map<String, Integer> lemmas;
  private Map<WebPage, Map<String, BigDecimal>> webPageIndexes;

  public void index(Site site) {
    lemmas = new HashMap<>();
    webPageIndexes = new ConcurrentHashMap<>();
    var pageable = PageRequest.ofSize(MAX_PAGES_SIZE);
    var start = System.currentTimeMillis();
    log.info("New indexer started: {}", site.getUrl());
    var pagesPage = getPages(site, pageable);
    while (pagesPage.hasContent()) {
      var indexingTasks = new ArrayList<Callable<IndexerResult>>();

      pagesPage.get().forEach(page -> {
        var task = new PageIndexerTask(page, lemmaConverter, fieldService);
        indexingTasks.add(task);
      });

      try {
        var futureResults = executorService.invokeAll(indexingTasks);
        for (var futureResult : futureResults) {
          var indexerResult = futureResult.get();
          indexerResult.getLemmas().forEach((k, v) -> lemmas.merge(k, v, Integer::sum));
          webPageIndexes.put(indexerResult.getWebPage(), indexerResult.getIndexes());
        }
      } catch (InterruptedException | ExecutionException e) {
        log.error("Parsing text failed : {}", e.getLocalizedMessage());
      }

      saveResults(site);

      pageable = pageable.next();
      pagesPage = getPages(site, pageable);
    }
    var time = (System.currentTimeMillis() - start) / 1000d;
    log.info("Indexer finished. Time: {} sec.", time);
  }

  private void saveResults(Site site) {
    var lemmasMap = lemmaService.findAll(lemmas.keySet(), site).stream()
        .collect(Collectors.toMap(Lemma::getLemma, v -> v));
    lemmas.forEach((k, v) ->
        lemmasMap.merge(
            k,
            new Lemma(null, site, k, v),
            (lemma, lemma2) -> {
              lemma.setFrequency(lemma.getFrequency() + v);
              return lemma;
            }
        )
    );
    var indexSet = webPageIndexes.entrySet().stream()
        .flatMap(e -> e.getValue().entrySet().stream()
            .map(i -> new Index(
                    null,
                    e.getKey(),
                    lemmasMap.get(i.getKey()),
                    i.getValue().doubleValue()
                )
            )
        ).collect(Collectors.toSet());

    saveToDb(lemmasMap.values(), indexSet);

    webPageIndexes.clear();
    lemmas.clear();
  }

  private Page<WebPage> getPages(Site site, PageRequest pageable) {
    return webPageService
        .getPageParsedWithCode(site, PAGE_SUCCESSFULLY_PARSED_CODE, pageable);
  }

  private void saveToDb(Collection<Lemma> lemmas, Set<Index> indexes) {
    lemmaService.saveAll(lemmas);
    indexService.saveAll(indexes);
  }
}