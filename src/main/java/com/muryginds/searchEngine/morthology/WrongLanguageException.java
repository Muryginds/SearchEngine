package com.muryginds.searchEngine.morthology;

public class WrongLanguageException extends Exception {

  public WrongLanguageException(LemmaLanguage language) {
    super(language + " language not found");
  }
}