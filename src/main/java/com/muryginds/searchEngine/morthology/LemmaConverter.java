package com.muryginds.searchEngine.morthology;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

@Component
public class LemmaConverter {

  private static final String RUS_PATTERN = "[^А-я]";
  private static final String ENG_PATTERN = "[\\W\\d_]";
  private static final String[] RUS_SPECIAL_WORDS =
      {"ПРЕДЛ", "СОЮЗ", "МЕЖД", "ЧАСТ", "МС"};
  private static final String[] ENG_SPECIAL_WORDS =
      {"PREP", "CONJ", "PN", "ARTICLE", "INT"};

  public Map<String, BigDecimal> convert(String string, LemmaLanguage language)
      throws IOException, WrongLanguageException {
    String pattern;
    LuceneMorphology morphology;
    String[] specialWords;
    switch (language) {
      case RUS -> {
        pattern = RUS_PATTERN;
        morphology = new RussianLuceneMorphology();
        specialWords = RUS_SPECIAL_WORDS;
      }
      case ENG -> {
        pattern = ENG_PATTERN;
        morphology = new EnglishLuceneMorphology();
        specialWords = ENG_SPECIAL_WORDS;
      }
      default -> throw new WrongLanguageException(language);
    }

    return getResults(string, pattern, morphology, specialWords);
  }

  private Map<String, BigDecimal> getResults(
      String string, String pattern, LuceneMorphology luceneMorph, String[] specialWord) {
    String[] array = string
        .toLowerCase(Locale.ROOT)
        .replaceAll(pattern, " ")
        .split(" ");
    return Arrays.stream(array)
        .filter(s -> !s.isEmpty())
        .flatMap(s -> luceneMorph.getNormalForms(s).stream().limit(1))
        .filter(s -> {
          String str = luceneMorph.getMorphInfo(s).stream().limit(1)
              .reduce("", (s1, s2) -> s2);
          return notSpecialWord(str, specialWord);
        })
        .collect(Collectors.toMap(k -> k, v -> BigDecimal.valueOf(1), BigDecimal::add));
  }

  private boolean notSpecialWord(String string, String[] specialWords) {
    return Arrays.stream(specialWords).noneMatch(string::startsWith);
  }
}