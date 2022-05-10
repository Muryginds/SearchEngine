package com.muryginds.searchEngine.morthology;

import java.io.IOException;

public class Morphology {

  public static void main(String[] args) throws IOException, WrongLanguageException {
  String string = "Повторное появление леопарда в Осетии позволяет предположить,"
      + " что леопард постоянно обитает в некоторых районах Северного Кавказа.";
  System.out.println(new LemmaConverter().convert(string, LemmaLanguage.RUS));
  }
}