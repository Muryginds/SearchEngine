package com.muryginds.searchEngine.parser;

import com.muryginds.searchEngine.entity.Site;
import com.muryginds.searchEngine.entity.WebPage;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
@RequiredArgsConstructor
public class LinkParserTask extends RecursiveTask<Set<WebPage>> {

  private static final String FILE_PATTERN = ".*\\..[^ht].*";
  private static final String STARTING_URL = "/";
  private static final int URL_SCAN_WAIT_TIME = 500;
  private static final int MAX_PAGES_SIZE = 90;
  private static String rootUrl;
  private final String currentUrl;
  private final Set<String> scanResults;
  private final ParseConfiguration configuration;
  private final Site site;
  private final WebPageService webPageService;
  private String fullUrl;
  private Set<WebPage> webPages = new HashSet<>();

  public LinkParserTask (
      ParseConfiguration parseConfiguration,
      Site site,
      WebPageService webPageService) {
    this(
        STARTING_URL,
        ConcurrentHashMap.newKeySet(),
        parseConfiguration,
        site,
        webPageService);
    scanResults.clear();
    scanResults.add(STARTING_URL);
    rootUrl = site.getUrl().replaceFirst("www.", "");
  }

  @Override
  protected Set<WebPage> compute() {

    List<LinkParserTask> processors = new ArrayList<>();
    try {
      fullUrl = rootUrl + (currentUrl.equals("/") ? "" : currentUrl);
      Document doc = Jsoup.connect(fullUrl)
          .userAgent(configuration.getUserAgent())
          .referrer(configuration.getReferrer())
          .get();
      saveWebPage(doc);
      parseLinks(doc).forEach(element -> {
        if (scanResults.add(element)) {
          try {
            Thread.sleep(URL_SCAN_WAIT_TIME);
          } catch (InterruptedException e) {
            log.error("{}: {}", fullUrl, e.getLocalizedMessage());
          }
          LinkParserTask processor = new LinkParserTask(
              element, scanResults, configuration, site, webPageService);
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
      log.error("{} : {}", e.getLocalizedMessage(), fullUrl);
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
          .map(s -> !s.equals("/") && s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
          .collect(Collectors.toSet());
    document.select("a[href^=" + rootUrl + "]")
          .stream()
          .map(e -> e.attr("href"))
          .filter(s -> s.startsWith(rootUrl))
          .map(s -> s.substring(rootUrl.length()))
          .filter(s -> !s.contains("?") && !s.contains("#") && !s.matches(FILE_PATTERN))
          .map(s -> !s.equals("/") && s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
          .forEach(links::add);
    return links.stream()
        .filter(s -> !s.isBlank())
        .filter(s -> !scanResults.contains(s))
        .collect(Collectors.toSet());
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
    WebPage webPage = new WebPage(null, site, currentUrl, responseCode, content);
    webPages.add(webPage);
  }

  private void saveToDB(Collection<WebPage> collection) {
    RecursiveAction action = new RecursiveAction() {
      @Override
      protected void compute() {
        webPageService.saveAll(collection);
      }
    };
    action.invoke();
  }
}