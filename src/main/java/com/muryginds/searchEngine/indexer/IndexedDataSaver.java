package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.Index;
import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.service.IndexService;
import com.muryginds.searchEngine.service.LemmaService;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexedDataSaver {

  private final LemmaService lemmaService;
  private final IndexService indexService;

  public void saveResults(Map<String, Integer> lemmas,
      Map<WebPage, Map<String, BigDecimal>> indexes, Site site) {
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
    var indexSet = indexes.entrySet().stream()
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
  }

  @Transactional
  private void saveToDb(Collection<Lemma> lemmas, Set<Index> indexes) {
    lemmaService.saveAll(lemmas);
    indexService.saveAll(indexes);
  }
}
