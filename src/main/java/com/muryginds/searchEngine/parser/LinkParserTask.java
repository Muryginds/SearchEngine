package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.model.WebPage;
import com.muryginds.searchEngine.service.WebPageService;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public class LinkParserTask extends RecursiveTask<Set<WebPage>> {

  private static final String FILE_PATTERN = ".*\\..[^ht].*";
  private static final int URL_SCAN_WAIT_TIME = 150;
  private static final int MAX_PAGES_SIZE = 200;
  private final WebPageService webPageService;
  private final String rootUrl;
  private final String currentUrl;
  private final String fullUrl;
  private final Set<String> scanResults;
  private Set<WebPage> webPages = new HashSet<>();

  public LinkParserTask(
      String rootUrl,
      String currentUrl,
      Set<String> scanResults,
      WebPageService webPageService) {
    this.rootUrl = rootUrl;
    this.currentUrl = currentUrl;
    this.fullUrl = rootUrl + currentUrl;
    this.scanResults = scanResults;
    this.webPageService = webPageService;
  }

  @Override
  protected Set<WebPage> compute() {

    List<LinkParserTask> processors = new ArrayList<>();
    try {
      Document doc = Jsoup.connect(fullUrl).get();
      saveWebPage(doc);
      parseLinks(doc).forEach(element -> {
        if (scanResults.add(element)) {
          try {
            Thread.sleep(URL_SCAN_WAIT_TIME);
          } catch (InterruptedException e) {
            log.error(fullUrl + ": " + e.getLocalizedMessage());
          }
          LinkParserTask processor =
              new LinkParserTask(rootUrl, element, scanResults, webPageService);
          processor.fork();
          processors.add(processor);
        }
      });
    } catch (HttpStatusException e) {
      saveWebPage(e);
    } catch (ConnectException e) {
      log.info("Error connecting: {}", fullUrl);
    } catch (UnknownHostException e) {
      log.warn("Wrong host: {}", fullUrl);
    } catch (IOException e) {
      log.error("{}: {}", fullUrl, e.getLocalizedMessage());
    }

    for (LinkParserTask result : processors) {
      Set<WebPage> pages = result.join();
      if (pages.size() > MAX_PAGES_SIZE) {
        saveToDB(pages);
        pages = Collections.emptySet();
      }

      webPages.addAll(pages);

      if (webPages.size() > MAX_PAGES_SIZE) {
        saveToDB(webPages);
        webPages = new HashSet<>();
      }
    }

    return webPages;
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

  private void saveWebPage(Document doc) {
    int responseCode = doc.connection().response().statusCode();
    addToResults(responseCode, doc.html());
  }

  private void saveWebPage(HttpStatusException e) {
    int responseCode = e.getStatusCode();
    addToResults(responseCode, "");
  }

  private void addToResults(int responseCode, String content) {
    WebPage webPage = new WebPage(null, currentUrl, responseCode, content);
    webPages.add(webPage);
  }

  private void saveToDB(Collection<WebPage> collection) {
    RecursiveAction action = new RecursiveAction() {
      @Override
      protected void compute() {
        webPageService.saveAll(collection);
      }
    };
    action.fork();
  }
}