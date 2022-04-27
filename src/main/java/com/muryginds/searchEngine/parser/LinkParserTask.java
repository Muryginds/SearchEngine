package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.model.Page;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public class LinkParserTask extends RecursiveAction {

  private static final String FILE_PATTERN = ".*\\..[^ht].*";

  private final String rootUrl;
  private final String currentUrl;
  private final String fullUrl;
  private final Set<String> scanResults;
  private final Set<Page> finalResults;

  public LinkParserTask(String siteUrl) {
    this(siteUrl, "/", ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
  }
  public LinkParserTask(
      String rootUrl,
      String currentUrl,
      Set<Page> finalResults,
      Set<String> scanResults) {
    this.rootUrl = rootUrl;
    this.currentUrl = currentUrl;
    this.fullUrl = rootUrl + currentUrl;
    this.finalResults = finalResults;
    this.scanResults = scanResults;
  }

  @Override
  protected void compute() {

    List<LinkParserTask> processors = new ArrayList<>();
    try {
      Document doc = Jsoup.connect(fullUrl).get();
      savePage(doc);
      parseLinks(doc).forEach(element -> {
        if (scanResults.add(element)) {
          try {
            Thread.sleep(150);
          } catch (InterruptedException e) {
            log.error(fullUrl + ": " + e.getLocalizedMessage());
          }
          LinkParserTask processor =
              new LinkParserTask(rootUrl, element, finalResults, scanResults);
          processor.fork();
          processors.add(processor);
        }
      });
    } catch (HttpStatusException e) {
      savePage(e);
    } catch (ConnectException e) {
      log.info("Error connecting: {}", fullUrl);
    } catch (UnknownHostException e) {
      log.warn("Wrong host: {}", fullUrl);
    } catch (IOException e) {
      log.error("{} : {}", fullUrl, e.getLocalizedMessage());
      //throw new RuntimeException(e);
    }

    for (LinkParserTask result : processors) {
      result.join();
    }
  }

  public Set<String> parseLinks(Document document) {
    Set<String> links = document.select("a[href^=/]")
          .stream()
          .map(e -> e.attr("href"))
          .filter(s -> !s.contains("?") && !s.contains("#") && !s.matches(FILE_PATTERN))
          .collect(Collectors.toSet());
    document.select("a[href^=" + fullUrl + "]")
          .stream()
          .map(e -> e.attr("href").substring(fullUrl.length() - 1))
          .filter(s -> !s.contains("?") && !s.contains("#") && !s.matches(FILE_PATTERN))
          .filter(s -> s.contains("/"))
          .forEach(links::add);
    return links;
  }

  private void savePage(Document doc) {
    int responseCode = doc.connection().response().statusCode();
    addPageToResults(responseCode, doc.html());
  }

  private void savePage(HttpStatusException e) {
    int responseCode = e.getStatusCode();
    addPageToResults(responseCode, "");
  }

  private void addPageToResults(int responseCode, String content) {
    Page page = new Page(null, currentUrl, responseCode, content);
    finalResults.add(page);
  }

  public Set<Page> getResults() {
    return finalResults;
  }
}