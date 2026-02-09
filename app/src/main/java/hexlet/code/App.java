/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final HikariDataSource DATA_SOURCE = HikariDataSourceFactory.create();

    public static Javalin getApp() throws Exception {
        DatabaseInitializer.runMigrations(DATA_SOURCE);
        return JavalinFactory.createApp(DATA_SOURCE);
    }

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String dbUrl = AppConfig.getDatabaseUrl();
        if (dbUrl == null || dbUrl.isBlank()) {
            LOG.error(AppConfig.KEY_DATABASE_URL + " is required!");
            System.exit(1);
        }
        Javalin app = null;
        try {
            app = getApp();
            app.start(AppConfig.getPort());
            Javalin finalApp = app;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Received shutdown signal, stopping app...");
                finalApp.stop();
                DATA_SOURCE.close();
            }));
        } catch (Exception e) {
            LOG.error("Fatal error: ", e);
            if (app != null) {
                app.stop();
            }
            DATA_SOURCE.close();
            System.exit(1);
        }
    }
}
