package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.ValidationException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    private final UrlRepository urlRepository;
    private final UrlCheckRepository urlCheckRepository;

    public UrlController(UrlRepository urlRepository, UrlCheckRepository urlCheckRepository) {
        this.urlRepository = urlRepository;
        this.urlCheckRepository = urlCheckRepository;
    }

    public void index(Context ctx) throws SQLException {
        List<Url> urls = this.urlRepository.findAll();
        Map<Long, UrlCheck> latestUrlChecks = this.urlCheckRepository.findLatestUrlChecksByUrls(urls);
        UrlsPage page = new UrlsPage(urls, latestUrlChecks);
        page.setPath(ctx.path());
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/index.jte", model("page", page));
    }

    public void show(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = this.urlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url with id = " + id + " not found"));
        List<UrlCheck> urlChecks = this.urlCheckRepository.findByUrlId(id);
        UrlPage page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/show.jte", model("page", page));
    }

    public void create(Context ctx) throws SQLException {
        try {
            String urlFormParam = ctx.formParamAsClass("url", String.class)
                    .check(value -> value != null && !value.isBlank(), "Invalid URL.")
                    .get();
            URI uri = new URI(urlFormParam);
            URL urlParam = uri.toURL();
            Url url = new Url(urlParam.getProtocol() + "://" + urlParam.getHost());
            this.urlRepository.save(url);
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Page has been added successfully.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_SUCCESS);
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException | ValidationException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Invalid URL.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_DANGER);
        } catch (SQLIntegrityConstraintViolationException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Page already exists.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_INFO);
        }
        ctx.redirect(NamedRoutes.homePath());
    }

    public void check(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = this.urlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url with id = " + id + " not found"));
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
        ctx.sessionAttribute(BasePage.FLASH_KEY, "Page has been checked successfully.");
        ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_SUCCESS);
        ctx.redirect(NamedRoutes.urlPath(id));
    }
}
