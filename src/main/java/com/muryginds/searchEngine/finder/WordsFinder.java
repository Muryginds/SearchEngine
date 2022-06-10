package com.muryginds.searchEngine.finder;

import com.muryginds.searchEngine.entity.WebPage;
import com.muryginds.searchEngine.morthology.LemmaConverter;
import com.muryginds.searchEngine.morthology.WrongLanguageException;
import com.muryginds.searchEngine.service.WebPageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WordsFinder {

  private final WebPageService webPageService;
  private final LemmaConverter lemmaConverter;

  public List<WebPage> search(String search) {
    List<WebPage> pages = new ArrayList<>();
    try {
      var convertedWords = lemmaConverter.convert(search);
      pages = webPageService.findByLemmas(convertedWords.keySet());
    } catch (WrongLanguageException | IOException e) {
      log.warn("Parsing search text failed : {} : {}", search, e.getLocalizedMessage());
    }
    return pages;
  }
}