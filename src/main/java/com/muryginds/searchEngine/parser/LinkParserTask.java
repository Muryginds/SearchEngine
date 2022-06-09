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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkParserTask extends RecursiveTask<Set<WebPage>> {

  private static final String FILE_PATTERN = ".*\\..[^ht].*";
  private static final String STARTING_URL = "/";
  private static final int URL_SCAN_WAIT_TIME = 500;
  private static final int MAX_PAGES_SIZE = 90;
  private final String rootUrl;
  private final String currentUrl;
  private final String fullUrl;
  private final Set<String> scanResults;
  private final Site site;
  private final ParseConfiguration configuration;
  private final WebPageService pageService;
  private Set<WebPage> webPages = new HashSet<>();

  public static LinkParserTask initialise(
      Site site,
      ParseConfiguration configuration,
      WebPageService pageService) {
    var rootUrl = site.getUrl().replaceFirst("www.", "");
    Set<String> scanResults = ConcurrentHashMap.newKeySet();
    scanResults.add(STARTING_URL);
    return new LinkParserTask(
                  rootUrl, STARTING_URL, rootUrl, scanResults, site, configuration, pageService);
  }

  @Override
  protected Set<WebPage> compute() {
    var processors = new ArrayList<LinkParserTask>();
    try {
      var document = Jsoup.connect(fullUrl)
          .userAgent(configuration.getUserAgent())
          .referrer(configuration.getReferrer())
          .get();
      saveWebPage(document);
      for (var parsedLink : parseLinks(document)) {
        if (scanResults.add(parsedLink)) {
          Thread.sleep(URL_SCAN_WAIT_TIME);
          var parserTask = new LinkParserTask(
              rootUrl,
              parsedLink,
              rootUrl + parsedLink,
              scanResults,
              site,
              configuration,
              pageService);
          processors.add(parserTask);
          parserTask.fork();
        }
      }
    } catch (HttpStatusException e) {
      saveWebPage(e);
    } catch (ConnectException e) {
      log.warn("Error connecting: {}", fullUrl);
    } catch (UnknownHostException e) {
      log.warn("Wrong host: {}", fullUrl);
    } catch (IOException | InterruptedException e) {
      log.error("{} : {}", fullUrl, e.getLocalizedMessage());
    }

    for (var parserTask : processors) {
      var pages = parserTask.join();
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
    var links = document.select("a[href^=/]")
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
    var responseCode = doc.connection().response().statusCode();
    addToResults(responseCode, doc.html());
  }

  private void saveWebPage(HttpStatusException e) {
    var responseCode = e.getStatusCode();
    addToResults(responseCode, "");
  }

  private void addToResults(int responseCode, String content) {
    var webPage = new WebPage(null, site, currentUrl, responseCode, content);
    webPages.add(webPage);
  }

  private void saveToDB(Collection<WebPage> collection) {
    pageService.saveAll(collection);
  }
}