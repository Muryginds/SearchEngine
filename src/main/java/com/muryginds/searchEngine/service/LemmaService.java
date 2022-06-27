package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.repository.LemmaRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LemmaService {

  private final LemmaRepository lemmaRepository;

  public void saveAll(Collection<Lemma> collection) {
    lemmaRepository.saveAll(collection);
  }

  public List<Lemma> findAll(Collection<String> lemmas, Site site) {
    return lemmaRepository.findAllBySiteAndLemmaIn(site, lemmas);
  }
}