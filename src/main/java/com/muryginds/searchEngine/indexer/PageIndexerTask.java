package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Field;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.morthology.WrongLanguageException;
import com.muryginds.searchEngine.service.FieldService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
@Slf4j
@RequiredArgsConstructor
public class PageIndexerTask implements Callable<IndexerResult> {

  private static Map<String, BigDecimal> fields;
  private final WebPage webPage;
  private final LemmaConverter lemmaConverter;
  private final FieldService fieldService;
  private final Map<String, BigDecimal> indexes = new HashMap<>();
  private final Map<String, Integer> lemmas  = new HashMap<>();

  @Override
  public IndexerResult call() {
    fields = fields == null ? setFields() : fields;
    Document doc = Jsoup.parse(webPage.getContent());
    for (Map.Entry<String, BigDecimal> field : fields.entrySet()) {
      var text = doc.getElementsByTag(field.getKey()).text();
      try {
        addResults(lemmaConverter.convert(text), field.getValue());
      } catch (IOException | WrongLanguageException e) {
        log.error("Site {} : {} : {}",
            webPage.getSite().getUrl() + webPage.getPath(), field.getKey(), e.getLocalizedMessage());
      }
    }
    return new IndexerResult(webPage, lemmas, indexes);
  }

  private void addResults(Map<String, Integer> map, BigDecimal multiplier) {
    map.forEach((key, value) -> {
      lemmas.computeIfAbsent(key, v -> 1);
      indexes.merge(key, BigDecimal.valueOf(value).multiply(multiplier), BigDecimal::add);
    });
  }

  private Map<String, BigDecimal> setFields() {
    return fieldService.findAll().stream()
        .collect(Collectors.toMap(Field::getSelector, v -> BigDecimal.valueOf(v.getWeight())));
  }
}