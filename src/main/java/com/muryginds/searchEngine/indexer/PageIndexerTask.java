package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.morthology.WrongLanguageException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
@Slf4j
@RequiredArgsConstructor
public class PageIndexerTask implements Callable<Map<String, Integer>> {

  private final WebPage webPage;
  private final Map<String, BigDecimal> fields;
  private final Map<String, Integer> lemmas;
  private final Map<String, BigDecimal> indexes;
  private final LemmaConverter lemmaConverter;

  @Override
  public Map<String, Integer> call() {
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
    return lemmas;
  }

  private void addResults(Map<String, Integer> map, BigDecimal multiplier) {
    map.forEach((key, value) -> {
      lemmas.computeIfAbsent(key, v -> 1);
      indexes.merge(key, BigDecimal.valueOf(value).multiply(multiplier), BigDecimal::add);
    });
  }
}