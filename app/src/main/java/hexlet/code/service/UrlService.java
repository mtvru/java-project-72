package hexlet.code.service;

import hexlet.code.exception.IncorrectUrlException;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class UrlService {
    private final UrlRepository urlRepository;
    private final UrlCheckRepository urlCheckRepository;

    public List<Url> getAllUrlsWithLatestUrlChecks() throws SQLException {
        List<Url> urls = this.urlRepository.findAll();
        if (urls.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, UrlCheck> latestUrlChecks = this.urlCheckRepository.findLatestUrlChecksByUrls(urls);
        for (Url url : urls) {
            UrlCheck latestCheck = latestUrlChecks.get(url.getId());
            url.addUrlCheck(latestCheck);
        }
        return urls;
    }

    public Url getUrlWithLatestUrlChecks(Long urlId) throws SQLException {
        Url url = this.urlRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Url with id = " + urlId + " not found"));
        List<UrlCheck> urlChecks = this.urlCheckRepository.findByUrlId(urlId);
        urlChecks.forEach(url::addUrlCheck);
        return url;
    }

    public Url createUrl(String urlString) throws SQLException {
        try {
            URI uri = new URI(urlString);
            URL urlParam = uri.toURL();
            String port = urlParam.getPort() == -1 ? "" : ":" + urlParam.getPort();
            Url url = new Url(urlParam.getProtocol() + "://" + urlParam.getHost() + port);
            this.urlRepository.save(url);
            return url;
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new IncorrectUrlException();
        }
    }

    public UrlCheck checkUrl(Long urlId) throws SQLException {
        try {
            Url url = this.urlRepository.find(urlId)
                    .orElseThrow(() -> new NotFoundResponse("Url with id = " + urlId + " not found"));
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Integer statusCode = response.getStatus();
            Document doc = Jsoup.parse(response.getBody());
            String title = doc.title();
            Element h1Element = doc.selectFirst("h1");
            String h1 = h1Element != null ? h1Element.text() : "";
            Element descriptionElement = doc.selectFirst("meta[name=description]");
            String description = descriptionElement != null ? descriptionElement.attr("content") : "";
            UrlCheck urlCheck = new UrlCheck(url.getId(), statusCode, title, h1, description);
            this.urlCheckRepository.save(urlCheck);
            return urlCheck;
        } catch (UnirestException e) {
            throw new IncorrectUrlException();
        }
    }
}
