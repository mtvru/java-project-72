/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class App {
    public static Javalin getApp() {
        HikariDataSource dataSource = HikariDataSourceFactory.create();
        try {
            DatabaseInitializer.runMigrations(dataSource);
            Javalin app = JavalinFactory.createApp(dataSource);
            app.events(event -> {
                event.serverStopped(() -> {
                    log.info("Stopping HikariDataSource...");
                    dataSource.close();
                });
                event.serverStopFailed(() -> {
                    log.error("serverStopFailed stopping HikariDataSource...");
                    dataSource.close();
                });
            });
            return app;
        } catch (Exception e) {
            dataSource.close();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Javalin app = null;
        try {
            app = getApp();
            app.start(AppConfig.getPort());
            Javalin finalApp = app;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Received shutdown signal, stopping app...");
                finalApp.stop();
            }));
        } catch (Exception e) {
            log.error("Fatal error: ", e);
            if (app != null) {
                app.stop();
            }
            System.exit(1);
        }
    }
}
