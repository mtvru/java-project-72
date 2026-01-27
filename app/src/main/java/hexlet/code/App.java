/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Javalin app = JavalinFactory.createApp();
            app.start(AppConfig.getPort());
        } catch (Exception e) {
            log.error("Fatal error: ", e);
            System.exit(1);
        }
    }
}
