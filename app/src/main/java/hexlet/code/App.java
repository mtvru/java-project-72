/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.BaseRepository;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
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

public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static Javalin getApp() {
        log.info("Starting application and initializing database...");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDatabaseUrl());

        HikariDataSource dataSource = null;
        try {
            dataSource = new HikariDataSource(hikariConfig);
            String sql = readResourceFile("schema.sql");
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
        app.get("/", ctx -> ctx.render("index.jte"));
        return app;
    }

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Javalin app = getApp();
            app.start(getPort());
        } catch (Exception e) {
            log.error("Fatal error: ", e);
            System.exit(1);
        }
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }

    private static String getDatabaseUrl() {
        return System.getenv()
                .getOrDefault("DATABASE_URL", "jdbc:h2:mem:page_analyzer;DB_CLOSE_DELAY=-1;");
    }

    private static String readResourceFile(String fileName) throws IOException {
        // Get the path to the file in src/main/resources
        try (InputStream url = App.class.getClassLoader().getResourceAsStream(fileName)) {
            if (url == null) {
                throw new IllegalStateException("File " + fileName + " not found in resources.");
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