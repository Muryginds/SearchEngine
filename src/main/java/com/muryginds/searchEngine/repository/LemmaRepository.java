package com.muryginds.searchEngine.repository;

import com.muryginds.searchEngine.entity.Lemma;
import com.muryginds.searchEngine.entity.Site;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
  List<Lemma> findAllBySiteAndLemmaIn(Site site, Collection<String> lemmas);
}