package com.muryginds.searchEngine.indexer;

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

  private final String text;
  private final String siteUrl;
  private final Map<String, Double> fields;
  private final Map<String, Integer> lemmas;
  private final Map<String, BigDecimal> indexes;
  private final LemmaConverter lemmaConverter;

  @Override
  protected void compute() {
    Document doc = Jsoup.parse(text);
    for (Map.Entry<String, Double> field : fields.entrySet()) {
      String text = doc.getElementsByTag(field.getKey()).text();
      BigDecimal multiplier = BigDecimal.valueOf(field.getValue());
      try {
        addResults(lemmaConverter.convert(text, LemmaLanguage.RUS), multiplier);
        addResults(lemmaConverter.convert(text, LemmaLanguage.ENG), multiplier);
      } catch (IOException | WrongLanguageException e) {
        log.error("Site {} : {} : {}",
            siteUrl, field.getKey(), e.getLocalizedMessage());
      }
    }
  }

  private void addResults(Map<String, Integer> map, BigDecimal multiplier) {
    map.forEach((key, value) -> {
      lemmas.merge(key, 1, Integer::sum);
      indexes.merge(key, BigDecimal.valueOf(value).multiply(multiplier), BigDecimal::add);
    });
  }
}