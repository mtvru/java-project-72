package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.service.UrlService;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

@AllArgsConstructor
public class UrlController {
    private final UrlService urlService;

    public void index(Context ctx) throws SQLException {
        List<Url> urls = this.urlService.getAllUrlsWithLatestUrlChecks();
        UrlsPage page = new UrlsPage(urls);
        page.setPath(ctx.path());
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/index.jte", model("page", page));
    }

    public void show(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = this.urlService.getUrlWithLatestUrlChecks(id);
        UrlPage page = new UrlPage(url);
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/show.jte", model("page", page));
    }

    public void create(Context ctx) throws SQLException {
        try {
            String urlFormParam = ctx.formParamAsClass("url", String.class).get();
            this.urlService.createUrl(urlFormParam);
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Page has been added successfully.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_SUCCESS);
        } catch (IllegalArgumentException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Incorrect URL");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_DANGER);
            ctx.redirect(NamedRoutes.homePath());
            return;
        } catch (SQLIntegrityConstraintViolationException e) {
            ctx.sessionAttribute(BasePage.FLASH_KEY, "Page already exists.");
            ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_INFO);
        }
        ctx.redirect(NamedRoutes.urlsPath());
    }

    public void check(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        this.urlService.checkUrl(id);
        ctx.sessionAttribute(BasePage.FLASH_KEY, "Page has been checked successfully.");
        ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, BasePage.FLASH_TYPE_SUCCESS);
        ctx.redirect(NamedRoutes.urlPath(id));
    }
}
