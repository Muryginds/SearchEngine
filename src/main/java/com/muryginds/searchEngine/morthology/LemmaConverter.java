package com.muryginds.searchEngine.morthology;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

public class LemmaConverter {

  private static final String[] LIST_SPECIAL_WORDS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД", "ЧАСТ"};

  public Map<String, Integer> convert(String string) throws IOException {

    LuceneMorphology luceneMorph = new RussianLuceneMorphology();
    String[] array = string
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^А-я]", " ")
        .split(" ");
    return Arrays.stream(array)
        .filter(s -> !s.isEmpty())
        .flatMap(s -> luceneMorph.getNormalForms(s).stream().limit(1))
        .filter(s -> {
          String str = luceneMorph.getMorphInfo(s).stream().limit(1)
              .reduce("", (s1, s2) -> s2);
          return notSpecialWord(str);
        })
        .collect(Collectors.toMap(k -> k, v -> 1, Integer::sum));
  }

  private boolean notSpecialWord(String string) {
    return Arrays.stream(LIST_SPECIAL_WORDS).noneMatch(string::endsWith);
  }
}
