package com.muryginds.searchEngine.service;

import com.muryginds.searchEngine.entity.Index;
import com.muryginds.searchEngine.repository.IndexRepository;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {

  private final IndexRepository indexRepository;

  public void saveAll(Collection<Index> collection) {
    indexRepository.saveAll(collection);
  }
}