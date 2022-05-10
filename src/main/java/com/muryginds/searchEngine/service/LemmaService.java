package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.repository.LemmaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LemmaService {

  private final LemmaRepository lemmaRepository;

  public List<Lemma> saveAll(Collection<Lemma> collection) {
    return lemmaRepository.saveAll(collection);
  }

  public List<Lemma> saveAll(Map<String, Integer> map, Site site) {
    Map<String, Lemma> lemmas = findAll(map.keySet(), site).stream()
        .collect(Collectors.toMap(Lemma::getLemma, v -> v));
    return saveAll(
        map.entrySet().stream()
          .map(e -> {
            if (lemmas.containsKey(e.getKey())) {
              Lemma lemma = lemmas.get(e.getKey());
              lemma.setFrequency(lemma.getFrequency() + e.getValue());
              return lemma;
            } else {
              return new Lemma(null, site, e.getKey(), e.getValue());
            }
          }).collect(Collectors.toSet())
    );
  }

  public List<Lemma> findAll(Collection<String> lemmas, Site site) {
    return lemmaRepository.findAllBySiteAndLemmaIn(site, lemmas);
  }
}