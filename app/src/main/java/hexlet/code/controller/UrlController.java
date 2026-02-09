package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.exception.DbConnectionException;
import hexlet.code.exception.IncorrectUrlException;
import hexlet.code.model.Url;
import hexlet.code.service.UrlService;
import hexlet.code.util.FlashMessage;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

@AllArgsConstructor
public final class UrlController extends BaseController {
    private final UrlService urlService;

    public void index(Context ctx) {
        List<Url> urls = this.urlService.getAllUrlsWithLatestUrlChecks();
        UrlsPage page = new UrlsPage(urls);
        page.setPath(ctx.path());
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/index.jte", model("page", page));
    }

    public void show(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = this.urlService.getUrlWithLatestUrlChecks(id);
        UrlPage page = new UrlPage(url);
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("urls/show.jte", model("page", page));
    }

    public void create(Context ctx) {
        try {
            String urlFormParam = ctx.formParamAsClass("url", String.class).get();
            this.urlService.createUrl(urlFormParam);
            this.setFlash(ctx, FlashMessage.PAGE_ADDED);
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (IncorrectUrlException e) {
            this.setFlash(ctx, FlashMessage.INCORRECT_URL);
            ctx.redirect(NamedRoutes.homePath());
        } catch (DbConnectionException e) {
            SQLException sqlEx = e.getCause() instanceof SQLException ? (SQLException) e.getCause() : null;
            if (sqlEx != null && "23505".equals(sqlEx.getSQLState())) {
                this.setFlash(ctx, FlashMessage.PAGE_ALREADY_EXISTS);
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }
            throw e;
        }
    }

    public void check(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        try {
            this.urlService.checkUrl(id);
            this.setFlash(ctx, FlashMessage.PAGE_CHECKED);
        } catch (IncorrectUrlException e) {
            this.setFlash(ctx, FlashMessage.INCORRECT_URL);
        }
        ctx.redirect(NamedRoutes.urlPath(id));
    }
}
