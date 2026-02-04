/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String dbUrl = AppConfig.getDatabaseUrl();

        if (dbUrl == null || dbUrl.isBlank()) {
            log.error(AppConfig.KEY_DATABASE_URL + " is required!");
            System.exit(1);
        }

        try {
            DataSource dataSource = DataSourceFactory.createDataSource();
            DatabaseInitializer.runMigrations(dataSource);
            Javalin app = JavalinFactory.createApp(dataSource);
            app.start(AppConfig.getPort());
        } catch (Exception e) {
            log.error("Fatal error: ", e);
            System.exit(1);
        }
    }
}
