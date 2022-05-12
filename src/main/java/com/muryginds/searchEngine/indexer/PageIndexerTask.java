package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.morthology.LemmaLanguage;
import com.muryginds.searchEngine.morthology.WrongLanguageException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
@Slf4j
@RequiredArgsConstructor
public class PageIndexerTask extends RecursiveAction {

  private final WebPage webPage;
  private final Map<String, BigDecimal> fields;
  private final Map<String, Integer> lemmas;
  private final Map<String, BigDecimal> indexes;
  private final LemmaConverter lemmaConverter;

  @Override
  protected void compute() {
    Document doc = Jsoup.parse(webPage.getContent());
    for (Map.Entry<String, BigDecimal> field : fields.entrySet()) {
      String text = doc.getElementsByTag(field.getKey()).text();
      try {
        addResults(lemmaConverter.convert(text, LemmaLanguage.RUS), field.getValue());
        addResults(lemmaConverter.convert(text, LemmaLanguage.ENG), field.getValue());
      } catch (IOException | WrongLanguageException e) {
        log.error("Site {} : {} : {}",
            webPage.getSite().getUrl() + webPage.getPath(), field.getKey(), e.getLocalizedMessage());
      }
    }
  }

  private void addResults(Map<String, BigDecimal> map, BigDecimal multiplier) {
    map.forEach((key, value) -> {
      lemmas.merge(key, 1, (v1, v2) -> v1 + 1);
      indexes.merge(key, value.multiply(multiplier), BigDecimal::add);
    });
  }
}