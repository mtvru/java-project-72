package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public void index(Context ctx) throws SQLException {
        List<Url> urls = this.urlRepository.findAll();
        UrlsPage page = new UrlsPage(urls);
        page.setPath(ctx.path());
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/index.jte", model("page", page));
    }

    public void show(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = this.urlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url with id = " + id + " not found"));
        UrlPage page = new UrlPage(url);
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
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException | ValidationException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Invalid URL.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_DANGER);
            ctx.redirect(NamedRoutes.homePath());
        } catch (SQLIntegrityConstraintViolationException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Page already exists.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_INFO);
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }
}
