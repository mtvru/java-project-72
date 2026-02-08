package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.util.FlashMessage;
import io.javalin.http.Context;

public class BaseController {
    /**
     * @param ctx Context
     * @param flash FlashMessage
     */
    protected void setFlash(Context ctx, FlashMessage flash) {
        ctx.sessionAttribute(BasePage.FLASH_KEY, flash.getMessage());
        ctx.sessionAttribute(BasePage.FLASH_TYPE_KEY, flash.getType());
    }
}
