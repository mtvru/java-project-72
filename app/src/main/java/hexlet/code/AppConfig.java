package hexlet.code;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConfig {
    public static String ENV_DEV = "development";
    public static String DATABASE_URL = "DATABASE_URL";

    public static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }

    public static String getEnv() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    public static String getDatabaseUrl() {
        return System.getenv(DATABASE_URL);
    }
}
