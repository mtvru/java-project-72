package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.HomeController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class JavalinFactory {
    private static final Logger log = LoggerFactory.getLogger(JavalinFactory.class);

    public static Javalin createApp() {
        log.info("Starting application and initializing database...");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(AppConfig.getDatabaseUrl());

        HikariDataSource dataSource = null;
        try {
            dataSource = new HikariDataSource(hikariConfig);
            String sql = readResourceFile();
            executeSql(dataSource, sql);
            BaseRepository.dataSource = dataSource;
            log.info("Database initialized successfully.");
        } catch (Exception e) {
            if (dataSource != null) {
                dataSource.close();
            }
            throw new RuntimeException("Failed to initialize database", e);
        }

        final HikariDataSource dsToClose = dataSource;
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });
        app.events(event -> {
            event.serverStopping(dsToClose::close);
        });
        app.get(NamedRoutes.homePath(), HomeController::index);
        app.get(NamedRoutes.urlsPath(), UrlController::index);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
        app.post(NamedRoutes.urlsPath(), UrlController::create);
        return app;
    }

    private static String readResourceFile() throws IOException {
        // Get the path to the file in src/main/resources
        try (InputStream url = App.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (url == null) {
                throw new IllegalStateException("File " + "schema.sql" + " not found in resources.");
            }
            return new BufferedReader(new InputStreamReader(url, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static void executeSql(DataSource ds, String sql) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
        }
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }
}
