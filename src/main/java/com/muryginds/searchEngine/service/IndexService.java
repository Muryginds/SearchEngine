package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Index;
import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.repository.IndexRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {

  private final IndexRepository indexRepository;

  public void saveAll(Collection<Index> collection) {
    indexRepository.saveAll(collection);
  }

  public void saveAll(Map<WebPage, Map<String, BigDecimal>> map, Map<String, Lemma> lemmas) {
    saveAll(map.entrySet().stream()
      .flatMap(e -> e.getValue().entrySet().stream()
          .map(i -> new Index(
                null,
                  e.getKey(),
                  lemmas.get(i.getKey()),
                  i.getValue().doubleValue()
              )
          )
      ).collect(Collectors.toSet()));
  }
}