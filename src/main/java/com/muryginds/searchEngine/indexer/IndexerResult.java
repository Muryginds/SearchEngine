package com.muryginds.searchEngine.indexer;

import com.muryginds.searchEngine.entity.WebPage;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IndexerResult {
  private final WebPage webPage;
  private final Map<String, Integer> lemmas;
  private final Map<String, BigDecimal> indexes;
}