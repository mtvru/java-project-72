package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HikariDataSourceFactory {
    public static HikariDataSource create() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(AppConfig.getDatabaseUrl());
        hikariConfig.setMaximumPoolSize(AppConfig.getMaximumPoolSize());
        return new HikariDataSource(hikariConfig);
    }
}
