package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.HomeController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.service.UrlService;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;

public class JavalinFactory {
    private final static String TEMPLATES_NAME = "templates";
    private static final Logger log = LoggerFactory.getLogger(JavalinFactory.class);

    public static Javalin createApp(DataSource dataSource) {
        Javalin app = Javalin.create(config -> {
            if (AppConfig.ENV_DEV.equals(AppConfig.getEnv())) {
                config.bundledPlugins.enableDevLogging();
            }
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });
        HomeController homeController = new HomeController();
        UrlRepository urlRepository = new UrlRepository(dataSource);
        UrlCheckRepository urlCheckRepository = new UrlCheckRepository(dataSource);
        UrlService urlService = new UrlService(urlRepository, urlCheckRepository);
        UrlController urlController = new UrlController(urlService);
        app.get(NamedRoutes.homePath(), homeController::index);
        app.get(NamedRoutes.urlsPath(), urlController::index);
        app.get(NamedRoutes.urlPath("{id}"), urlController::show);
        app.post(NamedRoutes.urlsPath(), urlController::create);
        app.post(NamedRoutes.urlChecksPath("{id}"), urlController::check);
        app.exception(SQLException.class, (e, ctx) -> {
            log.error(e.getMessage(), e);
            ctx.status(500).result("DB connection error.");
        });
        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver(TEMPLATES_NAME, classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }
}
