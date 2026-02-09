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
    private static Javalin app;

    static {
        try {
            DatabaseInitializer.runMigrations(DATA_SOURCE);
            app = JavalinFactory.createApp(DATA_SOURCE);
            app.events(event -> {
                event.serverStopped(() -> {
                    LOG.info("Stopping HikariDataSource...");
                    DATA_SOURCE.close();
                });
                event.serverStopFailed(() -> {
                    LOG.error("serverStopFailed stopping HikariDataSource...");
                    DATA_SOURCE.close();
                });
            });
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Received shutdown signal, stopping app...");
                app.stop();
            }));
        } catch (Exception e) {
            LOG.error("Fatal error: ", e);
            if (app != null) {
                app.stop();
            }
            System.exit(1);
        }
    }

    public static Javalin getApp() {
        return app;
    }

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Javalin javalinApp = getApp();
        javalinApp.start(AppConfig.getPort());
    }
}
