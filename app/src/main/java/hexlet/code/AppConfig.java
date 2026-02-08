package hexlet.code;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConfig {
    public static final String ENV_DEV = "development";
    public static final String KEY_DATABASE_URL = "DATABASE_URL";

    public static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }

    public static String getEnv() {
        return System.getenv().getOrDefault("APP_ENV", ENV_DEV);
    }

    public static String getDatabaseUrl() {
        return System.getenv(KEY_DATABASE_URL);
    }

    public static int getMaximumPoolSize() {
        String size = System.getenv().getOrDefault("MAX_POOL_SIZE", "10");
        return Integer.parseInt(size);
    }
}
