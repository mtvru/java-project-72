package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.HomePage;
import io.javalin.http.Context;

import static io.javalin.rendering.template.TemplateUtil.model;

public class HomeController {
    public void index(Context ctx) {
        HomePage page = new HomePage();
        page.setPath(ctx.path());
        page.setFlash(ctx.consumeSessionAttribute(BasePage.FLASH_KEY));
        page.setFlashType(ctx.consumeSessionAttribute(BasePage.FLASH_TYPE_KEY));
        ctx.render("index.jte", model("page", page));
    }
}
