package hexlet.code;

public final class AppConfig {
    public static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }

    public static String getDatabaseUrl() {
        return System.getenv()
                .getOrDefault("DATABASE_URL", "jdbc:h2:mem:page_analyzer;DB_CLOSE_DELAY=-1;");
    }
}
