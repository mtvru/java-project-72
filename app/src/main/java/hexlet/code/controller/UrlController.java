package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
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
        try {
            List<Url> urls = this.urlService.getAllUrlsWithLatestUrlChecks();
            UrlsPage page = new UrlsPage(urls);
            page.setPath(ctx.path());
            page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
            page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            this.setFlash(ctx, FlashMessage.CONNECTION_ERROR);
            ctx.redirect(NamedRoutes.homePath());
        }
    }

    public void show(Context ctx) {
        try {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Url url = this.urlService.getUrlWithLatestUrlChecks(id);
            UrlPage page = new UrlPage(url);
            page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
            page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
            ctx.render("urls/show.jte", model("page", page));
        } catch (SQLException e) {
            this.setFlash(ctx, FlashMessage.CONNECTION_ERROR);
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public void create(Context ctx) {
        String urlFormParam = ctx.formParamAsClass("url", String.class).get();
        try {
            this.urlService.createUrl(urlFormParam);
            this.setFlash(ctx, FlashMessage.PAGE_ADDED);
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (IncorrectUrlException e) {
            this.setFlash(ctx, FlashMessage.INCORRECT_URL);
            ctx.redirect(NamedRoutes.homePath());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                this.setFlash(ctx, FlashMessage.PAGE_ALREADY_EXISTS);
            } else {
                this.setFlash(ctx, FlashMessage.CONNECTION_ERROR);
            }
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public void check(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        try {
            this.urlService.checkUrl(id);
            this.setFlash(ctx, FlashMessage.PAGE_CHECKED);
        } catch (IncorrectUrlException e) {
            this.setFlash(ctx, FlashMessage.INCORRECT_URL);
        } catch (SQLException e) {
            this.setFlash(ctx, FlashMessage.CONNECTION_ERROR);
        }
        ctx.redirect(NamedRoutes.urlPath(id));
    }
}
