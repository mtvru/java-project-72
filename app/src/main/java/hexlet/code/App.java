/**
 * This package contains the main application entry point.
 */
package hexlet.code;

import io.javalin.Javalin;

public final class App {
    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
        });
        app.get("/", ctx -> ctx.result("Hello World"));

        return app;
    }

    /**
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }
}
